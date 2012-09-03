<td>${lcs.id}</td>
<td>${lcs.name}</td>
<td>${lcs.stateClass.name}</td>
<td>${lcs.lifeCycle?.name}</td>
<td>${lcs.lifeCycleStateForCopy?.name}</td>
<td>
	<g:render template="/shared/renderXML" model="[renderId:lcs?.id, xml:lcs?.config]"/>
</td>

<td>
	<g:remoteLink action="edit"
			params="[id:lcs.id]"
			method="post"
		onLoading="\$('#infoMessage').hide();"
			update="[success:'lcs_'+lcs.id, failure:'message']">
		<g:message code="lcs.edit"/>
	</g:remoteLink>  &nbsp;|&nbsp;
	<g:remoteLink action="delete"
			params="[id:lcs.id]"
			method="post"
			before="if(! confirm('${message(code:'lcs.confirm.delete')?.encodeAsHTML()}')){return false};rePaginate('paginateButtons');"
			update="[success:'lcsTable', failure:'infoMessage']">
		<g:message code="lcs.delete"/>
	</g:remoteLink>
</td>