// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

grails.config.locations = ["classpath:${appName}-config.groovy",
                           "file:${userHome}/.grails/${appName}-config.groovy",
                           "file:${System.env.CINNAMON_HOME_DIR}/${appName}-config.groovy"
]

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html         : ['text/html', 'application/xhtml+xml'],
                     xml          : ['text/xml', 'application/xml'],
                     text         : 'text/plain',
                     js           : 'text/javascript',
                     rss          : 'application/rss+xml',
                     atom         : 'application/atom+xml',
                     css          : 'text/css',
                     csv          : 'text/csv',
                     all          : '*/*',
                     json         : ['application/json', 'text/json'],
                     form         : 'application/x-www-form-urlencoded',
                     multipartForm: 'multipart/form-data'
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// enable query caching by default
grails.hibernate.cache.queries = false

grails.gorm.default.mapping = {
    cache false
}

grails.gorm.autoFlush = true
grails.gorm.failOnError = true

// set per-environment serverURL stem for creating absolute links
environments {
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.logging.jul.usebridge = false
        // TODO: grails.serverURL = "http://www.changeme.com"
    }
}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    appenders {
        'null' name: 'stacktrace'
        console name: 'stdout', layout: pattern(conversionPattern: '%d{ISO8601} %t %c %m%n')
    }

    error 'net.sf.ehcache.hibernate',
            'org.apache.tomcat.util',
            'org.apache.coyote',
            'org.apache.commons.beanutils',
//            'org.springframework',
            'org.hibernate',
            'org.apache.naming.SelectorContext',
            'net.sf.ehcache',
            'org.apache.catalina'

    error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'grails.app.taglib.org.grails.plugin.resource',
            'org.grails.plugin.resource',
            'org.codehaus.groovy.grails.io',
            'org.codehaus.groovy.grails.io.support',
            'org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource'


    info 'org.codehaus.groovy.grails.context',
            'org.codehaus.groovy.grails.io',
            'org.codehaus.groovy.grails.web',
            'cinnamon.global.ConfThreadLocal'

    info 'grails.plugins.twitterbootstrap.BootstrapResources'
    info 'cinnamon.RequestTicketAuthenticationFilter'
    debug 'cinnamon.debug.ProviderManager'
    debug 'cinnamon.CinnamonUserDetailsService'
    info 'grails.app.filters.cinnamon.filters.PageFilters'
    debug 'grails.app.filters.cinnamon.filters.TriggerFilters'
    debug 'cinnamon.data'
    debug 'cinnamon.OsdController'
    info 'cinnamon.index.LuceneActor'
    info 'cinnamon.index.LuceneMaster'
    info 'cinnamon.index.LuceneService'
    info 'cinnamon.workflow.TransitionActor'
    info 'cinnamon.workflow'
    info 'cinnamon.index'
    debug 'humulus',
            'cinnamon.UserAccountController',
            'cinnamon.FolderController',
            'cinnamon.cinnamon.filters.TriggerFilters',
            'cinnamon.index.indexer.ParentFolderPathIndexer',
            'org.springframework.security.authentication',
            'cinnamon.PreAuthenticatedAuthenticationProvider',
            'cinnamon.servlet.ResponseFilter'

    debug 'grails.app.domain.cinnamon.index'
    debug 'grails.app.controllers.cinnamon.FolderController'
    info 'org.springframework'
    debug 'grails.plugin.springsecurity.web'
    root {
        debug 'stdout'
    }
}

grails.views.javascript.library = "jquery"

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'cinnamon.UserAccount'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'cinnamon.CmnGroupUser'
grails.plugin.springsecurity.authority.className = 'cinnamon.CmnGroup'
grails.plugin.springsecurity.authority.nameField = 'name'
grails.plugin.springsecurity.userLookup.usernamePropertyName = 'name'
grails.plugin.springsecurity.userLookup.passwordPropertyName = 'pwd'
grails.plugin.springsecurity.userLookup.enabledPropertyName = 'activated'
grails.plugin.springsecurity.userLookup.authoritiesPropertyName = 'groupUsers'
grails.plugin.springsecurity.successHandler.defaultTargetUrl = '/folder/index'
grails.plugin.springsecurity.auth.loginFormUrl = '/login/auth'
grails.plugin.springsecurity.providerNames = ['preauthAuthProvider', 'daoAuthenticationProvider', 'anonymousAuthenticationProvider']
grails.plugin.springsecurity.logout.afterLogoutUrl = '/login/auth'
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        '/assets/**'              : ['permitAll'],
        '/cinnamon/cinnamon/index': ['permitAll'],
        '/cinnamon/assets/**'     : ['permitAll'],
        '/error/**'               : ['permitAll'],
]
grails.logging.jul.usebridge = false

/*
 The default page is responsible for connecting to the right database,
 so we always redirect the user there:
*/
grails.plugin.springsecurity.successHandler.alwaysUseDefault = false
grails.plugin.springsecurity.http.useExpressions = false

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */

grails.assets.minifyJs = false
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']
grails.resources.adhoc.includes = ['/images/**', '/css/**', '/js/**', '/plugins/**']
