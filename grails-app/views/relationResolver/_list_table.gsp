<table>
    <thead>
    <tr>
        <g:sortableColumn property="id" title="${message(code:'relationResolver.id')}"/>
        <g:sortableColumn property="name" title="${message(code:'relationResolver.name')}"/>
        <g:sortableColumn property="config" title="${message(code:'relationResolver.config')}"/>
        <g:sortableColumn property="resolverClass" title="${message(code:'relationResolver.resolverClass')}"/>
        <th class="center"><g:message code="relationResolver.options"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${relationResolverList}" status="i" var="relationResolver">
        <tr id="relationResolver_${relationResolver.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <g:render template="row" model="[relationResolver:relationResolver]"/>
        </tr>
    </g:each>
    </tbody>
</table>

<div id="paginateButtons" class="paginateButtons">
    <g:render template="rePaginate"/>
</div>