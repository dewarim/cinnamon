<td>${changeTriggerType.id}</td>
<td colspan="6">
	<g:form action="update">
		 <g:if test="${errorMessage}">
			 <p class="error_message">
				 <g:message code="${errorMessage}"/>
			 </p>
		 </g:if>
		<input type="hidden" name="id" value="${changeTriggerType?.id}"/>

		<table>

			<g:render template="fields" model="[changeTriggerType:changeTriggerType, triggers:triggers]"/>

			<td>

				<g:submitToRemote url="[action:'update', controller:'changeTriggerType']"
						update="[success:'changeTriggerType_'+changeTriggerType.id, failure:'changeTriggerType_'+changeTriggerType.id]"
						value="${g.message(code:'save')}"/>
			</td>
		</table>
	</g:form>
</td>
<td>
	<g:form>
		<input type="hidden" name="id" value="${changeTriggerType.id}">
		<g:submitToRemote url="[action:'cancelEdit', controller:'changeTriggerType']"
				update="[success:'changeTriggerType_'+changeTriggerType.id, failure:'message']"
				value="${g.message(code:'cancel')}"/>
	</g:form>

</td>