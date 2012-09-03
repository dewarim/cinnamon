<td>${lifeCycle.id}</td>
<td colspan="6">
	<g:form action="update">
		 <g:if test="${errorMessage}">
			 <p class="error_message">
				 <g:message code="${errorMessage}"/>
			 </p>
		 </g:if>
		<input type="hidden" name="id" value="${lifeCycle?.id}"/>

		<table>

			<g:render template="fields" model="[lifeCycle:lifeCycle, states:states, defaultStates:defaultStates]"/>

			<td>

				<g:submitToRemote url="[action:'update', controller:'lifeCycle']"
						update="[success:'lifeCycle_'+lifeCycle.id, failure:'lifeCycle_'+lifeCycle.id]"
						value="${g.message(code:'save')}"/>
			</td>
		</table>
	</g:form>
</td>
<td>
	<g:form>
		<input type="hidden" name="id" value="${lifeCycle.id}">
		<g:submitToRemote url="[action:'cancelEdit', controller:'lifeCycle']"
				update="[success:'lifeCycle_'+lifeCycle.id, failure:'message']"
				value="${g.message(code:'cancel')}"/>
	</g:form>

</td>