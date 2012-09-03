<a style="float:right;" href="#" onclick="$('#import_export').html('');return false;"><g:message code="html.hide.area"/></a>
<h2><g:message code="messages.import.results"/></h2>
<table class="import_results" border="1" cellpadding="8">
	<tr>
		<td><g:message code="messages.import.new"/></td>
		<td>${newMessages}</td>
	</tr>
	<tr>
		<td><g:message code="messages.import.deleted"/></td>
		<td>${deletedMessages}</td>
	</tr>
	<tr>
		<td><g:message code="messages.import.unchanged"/></td>
		<td>${unchangedMessages}</td>
	</tr>
	<tr>
		<td><g:message code="messages.import.updated"/></td>
		<td>${updatedMessages}</td>
	</tr>
</table>