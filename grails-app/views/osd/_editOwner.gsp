<%@ page import="cinnamon.UserAccount" %>
<g:form onsubmit="return false;">
	<input type="hidden" name="osd" value="${osd.id}">
    <input type="hidden" name="fieldName" value="owner" />
    <g:select from="${UserAccount.list()}" name="fieldValue" value="${osd.owner.id}"
        optionKey="id" optionValue="name"/>
	<g:submitToRemote url="[action:'saveField', controller:'osd']"
			update="[success:'objectDetails', failure:'message']"
		onFailure="showClearButton();"		
		value="${message(code:'data.save')}"
	/>
</g:form>