<g:form onsubmit="return false;">
	<input type="hidden" name="osd" value="${osd.id}">
    <input type="hidden" name="fieldName" value="name" />
    <g:textField name="fieldValue" value="${osd.name}" size="30" maxlength="64"/>
	<g:submitToRemote url="[action:'saveField', controller:'osd']"
		update="[success:'objectDetails', failure:'message']"
        id="foo"
		onFailure="showClearButton();"		
		value="${message(code:'data.save')}"
	/>
</g:form>