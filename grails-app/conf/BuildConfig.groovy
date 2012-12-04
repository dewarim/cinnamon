grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
grails.project.war.file = "target/${appName}.war"
grails.project.repos.default = "myRepo"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
        excludes "xercesImpl", "xmlParserAPIs", "xml-apis", 'groovy';
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins
        mavenRepo name:'myRepo'
        mavenLocal()
       
        grailsRepo "http://grails.org/plugins"
        mavenCentral()
        
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment these to enable remote dependency resolution from public Maven repositories
        //mavenCentral()
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.16'

        runtime('org.apache.lucene:lucene-core:3.6.1')
        runtime('org.apache.lucene:lucene-xml-query-parser:3.6.1')
        runtime('org.apache.lucene:lucene-queries:3.6.1')
        runtime 'postgresql:postgresql:9.1-901.jdbc4'
        runtime 'dom4j:dom4j:1.6.1'
        runtime 'jaxen:jaxen:1.1.4'
        compile 'org.codehaus.gpars:gpars:1.0-beta-3'
        runtime 'cinnamon:humulus:0.1.2'
        compile("org.codehaus.groovy.modules.http-builder:http-builder:0.5.2"){
            excludes "groovy"
        }
    }

    plugins {
        runtime ":hibernate:$grailsVersion"
        compile ":release:2.0.4"
        build ":tomcat:$grailsVersion"
        
        runtime ":jquery:1.8.0"
        runtime ":resources:1.1.6"
        compile(':spring-security-core:1.2.7.3')
        compile (":twitter-bootstrap:2.1.0.1"){excludes 'svn'}
        
        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"

        runtime 'cinnamon:cinnamon-db:0.2.37'
        compile ":remote-pagination:0.3"
        test (':spock:0.6'){
            excludes: 'groovy'
        }
    }
}
