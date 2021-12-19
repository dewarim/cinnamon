package cinnamon

import cinnamon.index.IndexJob
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.store.Directory
import org.apache.lucene.store.SimpleFSDirectory
import org.apache.lucene.store.SingleInstanceLockFactory

import cinnamon.index.Repository
import cinnamon.index.IndexCommand
import cinnamon.index.CommandType
import cinnamon.index.LuceneResult
import cinnamon.index.SearchableDomain

import java.nio.file.Paths
import java.text.DecimalFormat

class LuceneService {

    def grailsApplication
    def itemService
    def infoService

    static transactional = false

    static Repository repository

    void initialize() {
        BooleanQuery.maxClauseCount = 10000;
        Analyzer standardAnalyzer = new StandardAnalyzer()
        String name = infoService.repositoryName
        log.debug("create repository object for ${name}")
        try {
            Analyzer analyzer = new LimitTokenCountAnalyzer(standardAnalyzer, Integer.MAX_VALUE)
            File indexFolder = new File(grailsApplication.config.luceneIndexPath, name.toString())
            Directory indexDir = new SimpleFSDirectory(Paths.get(indexFolder.absolutePath), new SingleInstanceLockFactory())
            Repository luceneRepository = new Repository(name: name,
                    indexDir: indexDir, indexFolder: indexFolder,
                    analyzer: analyzer)
            luceneRepository.createWriter()
            repository = luceneRepository
        } catch (IOException e) {
            log.debug("failed to initialize lucene for repository $name", e)
            throw new RuntimeException("error.lucene.IO", e);
        }
    }

    /**
     *
     * @param query a simple query string
     * @param domain currently, the domain class name
     * @return
     */
    LuceneResult search(String query, SearchableDomain domain) {
        return searchXml(query, domain, [])
    }

    /**
     *
     * @param query a simple query string
     * @param domain currently, the domain class name
     * @param fields List of fields for which content stored in the Lucene index should be returned.
     *
     * @return
     */
    LuceneResult search(String query, SearchableDomain domain, List fields) {
        def cmd = new IndexCommand(repository: repository, type: CommandType.SEARCH,
                query: query, domain: domain)
        LuceneResult result = cmd.repository.doSearch(cmd) // TODO: beautify
        if (result.failed) {
            log.debug("search error: ${result.errorMessage}")
        }
        return result
    }

    /**
     *
     * @param query an XML query string
     * @param domain currently, the domain class name, may be null
     * @return
     */
    LuceneResult searchXml(String query, SearchableDomain domain) {
        return searchXml(query, domain, [])
    }

    /**
     *
     * @param query an XML query string
     * @param domain currently, the domain class name, may be null
     * @param fields List of fields for which content stored in the Lucene index should be returned.
     * @return
     */
    LuceneResult searchXml(String query, SearchableDomain domain, List fields) {
        def cmd = new IndexCommand(repository: repository, type: CommandType.SEARCH,
                query: query, domain: domain, xmlQuery: true, fields: fields)
        return cmd.repository.doSearch(cmd) // TODO: beautify
    }

    private static final DecimalFormat formatter =
            new DecimalFormat("00000000000000000000");

    /**
     * Pad an integer number to the decimal string format internally used by the LuceneIndexer
     * @param n the number
     * @return a zero-padded string with a length of 20 characters   
     */
    public static String pad(Integer n) {
        return formatter.format(n);
    }

    /**
     * Pad an long number to the decimal string format internally used by the LuceneIndexer
     * @param n the number
     * @return a zero-padded string with a length of 20 characters   
     */
    public static String pad(Long n) {
        return formatter.format(n);
    }

    // TODO: the fields list is currently not returned in the results - needs a new container object
    Set fetchSearchResults(String query, UserAccount user, SearchableDomain domain, List<String> fields) {
        LuceneResult results = searchXml(query, domain, fields)
        log.debug("results before filter: " + results.itemIdMap.size())
        
        def validator = new Validator(user)
        results.filterResultToSet(domain, itemService, validator)
    } 
    
    Set fetchSearchResultIds(String query, SearchableDomain domain, List<String> searchFields, BrowseAcls browseAcls) {
        LuceneResult results = searchXml(query, domain, searchFields)
        log.debug("results before filter: " + results.itemIdMap.size())
        Set<Long> browsableIds = new HashSet<>();
        results.itemIdMap.get(domain.name)?.forEach{id ->
            Map<String,String>  fields = results.idFieldMap.get(id);
            // migration control:
            if(!fields.containsKey("acl") || !fields.containsKey("owner")){
                throw new IllegalStateException("Search is not configured correctly. Please make acl and owner fields storable. This requires a new index.")
            }
            Long objectAclId = Long.parseLong(fields.get("acl"));
            Long ownerAclId = Long.parseLong(fields.get("owner"));
            if(browseAcls.hasUserBrowsePermission(objectAclId) || browseAcls.hasOwnerBrowsePermission(ownerAclId)){
                browsableIds.add(id);
            }
        }
        if(domain == SearchableDomain.OSD) {
            log.debug("check if OSDs found by search really exist:")
            Set<Long> existingIds = new HashSet<>();
            while (!browsableIds.isEmpty()) {
                def first1000 = browsableIds.take(1000)
                def existingOsds = ObjectSystemData.findAll("from ObjectSystemData o where o.id in (:ids)", [ids: first1000.asList()])
                existingOsds.forEach{osd -> existingIds.add(osd.id)}
                browsableIds = browsableIds.drop(1000)
            }
            log.debug("Existence check result: ${existingIds.size()} / ${browsableIds.size()} exist.")
            return existingIds;
        }
        return browsableIds;
    }


    void waitForIndexer() {
        def jobCount = IndexJob.countByFailed(false)
        while (jobCount > 0) {
            log.info("Waiting for indexer to finish indexing. Current active index tasks: " + jobCount)
            Thread.sleep(2000)
            jobCount = IndexJob.countByFailed(false)
        }
    }
}
