<table>
    <thead>
    <tr>
        <g:sortableColumn action="index" property="id" title="${message(code:'changeTrigger.id')}"/>
        <g:sortableColumn action="index" property="controller" title="${message(code:'changeTrigger.controller')}"/>
        <g:sortableColumn action="index" property="action" title="${message(code:'changeTrigger.action')}"/>
        <g:sortableColumn action="index" property="triggerType" title="${message(code:'changeTrigger.triggerType')}"/>
        <g:sortableColumn action="index" property="preTrigger" title="${message(code:'changeTrigger.preTrigger')}"/>
        <g:sortableColumn action="index" property="postTrigger" title="${message(code:'changeTrigger.postTrigger')}"/>
        <g:sortableColumn action="index" property="ranking" title="${message(code:'changeTrigger.ranking')}"/>
        <g:sortableColumn action="index" property="active" title="${message(code:'changeTrigger.active')}"/>
        <th class="center"><g:message code="changeTrigger.config"/></th>
        <th class="center"><g:message code="changeTrigger.options"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${changeTriggerList}" status="i" var="changeTrigger">
        <tr id="changeTrigger_${changeTrigger.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <g:render template="row" model="[changeTrigger:changeTrigger]"/>
        </tr>
    </g:each>
    </tbody>
</table>

<div id="paginateButtons" class="paginateButtons">
    <g:render template="rePaginate"/>
</div>
