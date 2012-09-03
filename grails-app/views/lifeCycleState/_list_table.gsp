<table>
    <thead>
    <tr>
        <g:sortableColumn property="id" title="${message(code:'lcs.id')}"/>
        <g:sortableColumn property="name" title="${message(code:'lcs.name')}"/>
        <g:sortableColumn property="stateClass" title="${message(code:'lcs.stateClass')}"/>
        <g:sortableColumn property="lifeCycle" title="${message(code:'lcs.lifeCycle')}"/>
        <g:sortableColumn property="lifeCycleStateForCopy" title="${message(code:'lcs.lifeCycleStateForCopy')}"/>
        <g:sortableColumn property="config" title="${message(code:'lcs.config')}"/>
        <th class="center"><g:message code="lifeCycleState.options"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${lcsList}" status="i" var="lcs">
        <tr id="lcs_${lcs.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <g:render template="row" model="[lcs:lcs]"/>
        </tr>
    </g:each>
    </tbody>
</table>

<div id="paginateButtons" class="paginateButtons">
    <g:render template="rePaginate"/>
</div>
