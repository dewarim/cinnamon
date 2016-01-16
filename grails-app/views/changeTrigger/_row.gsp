<td>${changeTrigger.id}</td>

<td>${changeTrigger.controller}</td>
<td>${changeTrigger.action}</td>

<td><g:message code="${changeTrigger.triggerType.name}"/></td>

<td class="center">
    <g:enabledDisabledIcon test="${changeTrigger.preTrigger}"/>
</td>
<td class="center">
    <g:enabledDisabledIcon test="${changeTrigger.postTrigger}"/>
</td>

<td>${changeTrigger.ranking}</td>
<td class="center">
    <g:enabledDisabledIcon test="${changeTrigger.active}"/>
</td>
<td>
    <g:render template="/shared/renderXML"
              model="[renderId: changeTrigger.id, xml: changeTrigger.config]"/>
</td>
<td>
    <g:remoteLink action="edit"
                  params="[id: changeTrigger.id]"
                  method="post"
                  update="[success: 'changeTrigger_' + changeTrigger.id, failure: 'message']">
        <g:message code="changeTrigger.edit"/>
    </g:remoteLink>  &nbsp;|&nbsp;
    <g:remoteLink action="delete"
                  params="[id: changeTrigger.id]"
                  method="post"
                  before="if(! confirm('${message(code: 'changeTrigger.confirm.delete')?.encodeAsHTML()}')){return false};"
                  onSuccess="rePaginate('paginateButtons');"
                  update="[success: 'changeTriggerList', failure: 'message']">
        <g:message code="changeTrigger.delete"/>
    </g:remoteLink>
</td>