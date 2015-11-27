<%@ page import="cinnamon.Acl" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code:'id')}"/>

        <g:sortableColumn property="name" title="${message(code:'acl.name')}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${aclList}" status="i" var="acl">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show" id="${acl.id}">${fieldValue(bean: acl, field: 'id')}</g:link></td>

            <td>${fieldValue(bean: acl, field: 'name')}</td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
    <util:remotePaginate controller="acl" action="updateList" total="${Acl.count()}"
                         update="aclList" max="100" pageSizes="[100, 250, 500, 1000]"/>
</div>