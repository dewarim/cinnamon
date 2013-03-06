<td>${changeTriggerType.id}</td>

<td>${changeTriggerType.name}</td>

<td>${changeTriggerType.triggerClass.name}</td>

<td>
	<g:remoteLink action="edit"
			params="[id:changeTriggerType.id]"
			method="post"
			update="[success:'changeTriggerType_'+changeTriggerType.id, failure:'infoMessage']">
		<g:message code="changeTriggerType.edit"/>
	</g:remoteLink>  &nbsp;|&nbsp;
	<g:remoteLink action="delete"
			params="[id:changeTriggerType.id]"
			method="post"
			before="if(! confirm('${message(code:'changeTriggerType.confirm.delete')?.encodeAsHTML()}')){return false}"
			onSuccess="rePaginate('paginateButtons');"
			update="[success:'changeTriggerTypeList', failure:'infoMessage']">
		<g:message code="changeTriggerType.delete"/>
	</g:remoteLink>
</td>