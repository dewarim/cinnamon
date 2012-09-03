<%@ page import="cinnamon.index.IndexGroup" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code: 'indexGroup.id.label', default: 'Id')}"/>

        <g:sortableColumn property="name" title="${message(code: 'indexGroup.name.label', default: 'Name')}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${indexGroupList}" status="i" var="indexGroupInstance">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show"
                        id="${indexGroupInstance.id}">${fieldValue(bean: indexGroupInstance, field: "id")}</g:link></td>

            <td>${fieldValue(bean: indexGroupInstance, field: "name")}</td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
      <util:remotePaginate controller="indexGroup" action="updateList" total="${IndexGroup.count()}"
                         update="indexGroupList" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>
</div>