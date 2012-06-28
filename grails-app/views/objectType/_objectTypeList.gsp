<%@ page import="cinnamon.ObjectType" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code: 'objectType.id')}"/>

        <g:sortableColumn property="name" title="${message(code: 'objectType.name')}"/>

        <g:sortableColumn property="description" title="${message(code: 'objectType.description')}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${objectTypeList}" status="i" var="objectTypeInstance">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show"
                        id="${objectTypeInstance.id}">${fieldValue(bean: objectTypeInstance, field: 'id')}</g:link></td>

            <td>${fieldValue(bean: objectTypeInstance, field: 'name')}</td>

            <td>${fieldValue(bean: objectTypeInstance, field: 'description')}</td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
    <util:remotePaginate controller="objectType" action="updateList" total="${ObjectType.count()}"
                         update="objectTypeList" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>
</div>