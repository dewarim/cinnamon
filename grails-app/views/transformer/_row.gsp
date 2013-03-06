<td>${transformer.id}</td>
<td>${transformer.name}</td>
<td>${transformer.transformerClass?.name}</td>
<td>${transformer.sourceFormat?.name}</td>
<td>${transformer.targetFormat?.name}</td>

<td>
	<g:remoteLink action="edit"
		controller="transformer"
			params="[id:transformer.id]"
			method="post"
		onLoading="\$('#infoMessage').hide();"
			update="[success:'transformer_'+transformer.id, failure:'message']">
		<g:message code="transformer.edit"/>
	</g:remoteLink>  &nbsp;|&nbsp;
	<g:remoteLink action="delete"
		controller="transformer"
			params="[id:transformer.id]"
			method="post"
			before="if(! confirm('${message(code:'transformer.confirm.delete')?.encodeAsHTML()}')){return false};"
			onSuccess="rePaginate('paginateButtons');"
			update="[success:'transformerTable', failure:'infoMessage']">
		<g:message code="transformer.delete"/>
	</g:remoteLink>
</td>