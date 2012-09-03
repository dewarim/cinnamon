<td>${lcs.id}</td>
<td colspan="5">
	<g:form action="update">
		 <g:if test="${errorMessage}">
			 <p class="error_message">
				 <g:message code="${errorMessage}"/>
			 </p>
		 </g:if>
		<input type="hidden" name="id" value="${lcs?.id}"/>

		<table>

			<g:render template="fields" model="[lcs:lcs, copyStates:copyStates, stateClasses:stateClasses]"/>

			<td>

				<g:submitToRemote url="[action:'update', controller:'lifeCycleState	']"
					before="codeMirrorEditor.toTextArea(\$('#config_${lcs.id}').get(0));"
					after="createEditor(\$('#config_${lcs?.id}').get(0));"
						update="[success:'lcs_'+lcs.id, failure:'lcs_'+lcs.id]"
						value="${g.message(code:'save')}"/>
			</td>
		</table>
	</g:form>
</td>
<td>
	<g:form>
		<input type="hidden" name="id" value="${lcs.id}">
		<g:submitToRemote url="[action:'cancelEdit', controller:'lifeCycleState']"
				update="[success:'lcs_'+lcs.id, failure:'message']"
				value="${g.message(code:'cancel')}"/>
	</g:form>

</td>