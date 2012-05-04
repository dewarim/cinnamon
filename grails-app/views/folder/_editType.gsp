<%@ page import="cinnamon.FolderType" %>
<g:form>
	<input type="hidden" name="folder" value="${folder.id}">
    <input type="hidden" name="fieldName" value="type">
	<g:select from="${FolderType.list()}" name="fieldValue" value="${folder.type.id}"
			optionKey="id" optionValue="name"/>

	<g:submitToRemote url="[action:'saveField', controller:'folder']"
			update="[success:'folderMeta', failure:'message']"
		onFailure="showClearButton();"		
		value="${message(code:'data.save')}"
	/>
</g:form>