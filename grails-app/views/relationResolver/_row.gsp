<td>${relationResolver.id}</td>
<td>${relationResolver.name}</td>
<td>
	<g:render template="/shared/renderXML"
			  model="[renderId:relationResolver.id, xml:relationResolver.config]"/>
</td>
<td>${relationResolver.resolverClass.name}</td>
<td>
	<g:remoteLink action="edit"
				  params="[id:relationResolver.id]"
				  method="post"
				  update="[success:'relationResolver_'+relationResolver.id, failure:'message']">
		<g:message code="relationResolver.edit"/>
	</g:remoteLink>  &nbsp;|&nbsp;
	<g:remoteLink action="delete"
				  params="[id:relationResolver.id]"
				  method="post"
				  before="if(! confirm('${message(code:'relationResolver.confirm.delete')?.encodeAsHTML()}')){return false};"
				  onSuccess="rePaginate('paginateButtons');"
				  update="[success:'relationResolverTable', failure:'infoMessage']">
		<g:message code="relationResolver.delete"/>
	</g:remoteLink>
</td>