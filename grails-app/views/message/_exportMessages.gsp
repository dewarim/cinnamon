<?xml version="1.0" encoding="UTF-8"?>
<messages isoCode="${language.isoCode}">
	<g:each in="${messages}" var="msg">
		<message><id>${outputService.replaceXmlEntities(msg.message)}</id><translation>${outputService.replaceXmlEntities(msg.translation)}</translation></message>
	</g:each>
</messages>