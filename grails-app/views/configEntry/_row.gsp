<td>${configEntry.id}</td>

<td>${configEntry.name}</td>
<td>
		<g:render template="/shared/renderXML"
							  model="[renderId:configEntry.id, xml:configEntry.config]"/>
</td>
<td>
	<g:remoteLink action="edit"
			params="[id:configEntry.id]"
			method="post"
			update="[success:'configEntry_'+configEntry.id, failure:'message']">
		<g:message code="configEntry.edit"/>
	</g:remoteLink>  &nbsp;|&nbsp;
	<g:remoteLink action="delete"
			params="[id:configEntry.id]"
			method="post"
			before="if(! confirm('${message(code:'configEntry.confirm.delete')?.encodeAsHTML()}')){return false};"
			onSuccess="rePaginate('paginateButtons');"
			update="[success:'configEntryList', failure:'message']">
		<g:message code="configEntry.delete"/>
	</g:remoteLink>
</td>