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
import cinnamon.index.SearchableDomain
import humulus.Environment

class LuceneService {

    def grailsApplication

    static transactional = false

    static Map<String, Repository> repositories = new HashMap<String, Repository>()

    static LuceneActor lucene

    static {
        lucene = new LuceneActor()
        lucene.start()
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
                throw new RuntimeException("error.lucene.IO", e);
            }
        }
        lucene.repositories = repositories

    }

    void addToIndex(Indexable indexable, String database) {
        def cmd = new IndexCommand(indexable: indexable, repository: database, type: CommandType.ADD_TO_INDEX)
        // we have to sendAndWait, because otherwise the thread would finish and
        // confuse the actor before its work is done.
//        lucene.sendAndWait(cmd, 4, TimeUnit.SECONDS)
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
     * @param domainRestriction currently, the domain class name
     * @return
     */
    LuceneResult search(String query, String database, SearchableDomain domain) {
        def cmd = new IndexCommand(repository: database, type: CommandType.SEARCH,
                query: query, domain: domain)
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
