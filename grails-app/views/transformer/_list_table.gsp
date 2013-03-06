<table>
    <thead>
    <tr>
        <g:sortableColumn property="id" title="${message(code:'id')}"/>
        <g:sortableColumn property="name" title="${message(code:'transformer.name')}"/>
        <th><g:message code="transformer.transformerClass"/></th>
        <th><g:message code="transformer.sourceFormat"/></th>
        <th><g:message code="transformer.targetFormat"/></th>
        <th class="center"><g:message code="transformer.options"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${transformerList}" status="i" var="transformer">
        <tr id="transformer_${transformer.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <g:render template="row" model="[transformer:transformer]"/>
        </tr>
    </g:each>
    </tbody>
</table>

<div id="paginateButtons" class="paginateButtons">
    <g:render template="rePaginate"/>
</div>