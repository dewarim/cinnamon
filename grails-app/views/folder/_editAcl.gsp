<%@ page import="cinnamon.Acl" %>
<g:form>
	<input type="hidden" name="folder" value="${folder.id}">
    <input type="hidden" name="fieldName" value="acl">
	<g:select from="${Acl.list()}" name="fieldValue" value="${folder.acl.id}"
			optionKey="id" optionValue="name"/>

	<g:submitToRemote url="[action:'saveField', controller:'folder']"
			update="[success:'folderMeta', failure:'message']"
		onFailure="showClearButton();"		
		value="${message(code:'data.save')}"
	/>
</g:form>