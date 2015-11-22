grails.servlet.version = "3.0" 
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.8
grails.project.source.level = 1.8
grails.project.war.file = "target/${appName}.war"
grails.project.repos.default = "myRepo"
grails.project.dependency.resolver = "maven"

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
        mavenLocal()
        mavenCentral()
        grailsCentral()
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        runtime('org.apache.lucene:lucene-core:3.6.2')
        runtime('org.apache.lucene:lucene-xml-query-parser:3.6.2')
        runtime('org.apache.lucene:lucene-queries:3.6.2')
        runtime 'postgresql:postgresql:9.1-901.jdbc4'
        runtime 'xml-apis:xml-apis:1.4.01'
        runtime('dom4j:dom4j:1.6.1'){
            exclude 'xml-apis'
        }
        runtime 'jaxen:jaxen:1.1.4'
        compile 'org.codehaus.gpars:gpars:1.2.1'
//        runtime 'org.grails.plugins:cinnamon-humulus:0.3'
        runtime 'commons-net:commons-net:3.2'
        compile 'javax.mail:mail:1.4.7'
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

        runtime 'org.apache.httpcomponents:httpclient:4.5.1'
    }

    plugins {
//        runtime (":hibernate:3.6.10.19") {
        runtime (":hibernate4:4.3.8.1"){
            export = false
        }
        compile (":release:3.1.1"){
            export = false
        }
        build (":tomcat:8.0.15"){
            export = false
        }
        
        runtime ":jquery:1.11.1"
//        runtime ":resources:1.2.RC2"
        compile(':spring-security-core:2.0-RC5')
        compile ":rest-client-builder:2.1.1"
        compile (":twitter-bootstrap:3.2.0.2"){excludes 'svn'}
        compile ":cinnamon-db:3.6.8"
        runtime ':tika-parser:1.3.0.1'
        compile ":remote-pagination:0.3"
        compile ":geb:${gebPluginVersion}"
        compile ":asset-pipeline:1.9.9"
    }
}
