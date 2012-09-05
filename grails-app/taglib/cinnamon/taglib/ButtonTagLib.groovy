package cinnamon.taglib
class ButtonTagLib {
    
    /**
     * Display a link to the start page.
     */
    def homeButton = { attrs, body ->
		out << """<span class="menuButton"><a class="home" href="${createLink(action:grailsApplication.config.defaultAction ?: 'index', controller: grailsApplication.config.defaultController ?: 'folder')}">""" << body() << "</a></span>"
	}

    /**
     * Display a link to the main administration page.
     */
    def adminButton = { attrs ->
		out << """<span class="menuButton">
        <a class="administration" href="${createLink(action:'index', controller: 'admin')}">
        ${message(code:"link.to.administration")}
        </a>
        </span>"""
	}
    
}
