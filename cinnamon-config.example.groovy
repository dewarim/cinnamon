/*
 * Put this file either in:
 *  $userDirectory/.grails
 *  or
 *  $CINNAMON_HOME_DIR
 *  
 * Note: this configuration file is a first step to loading the Cinnamon configuration
 * from a groovy file instead of the traditional cinnamon_config.xml.
 * I'm still undecided if this is really the better approach - it certainly is much
 * easier to "just load this config" with Grails, but the XML file could be 
 * a safer way (along with a xsd file which describes the XML Schema).  
 */

/*
 * There were some issues loading this configuration files, so the current BootStrap
 * code looks if the configLoaded-parameter is set in grailsApplication.config.configLoaded.
 */
configLoaded = true

/*
 * How many rounds the bcrypt algorithm should run to hash the password.
 * 10 is a sensible default, going much higher will make login painfully slow.
 */
passwordRounds = 10

/*
 * You can opt to not encrypt passwords.
 * (this settings exists for debugging and legacy purposes)
 */
encryptPasswords = true

/*
 * Where the system files are kept (currently: log files)
 */
system_root = '/opt/cinnamon/cinnamon-system'

/*
 * Where the data files are kept.
 */
data_root = '/opt/cinnamon/cinnamon-data'

/*
 * Where to store the Lucene index files.
 * The recommended place is in $data_root/index, 
 * but some legacy systems have stored the index in system_root/index.
 */
luceneIndexPath = '/opt/cinnamon/cinnamon-data/index'

/*
 * System administrator - currently not used.
 */
system_administrator = 'ingo_wiarda@dewarim.de'

default_repository = 'demo'

// Size of preview images in folder view.
previewSize = 128

// Database connection properties:
environments{
    development{
        dataSource {
            dialect = org.hibernate.dialect.PostgreSQLDialect
            driverClassName = 'org.postgresql.Driver'
            username = 'cinnamon'
            password = 'cinnamon'
            url = 'jdbc:postgresql://localhost/demo'
            properties {
                maxActive = -1
                minEvictableIdleTimeMillis = 1800000
                timeBetweenEvictionRunsMillis = 1800000
                numTestsPerEvictionRun = 3
                testOnBorrow = true
                testWhileIdle = true
                testOnReturn = true
                validationQuery = "SELECT 1"
            }
        }
    }
    production{
        dataSource {
            dialect = org.hibernate.dialect.PostgreSQLDialect
            driverClassName = 'org.postgresql.Driver'
            username = 'cinnamon'
            password = 'cinnamon'
            url = 'jdbc:postgresql://localhost/demo'
            properties {
                maxActive = -1
                minEvictableIdleTimeMillis = 1800000
                timeBetweenEvictionRunsMillis = 1800000
                numTestsPerEvictionRun = 3
                testOnBorrow = true
                testWhileIdle = true
                testOnReturn = true
                validationQuery = "SELECT 1"
            }
        }
    }
    
}

/*
 This defines a dataSource for the audit-log for lifecycle changes.
 You may use one of your Cinnamon repository databases or a separate
 (depending on your security requirements)
 This config file uses the "demo" repository.
 */
dataSource_logging {
    dialect = org.hibernate.dialect.PostgreSQLDialect
    driverClassName = 'org.postgresql.Driver'
    username = 'cinnamon'
    password = 'cinnamon'
    url = 'jdbc:postgresql://localhost/demo'
    properties {
        maxActive = -1
        minEvictableIdleTimeMillis = 1800000
        timeBetweenEvictionRunsMillis = 1800000
        numTestsPerEvictionRun = 3
        testOnBorrow = true
        testWhileIdle = true
        testOnReturn = true
        validationQuery = "SELECT 1"
    }
}

/*
 * Repositories. Those will be available for login with the webclient and desktop client.
 * (The complete repository definition is still kept in cinnamon_config.xml)
 */
repositories {
    repository {
        name = 'demo'
        categories {
            category {
                name = 'DEMO'
            }
        }
    }    
}

/*
 * You can add your own Indexer classes here, this is a list of the default indexers.
 */
indexers = [
        'cinnamon.index.indexer.DefaultIndexer',
        'cinnamon.index.indexer.BooleanXPathIndexer',
        'cinnamon.index.indexer.DateXPathIndexer',
        'cinnamon.index.indexer.IntegerXPathIndexer',
        'cinnamon.index.indexer.DefaultIndexer',
        'cinnamon.index.indexer.DecimalXPathIndexer',
        'cinnamon.index.indexer.TimeXPathIndexer',
        'cinnamon.index.indexer.ReverseStringIndexer',
        'cinnamon.index.indexer.ReverseCompleteStringIndexer',
        'cinnamon.index.indexer.ParentFolderPathIndexer',
        'cinnamon.index.indexer.DateTimeIndexer',
        'cinnamon.index.indexer.CountIndexer',
        'cinnamon.index.indexer.DescendingReverseStringIndexer',
        'cinnamon.index.indexer.DescendingReverseCompleteStringIndexer',
        'cinnamon.index.indexer.DescendingStringIndexer',
        'cinnamon.index.indexer.DescendingCompleteStringIndexer',
        'cinnamon.index.indexer.CompleteStringExpressionIndexer',
        'cinnamon.index.indexer.EncodedFieldIndexer'
]

/*
 * Value assistance providers may be used to populate search form fields.
 */
vaProviders = [
        'cinnamon.index.valueAssistance.DefaultProvider',
]

/*
 * Trigger classes can be configured to be run before / after a request.
 */
triggerClasses = [
        'cinnamon.trigger.impl.RelationChangeTrigger',
        'cinnamon.trigger.impl.LifecycleStateAuditTrigger'
]

/*
 * Relation resolver classes - for example,
 * use the FixedRelationResolver to keep a [document : image] relation historically accurate,
 * and a LatestHeadResolver if you always want to have the document point to the newest version
 * of the related image.
 */
relationResolvers = [
        'cinnamon.relation.resolver.FixedRelationResolver',
        'cinnamon.relation.resolver.LatestHeadResolver',
        'cinnamon.relation.resolver.LatestBranchResolver'
]

/*
 * The transformer API may be used to offer direct transformations of OSDs from one
 * format to another.
 */
transformers = [
//        'cinnamon.transformation.XhtmlToPdfTransformer'
]


lifeCycleStateClasses = [
        'cinnamon.lifecycle.state.NopState',
        'cinnamon.lifecycle.state.ChangeAclState'
]

/*
 * This regex defines which resources will be delivered unchecked, that is they will be
 * delivered to unauthenticated users. This affects static website resources.
 */
humulus {
    urlFilterRegex = '(?:(?:plugins|static)/[-_.a-zA-Z0-9]+/)?(?:images|css|js)/.*(?:css|js|png|jpe?g|gif|handlebars|jar)$'
}

/*
 * Which GSP template to use to display the objects inside a folder on the webclient.
 * Do not change.
 */
templates = [
        'osd': [
                'osdList': '/osd/osdList'
        ]
]