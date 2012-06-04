package cinnamon.taglib
class DandelionTagLib {
	def descriptionTextArea = { attrs, body ->
		out << "<textarea name=\"" << attrs.name << "\" id=\"" << attrs.name  << """" cols="32" rows="8" onKeyDown="limitText(this.form.description, 255);" onKeyUp="limitText(this.form.description, 255);">""" << attrs.value << body() << "</textarea>"
	}

	def xpathTextArea = { attrs, body ->
		out << "<textarea name=\"" << attrs.name << "\" id=\"" << attrs.name  << """" cols="80" rows="10" onKeyDown="limitText(this.form.description, 1048576);" onKeyUp="limitText(this.form.description, 1048576);">""" << attrs.value << body() << "</textarea>"
	}

	def xmlParamsTextArea = { attrs, body ->
		out << "<textarea name=\"" << attrs.name << "\" id=\"" << attrs.name  << """" cols="80" rows="10" onKeyDown="limitText(this.form.description, 2097152);" onKeyUp="limitText(this.form.description, 2097152);">""" << attrs.value << body() << "</textarea>"
	}
}