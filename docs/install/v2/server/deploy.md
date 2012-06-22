# Installing Cinnamon Server v2 - Configuration and Deployment

## configuration files: Server

The configuration files should be placed in the cinnamon-system directory.
Then you have to add an environment variable CINNAMON_HOME_DIR which points to this
directory. If you want to use the administrative tool, define a DANDELION_HOME_DIR variable
which points to the same directory.

### cinnamon_config.xml

The main [Cinnamon configuration file](../configure/config.md) has its own page which explain all the settings.
You need to edit the file to supply the correct connection parameters for the database and to tell Cinnamon 
where to store data and index files.

### logback.xml

Cinnamon uses [Logback](http://logback.qos.ch/) as its main logging framework.
Copy Server/logback.example.xml to logback.xml.

To change the logging level or enable/disable certain logging behaviours, edit this file.
Activating all log options on debug level will create *very* large log files (GBytes) in short time,
so you should watch out for out-of-disk space during debugging (or configure logback to only
use debug settings on the relevant classes)

### lucene.properties

Cinnamon uses the Lucene search library for indexing and searching content. In most cases,
it should suffice to use the synchronous indexing facilities. But if you want to index many objects
at once, for example when after changes to the index items a re-built of the index is necessary,
you can use the background thread of the Lucene indexer. This will be configured in the
lucene.properties file, which defines the following properties:

* indexDir: where the index will be stored. Your servlet container (for example, Tomcat 7) needs
 read/write permissions on this folder.
* sleepBetweenRuns: to prevent the IndexServer from querying the database all the time, it should "rest"
 once in a while. You can specify how many milliseconds it should wait before starting with the next batch.
* itemsPerRun: how many objects should be indexed in one run.

Example file:

    # configuration file for Lucene indexer   
    indexDir=/home/cinnamon/cinnamon-system/index
    sleepBetweenRuns=3000
    itemsPerRun=100

## configuration files: Administrator's GUI

### dandelion-config.groovy

The Dandelion configuration file defines the names of several types of classes which
are used to customize Cinnamon server object types.

* _indexers_: used for IndexItems (Lucene configuration)
* _vaProviders_: value assistence providers may generate lists for the client gui to use in forms, (for example names of searchable fields)
* _triggerClasses_: specialized classes used in ChangeTriggers which add new behaviour to the Cinnamon API
* _relationResolvers_: classes which govern the resolution of object to object relations.
* _transformers_: receive a Cinnamon object of format/type X/Y save or return it as format/type A/B
* _lifeCycleStateClasses_: classes used for validation of lifecycle state changes.

Classes defined here must be available in the Cinnamon server and Dandelion classpath, ideally
in the same war/jar file to prevent classloading problems.

Example entry:

    indexers = [ 'cinnamon.index.indexer.DefaultIndexer', ]


### dandelion.log4j.properties

Standard log4j configuration file, see the [official introduction to log4j](http://logging.apache.org/log4j/1.2/manual.html) for more details.

### database-config.groovy

The database-config.groovy file contains the database connections and repository layout for all Grails based tools
that may access Cinnamon. Currently in Cinnamon 2, those are Dandelion for administration and Illicium the web client.

    dbconnections {
        demo { 
                id = 1
                prefix = 'demo'
                driverClassName = 'org.postgresql.Driver'
                cinnamonServerUrl = 'http://127.0.0.1:8080/cinnamon/cinnamon'
                jdbcType = 'postgresql'
                host = '127.0.0.1'
                port = 5432
                dbName = 'demo'
                dbname = 'demo' // legacy with lower-cased name
                username = 'cinnamon'
                password = 'cinnamon'
                encryptPasswords='true'
                dbUser = 'cinnamon'
                dbPassword = 'cinnamon'
                dbType = 'postgresql'
        }
    }

The prefix is used in the selection menu on the login page. For legacy reasons, the name of the connection and the
prefix must be identical. The id must be unique. Breaking this file makes for interesting times.

## configure database

The default configuration of cinnamon expects a database owner with username and password "cinnamon".
If you are using Postgresql, you can just import the downloadable demo database. 

// TODO: link to download.
// TODO: tutorial for bootstrapping on other platforms.

## configure servlet container

Generally, you will have to increase the amount of RAM that the JVM of the web server has available:

    -Xmx1200m -Xms1200m -XX:PermSize=500m -XX:MaxPermSize=500m 

are good values for a medium sized production ready setting where users may upload 50MByte of XML content and
do expect fast indexing.

You must set the environment variables CINNAMON_HOME_DIR and DANDELION_HOME_DIR, which should point to the
cinnamon-system directory (for example, CINNAMON_HOME_DIR=/home/cinnamon/cinnamon-system). Depending on your
machine, a restart may be necessary afterwards to test / ensure that the environment variables are loaded at boot time.

### Tomcat 7

Tomcat may not have access to the same environment variables as the average user. So depending on your system
layout, you can add the environment variables to the Tomcat startup script or configuration files.
On Ubuntu 12.04 server, that would be /etc/default/tomcat7, and the variables something like:

    export CINNAMON_HOME_DIR=/home/cinnamon/data
    export DANDELION_HOME_DIR=/home/cinnamon/data

In the server.xml file, add maxPostSize to the connector:

    <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               maxPostSize="100000000"
               URIEncoding="UTF-8"
               redirectPort="8443" />


## actual deployment

If the database is ready, the servlet container is configured and the configuration files have been setup correctly,
you are now ready to deploy the cinnamon.war:

1.    stop your webserver/servlet container,
2.    remove the old Cinnamon server installation
3.    copy the new cinnamon.war into the webserver's webapps directory
4.    start your webserver

If your servlet container has auto-deploy enabled, it should pick up and install the cinnamon.war automatically from its webapps directory. 
More advanced servers may be able to do this on-the-fly, but this can break existing database connection pooling and 
(in some cases on Tomcat 6) may also exhaust PermGenSpace in the virtual machine,
 so stop-deploy-restart is the recommended way.
