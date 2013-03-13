package cinnamon

import cinnamon.index.ResultCollector
import cinnamon.interfaces.XmlConvertable
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.LimitTokenCountAnalyzer
import org.apache.lucene.store.Directory
import org.apache.lucene.store.SimpleFSDirectory
import org.apache.lucene.store.SingleInstanceLockFactory

import cinnamon.index.Repository
import cinnamon.index.LuceneActor
import cinnamon.index.Indexable
import cinnamon.index.IndexCommand
import cinnamon.index.CommandType
import cinnamon.index.LuceneResult
import humulus.Environment
import cinnamon.index.SearchableDomain
import cinnamon.index.LuceneBackgroundActor

import java.text.DecimalFormat

class LuceneService {

    def grailsApplication
    def itemService

    static transactional = false

    static Map<String, Repository> repositories = new HashMap<String, Repository>()

    static LuceneActor lucene
    static LuceneBackgroundActor backgroundActor

    static {
        lucene = new LuceneActor()
        lucene.start()
        backgroundActor = new LuceneBackgroundActor(lucene)
        backgroundActor.start()
    }

    void initialize() {
        Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_36)

        Environment.list().each {repo ->
            def name = repo.dbName
            log.debug("create repository object for ${name}")
            try {
                Analyzer analyzer = new LimitTokenCountAnalyzer(standardAnalyzer, Integer.MAX_VALUE)
                File indexFolder = new File(grailsApplication.config.luceneIndexPath, name.toString())
                Directory indexDir = new SimpleFSDirectory(indexFolder, new SingleInstanceLockFactory())
                Repository repository = new Repository(name: name,
                        indexDir: indexDir, indexFolder: indexFolder,
                        analyzer: analyzer)
                repository.createWriter()
                repositories.put(name, repository)
            } catch (IOException e) {
                log.debug("failed to initialize lucene for repository $name",e)
                throw new RuntimeException("error.lucene.IO", e);
            }
        }
        lucene.repositories = repositories
        backgroundActor.repositories = Environment.list().collect {it.dbName}
        def reIndexCommand = new IndexCommand(type: CommandType.RE_INDEX)
        backgroundActor.sendAndContinue(reIndexCommand){ LuceneResult result ->
            log.debug("received re-index result: ${result.resultMessages}")
        }
        
    }

    void addToIndex(Indexable indexable, String database) {
        def cmd = new IndexCommand(indexable: indexable, repository: database, type: CommandType.ADD_TO_INDEX)
        // we have to sendAndWait, because otherwise the thread would finish and
        // confuse the actor before its work is done.
        lucene.sendAndWait(cmd)
    }

    void updateIndex(Indexable indexable, String database) {
        def cmd = new IndexCommand(indexable: indexable, repository: database, type: CommandType.UPDATE_INDEX)
        lucene.sendAndWait(cmd)
    }

    void removeFromIndex(Indexable indexable, String database) {
        def cmd = new IndexCommand(indexable: indexable, repository: database, type: CommandType.REMOVE_FROM_INDEX)
        lucene.sendAndWait(cmd)
    }

    /**
     *
     * @param query a simple query string
     * @param database the database which stores the object. Must be specified, as this may change per request,
     *        depending on customer.
     * @param domain currently, the domain class name
     * @return
     */
    LuceneResult search(String query, String database, SearchableDomain domain) {
        return searchXml(query, database, domain, [])
    } 
    
    /**
     *
     * @param query a simple query string
     * @param database the database which stores the object. Must be specified, as this may change per request,
     *        depending on customer.
     * @param domain currently, the domain class name
     * @param fields List of fields for which content stored in the Lucene index should be returned.
     *
     * @return
     */
    LuceneResult search(String query, String database, SearchableDomain domain, List fields) {
        def cmd = new IndexCommand(repository: database, type: CommandType.SEARCH,
                query: query, domain: domain)
        LuceneResult result = lucene.sendAndWait(cmd)
        log.debug("LuceneService received: ${result}")
        if(result.failed){
            log.debug("search error: ${result.errorMessage}")
        }
        return result
    }
    
    /**
     *
     * @param query an XML query string
     * @param database the database which stores the object. Must be specified, as this may change per request,
     *        depending on customer.
     * @param domain currently, the domain class name, may be null
     * @return
     */
    LuceneResult searchXml(String query, String database, SearchableDomain domain) {
        return searchXml(query, database, domain, [])
    } 
    
    /**
     *
     * @param query an XML query string
     * @param database the database which stores the object. Must be specified, as this may change per request,
     *        depending on customer.
     * @param domain currently, the domain class name, may be null
     * @param fields List of fields for which content stored in the Lucene index should be returned.
     * @return
     */
    LuceneResult searchXml(String query, String database, SearchableDomain domain, List fields) {
        def cmd = new IndexCommand(repository: database, type: CommandType.SEARCH,
                query: query, domain: domain, xmlQuery:true, fields: fields)
        LuceneResult result = lucene.sendAndWait(cmd)
        log.debug("LuceneService received: ${result}")
        return result
    }

    void closeIndexes() {
        repositories.each {name, repository ->
            log.debug("close indexWriter of repository $name")
            repository.indexWriter.close()
        }
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
    public static String pad(Long n){
        return formatter.format(n);
    }

    // TODO: the fields list is currently not returned in the results - needs a new container object
    Set fetchSearchResults(String query,String repositoryName, 
            UserAccount user, SearchableDomain domain, List<String> fields) {
        LuceneResult results = searchXml(query, repositoryName, domain, fields)
        def validator = new Validator(user)
        results.filterResultToSet(domain, itemService, validator)
    }
    
}
