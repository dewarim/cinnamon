package cinnamon.taglib
class ButtonTagLib {
	def homeButton = { attrs, body ->
		out << """<span class="menuButton"><a class="home" href="${resource(dir:'folder', file:'index')}">""" << body() << "</a></span>"
	}
}
