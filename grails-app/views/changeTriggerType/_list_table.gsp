<table>
    <thead>
    <tr>
        <g:sortableColumn property="id" title="${message(code:'id')}"/>
        <g:sortableColumn property="name" title="${message(code:'changeTriggerType.name')}"/>
        <g:sortableColumn property="triggerClass" title="${message(code:'changeTriggerType.triggerClass')}"/>
        <th class="center"><g:message code="changeTriggerType.options"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${changeTriggerTypeList}" status="i" var="changeTriggerType">
        <tr id="changeTriggerType_${changeTriggerType.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <g:render template="row" model="[changeTriggerType:changeTriggerType]"/>
        </tr>
    </g:each>
    </tbody>
</table>

<div id="paginateButtons" class="paginateButtons">
    <g:render template="rePaginate"/>
</div>