<%@ page import="cinnamon.i18n.Language;" %>
<g:form onsubmit="return false;">
	<input type="hidden" name="osd" value="${osd.id}">
    <input type="hidden" name="fieldName" value="language_id" />
    <g:select from="${Language.list()}" name="fieldValue" value="${osd.language.id}"
        optionKey="id" optionValue="isoCode"/>
	<g:submitToRemote url="[action:'saveField', controller:'osd']"
			update="[success:'objectDetails', failure:'message']"
		onFailure="showClearButton();"		
		value="${message(code:'data.save')}"
	/>
</g:form>