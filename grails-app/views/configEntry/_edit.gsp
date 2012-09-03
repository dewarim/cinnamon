<td>${configEntry.id}</td>
<td colspan="6">
	<g:form action="update">
		 <g:if test="${errorMessage}">
			 <p class="error_message">
				 <g:message code="${errorMessage}"/>
			 </p>
		 </g:if>
		<input type="hidden" name="id" value="${configEntry?.id}"/>

		<table>

			<g:render template="fields" model="[configEntry:configEntry]"/>

			<td>

				<g:submitToRemote url="[action:'update', controller:'configEntry']"
					before="codeMirrorEditor.toTextArea(\$('#config_${configEntry.id}').get(0));"
						update="[success:'configEntry_'+configEntry.id, failure:'configEntry_'+configEntry.id]"
						value="${g.message(code:'save')}"/>
			</td>
		</table>
	</g:form>
</td>
<td>
	<g:form>
		<input type="hidden" name="id" value="${configEntry.id}">
		<g:submitToRemote url="[action:'cancelEdit', controller:'configEntry']"
				update="[success:'configEntry_'+configEntry.id, failure:'message']"
				value="${g.message(code:'cancel')}"/>
	</g:form>

</td>