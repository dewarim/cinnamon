<td>${lifeCycle.id}</td>

<td>${lifeCycle.name}</td>

<td>${lifeCycle.defaultState?.name}</td>

<td>
	<ul>
		<g:each in="${lifeCycle.fetchStates()}" var="state">
			<li><g:message code="${state.name}"/></li>
		</g:each>
	</ul>
</td>

<td>
	<g:remoteLink action="edit"
			params="[id:lifeCycle.id]"
			method="post"
			update="[success:'lifeCycle_'+lifeCycle.id, failure:'infoMessage']"
			onFailure="\$('#infoMessage').show();"
	>

		<g:message code="lifeCycle.edit"/>
	</g:remoteLink>  &nbsp;|&nbsp;
	<g:remoteLink action="delete"
			params="[id:lifeCycle.id]"
			method="post"
			before="if(! confirm('${message(code:'lifeCycle.confirm.delete')?.encodeAsHTML()}')){return false};"
			update="[success:'lifeCycleTable', failure:'infoMessage']">
		<g:message code="lifeCycle.delete"/>
	</g:remoteLink>
</td>