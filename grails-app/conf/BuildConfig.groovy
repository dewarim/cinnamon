grails.servlet.version = "3.0" 
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
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

    def gebPluginVersion = '0.9.0-RC-1'
    def seleniumVersion = "2.31.0"



    repositories {
        inherits true // Whether to inherit repository definitions from plugins
        mavenRepo name:'myRepo'
        mavenLocal()
       
        grailsRepo "http://grails.org/plugins"
        mavenCentral()
        
        grailsPlugins()
        grailsHome()
        grailsCentral()
//        mavenRepo "http://mvnrepository.com"
        // uncomment these to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        runtime('org.apache.lucene:lucene-core:3.6.2')
        runtime('org.apache.lucene:lucene-xml-query-parser:3.6.2')
        runtime('org.apache.lucene:lucene-queries:3.6.2')
        runtime 'postgresql:postgresql:9.1-901.jdbc4'
        runtime 'dom4j:dom4j:1.6.1'
        runtime 'jaxen:jaxen:1.1.4'
        compile 'org.codehaus.gpars:gpars:1.0.0'
        runtime 'cinnamon:cinnamon-humulus:0.2.1'
        
        // TODO: is this used anywhere? (commented out to test this ;) )
//        compile("org.codehaus.groovy.modules.http-builder:http-builder:0.6.0"){
//            excludes "groovy"
//        }
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
        test "org.gebish:geb-spock:${gebPluginVersion}" 
        
        /*
         * Note: run test like -Dgeb.env=firefox test-app -functional RelationController
         * because htmlunit-driver seems to be broken at the moment.
         */
        test "org.seleniumhq.selenium:selenium-firefox-driver:${seleniumVersion}"
        test "org.seleniumhq.selenium:selenium-chrome-driver:${seleniumVersion}"       
        test ("org.seleniumhq.selenium:selenium-htmlunit-driver:${seleniumVersion}"){
            exclude 'xml-apis'
        }
        test "org.seleniumhq.selenium:selenium-support:${seleniumVersion}"
    }

    plugins {
        runtime (":hibernate:$grailsVersion"){
            export = false
        }
        compile (":release:2.2.1"){
            export = false
        }
        build (":tomcat:$grailsVersion"){
            export = false
        }
        
        runtime ":jquery:1.8.0"
        runtime ":resources:1.1.6"
        compile(':spring-security-core:1.2.7.3')
        compile (":twitter-bootstrap:2.1.0.1"){excludes 'svn'}
        runtime ':cinnamon-db:3.0.0.7'
        compile ":remote-pagination:0.3"
        compile ":geb:${gebPluginVersion}"
        test (':spock:0.7'){
            exclude "spock-grails-support"
        }
       
    }
}
