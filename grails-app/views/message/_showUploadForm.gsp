<div class="upload_form" style="padding-top: 2ex;padding-bottom: 2ex;">
	<g:uploadForm name="importMessages" action="importMessages" controller="message">
		<label for="importFile"><g:message code="message.import.file"/></label>
		<input id="importFile" type="file" name="messages"/>
		<g:submitButton name="doImport" value="${message(code:'message.import')}"/>
	</g:uploadForm>
</div>
