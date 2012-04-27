<%@ page import="cinnamon.ObjectType;" %>
<g:form onsubmit="return false;">
	<input type="hidden" name="osd" value="${osd.id}">
    <input type="hidden" name="fieldName" value="objtype" />
    	<g:select from="${ObjectType.list()}" name="fieldValue" value="${osd.type.id}"
			optionKey="name" optionValue="name"/>
	<g:submitToRemote url="[action:'saveField', controller:'osd']"
			update="[success:'objectDetails', failure:'message']"
		onFailure="showClearButton();"		
		value="${message(code:'data.save')}"
	/>
</g:form>