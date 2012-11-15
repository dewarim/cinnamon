<%@ page import="cinnamon.index.IndexType" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code: 'id')}"/>

        <g:sortableColumn property="name" title="${message(code: 'indexType.name.label')}"/>

        <g:sortableColumn property="dataType" title="${message(code: 'indexType.dataType.label')}"/>

        <g:sortableColumn property="indexerClass" title="${message(code: 'indexType.indexerClass.label')}"/>

        <g:sortableColumn property="vaProviderClass" title="${message(code: 'indexType.vaProviderClass.label')}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${indexTypeList}" status="i" var="indexTypeInstance">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show"
                        id="${indexTypeInstance.id}">${fieldValue(bean: indexTypeInstance, field: "id")}</g:link></td>

            <td>${fieldValue(bean: indexTypeInstance, field: "name")}</td>

            <td>${fieldValue(bean: indexTypeInstance, field: "dataType")}</td>

            <td>${fieldValue(bean: indexTypeInstance, field: "indexerClass")}</td>

            <td>${fieldValue(bean: indexTypeInstance, field: "vaProviderClass")}</td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
    <util:remotePaginate controller="indexType" action="updateList" total="${IndexType.count()}"
                         update="indexTypeList" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>
</div>