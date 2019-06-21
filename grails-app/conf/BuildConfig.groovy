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

    repositories {
        mavenLocal()
        mavenCentral()
        grailsCentral()
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        runtime('org.apache.lucene:lucene-core:6.3.0')
        runtime('org.apache.lucene:lucene-queries:6.3.0')
        runtime('org.apache.lucene:lucene-misc:6.3.0')
        runtime('org.apache.lucene:lucene-analyzers-common:6.3.0')
        runtime('org.apache.lucene:lucene-queryparser:6.3.0')
        runtime 'postgresql:postgresql:9.1-901.jdbc4'
        runtime 'xml-apis:xml-apis:1.4.01'
        compile('dom4j:dom4j:1.6.1') {
            exclude 'xml-apis'
        }
        runtime 'jaxen:jaxen:1.1.4'
        compile 'org.codehaus.gpars:gpars:1.2.1'
//        runtime 'org.grails.plugins:cinnamon-humulus:0.3'
        runtime 'commons-net:commons-net:3.2'
        compile 'javax.mail:mail:1.4.7'
        test "org.gebish:geb-spock:${gebPluginVersion}"

        runtime 'org.apache.httpcomponents:httpclient:4.5.1'
        compile 'com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.2'
        compile 'org.codehaus.woodstox:woodstox-core-asl:4.4.1'
        // unboundId sdk used under LGPL 2.1 license
        compile 'com.unboundid:unboundid-ldapsdk:4.0.4'
        compile 'com.dewarim.cinnamon:cinnamon-model:0.1'
    }

    plugins {
//        runtime (":hibernate:3.6.10.19") {
        runtime(":hibernate4:4.3.8.1") {
            export = false
        }
        compile(":release:3.1.1") {
            export = false
        }
        build(":tomcat:8.0.15") {
            export = false
        }

        runtime ":jquery:1.11.1"
        runtime ":resources:1.2.14"
        compile(':spring-security-core:2.0.0')
        compile ":rest-client-builder:2.1.1"
        compile(":twitter-bootstrap:3.2.0.2") { excludes 'svn' }
        compile ":cinnamon-db:3.8.9"
        runtime ':tika-parser:1.13.1'
        compile ":remote-pagination:0.4.8"
        compile ":geb:${gebPluginVersion}"
        compile ":asset-pipeline:2.11.0"
        runtime "org.grails.plugins:cors:1.1.8"
        compile "org.grails.plugins:quartz:1.0.2"
    }
}

grails.project.fork = [
        test   : [maxMemory: 4096, minMemory: 2048, debug: false, maxPerm: 256, daemon: true], // configure settings for 
        // the 
        // test-app JVM
        run    : [maxMemory: 4096, minMemory: 2048, debug: false, maxPerm: 256], // configure settings for the run-app JVM
        war    : [maxMemory: 4096, minMemory: 2048, debug: false, maxPerm: 256], // configure settings for the run-war JVM
        console: [maxMemory: 768, minMemory: 2048, debug: false, maxPerm: 256]// configure settings for the Console UI
        // JVM
]