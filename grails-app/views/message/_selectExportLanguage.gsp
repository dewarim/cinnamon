<p><strong><g:message code="message.export.xml"/></strong></p>
<div class="ajax_form" style="padding-top: 2ex;padding-bottom: 2ex;">
	<g:form controller="message" action="exportMessages">
		<label for="exportLanguage"><g:message code="message.export.language"/> </label>
	<g:select id="exportLanguage" from="${languages}" name="language" optionKey="id" optionValue="${{cinnamon.i18n.LocalMessage.loc(it.isoCode) }}"/>
	<g:submitButton name="doExport" value="${message(code:'message.export')}"/>
	</g:form>
</div>
<p><strong><g:message code="message.export.xliff"/></strong></p>
<div class="ajax_form" style="padding-top: 2ex;padding-bottom: 2ex;">
	<g:form controller="message" action="exportMessagesXliff">
		<label for="exportLanguage"><g:message code="message.export.language"/> </label>
	<g:select id="exportLanguage" from="${languages}" name="language" optionKey="id" optionValue="${{cinnamon.i18n.LocalMessage.loc(it.isoCode) }}"/>
	<label for="targetLanguage"><g:message code="message.export.targetLanguage"/> </label>
	<g:select id="targetLanguage" from="${languages}" name="targetLanguage" optionKey="id" optionValue="${{cinnamon.i18n.LocalMessage.loc(it.isoCode) }}"/>

	<g:submitButton name="doExport" value="${message(code:'message.export')}"/>
	</g:form>
</div>
