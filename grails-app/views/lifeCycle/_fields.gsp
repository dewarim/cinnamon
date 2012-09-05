<g:set var="cid" value="${lifeCycle?.id}"/>
<td class="value ${hasErrors(bean: lifeCycle, field: 'name', 'errors')}">
	<label for="name_${cid}"><g:message code="lifeCycle.name"/></label> <br>
	<input type="text" name="name" id="name_${cid}" value="${lifeCycle?.name}"/>
</td>
<td class="value ${hasErrors(bean: lifeCycle, field: 'defaultState', 'errors')}">
	<label for="defaultState_${cid}"><g:message code="lifeCycle.defaultState"/></label> <br>
	<g:if test="${ defaultStates.isEmpty()}">
		<g:message code="lifeCycle.defaultStates.none"/>
	</g:if>
	<g:else>
		<g:select from="${defaultStates}"
				name="defaultState" id="defaultState_${cid}"
				optionKey="id" optionValue="name" value="${lifeCycle?.defaultState?.id}"
				noSelection="['':'---']"
		/>
	</g:else>
</td>
<td class="value ${hasErrors(bean: lifeCycle, field: 'defaultState', 'errors')}">
	<label for="states_${cid}"><g:message code="lifeCycle.states"/></label> <br>
	<g:if test="${ states.isEmpty()}">
		<g:message code="lifeCycle.states.none.open"/>
	</g:if>
	<g:else>
		<g:select from="${states}" name="states"
				id="states_${cid}"
				optionKey="id" multiple="true" optionValue="name"
				value="${lifeCycle?.fetchStates()*.id}"/>
	</g:else>
</td>