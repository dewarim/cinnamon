package cinnamon.taglib
class ButtonTagLib {
    
	def homeButton = { attrs, body ->
		out << """<span class="menuButton"><a class="home" href="${createLink(action:grailsApplication.config.defaultAction ?: 'index', controller: grailsApplication.config.defaultController ?: 'folder')}">""" << body() << "</a></span>"
	}
}
