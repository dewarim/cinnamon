<%@ page import="cinnamon.Acl;" %>
<g:form onsubmit="return false;">
	<input type="hidden" name="osd" value="${osd.id}">
    <input type="hidden" name="fieldName" value="acl" />
    <g:select from="${Acl.list()}" name="fieldValue" value="${osd.acl.id}"
        optionKey="id" optionValue="name"/>
	<g:submitToRemote url="[action:'saveField', controller:'osd']"
			update="[success:'objectDetails', failure:'message']"
		onFailure="showClearButton();"		
		value="${message(code:'data.save')}"
	/>
</g:form>