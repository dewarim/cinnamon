<g:set var="cid" value="${changeTriggerType?.id}"/>
<td class="value ${hasErrors(bean: changeTriggerType, field: 'name', 'errors')}">
	<label for="name_${cid}"><g:message code="changeTriggerType.name"/></label> <br>
	<input type="text" name="name" id="name_${cid}" value="${changeTriggerType?.name}"/>
</td>
<td colspan="2" class="value ${hasErrors(bean: changeTriggerType, field: 'triggerClass', 'errors')} ">
	<label for="triggerClass_${cid}"><g:message code="changeTriggerType.triggerClass"/></label> <br>
    <g:select id="triggerClass_${cid}" name="triggerClass" from="${triggers}" value="${changeTriggerType?.triggerClass?.name}" />
</td>
