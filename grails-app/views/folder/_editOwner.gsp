<%@ page import="cinnamon.UserAccount" %>
<g:form>
	<input type="hidden" name="folder" value="${folder.id}">
    <input type="hidden" name="fieldName" value="ownerid">
	<g:select from="${UserAccount.list()}" name="fieldValue" value="${folder.owner.id}"
			optionKey="id" optionValue="name"/>

	<g:submitToRemote url="[action:'saveField', controller:'folder']"
			update="[success:'folderMeta', failure:'message']"
		onFailure="showClearButton();"		
		value="${message(code:'data.save')}"
	/>
</g:form>