<td>${changeTrigger.id}</td>
<td colspan="6">
	<g:form action="update">
		 <g:if test="${errorMessage}">
			 <p class="error_message">
				 <g:message code="${errorMessage}"/>
			 </p>
		 </g:if>
		<input type="hidden" name="id" value="${changeTrigger?.id}"/>

		<table>

			<g:render template="fields" model="[changeTrigger:changeTrigger]"/>

			<td>

				<g:submitToRemote url="[action:'update', controller:'changeTrigger']"
					before="codeMirrorEditor.toTextArea(\$('#config_${changeTrigger.id}').get(0));"
						update="[success:'changeTrigger_'+changeTrigger.id, failure:'changeTrigger_'+changeTrigger.id]"
						value="${g.message(code:'save')}"/>
			</td>
		</table>
	</g:form>
</td>
<td>
	<g:form>
		<input type="hidden" name="id" value="${changeTrigger.id}">
		<g:submitToRemote url="[action:'cancelEdit', controller:'changeTrigger']"
				update="[success:'changeTrigger_'+changeTrigger.id, failure:'message']"
				value="${g.message(code:'cancel')}"/>
	</g:form>

</td>