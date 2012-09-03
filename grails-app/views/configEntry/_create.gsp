<g:form action="save">
	<br>
	<g:if test="${errorMessage}">
		<p class="error_message">
			<g:message code="${errorMessage}"/>
		</p>
	</g:if>
	<table>

		<g:render template="fields" model="[configEntry:configEntry]"/>
        <script type="text/javascript">
            $('#name_${configEntry?.id}').focus();
        </script>
		<td>
			<g:submitToRemote url="[action:'save', controller:'configEntry']"
					update="[success:'configEntryList', failure:'createConfigEntry']"
					value="${g.message(code:'save')}"
					before="codeMirrorEditor.toTextArea(\$('#config_${configEntry?.id}').get(0));"
					onSuccess="\$('#ajaxMessage').text('${g.message(code:'create.success')}');\$('#createConfigEntry').text('');rePaginate('paginateButtons');"
					onSubmit="\$('#ajaxMessage').text('');\$('#errorMessage').text('');"/>
		</td>
	</table>
</g:form>