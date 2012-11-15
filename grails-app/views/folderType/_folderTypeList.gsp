<%@ page import="cinnamon.FolderType" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code:'id')}"/>

        <g:sortableColumn property="name" title="${message(code:'folderType.name')}"/>

        <g:sortableColumn property="description" title="${message(code:'folderType.description')}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${folderTypeList}" status="i" var="folderType">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show" id="${folderType.id}">${fieldValue(bean: folderType, field: 'id')}</g:link></td>

            <td>${fieldValue(bean: folderType, field: 'name')}</td>

            <td>${fieldValue(bean: folderType, field: 'description')}</td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
     <util:remotePaginate controller="folderType" action="updateList" total="${FolderType.count()}"
                         update="folderTypeList" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>
</div>