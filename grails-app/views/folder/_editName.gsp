<g:form>
	<input type="hidden" name="folder" value="${folder.id}">
    <input type="hidden" name="fieldName" value="name">
	<g:textField name="fieldValue" value="${folder.name}" size="30" maxlength="64"/>
	<g:submitToRemote url="[action:'saveField', controller:'folder']"
			update="[success:'folderMeta', failure:'message']"
		onFailure="showClearButton();"		
		value="${message(code:'data.save')}"
	/>
</g:form>