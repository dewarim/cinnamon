package cinnamon

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

class LuceneService {

    def grailsApplication

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
        Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_CURRENT)

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
        def cmd = new IndexCommand(repository: database, type: CommandType.SEARCH,
                query: query, domain: domain, xmlQuery:true)
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

}
