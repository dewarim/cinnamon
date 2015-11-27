<%@ page import="cinnamon.UserAccount;" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code:'id')}"/>
        <g:sortableColumn property="name" title="${message(code:'user.name')}"/>
        <g:sortableColumn property="fullname" title="${message(code:'user.fullname')}"/>
        <g:sortableColumn property="description" title="${message(code:'user.description')}"/>
        <g:sortableColumn property="activated" title="${message(code:'user.activated')}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${userList}" status="i" var="user">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show" id="${user.id}">${fieldValue(bean: user, field: 'id')}</g:link></td>
            <td>${fieldValue(bean: user, field: 'name')}</td>
            <td>${fieldValue(bean: user, field: 'fullname')}</td>
            <td>${fieldValue(bean: user, field: 'description')}</td>
            <td><g:checkBox name="activated" disabled="true" value="${user.activated}"/></td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
    <util:remotePaginate controller="userAccount" action="updateList" total="${UserAccount.count()}"
                         update="userList" max="100" pageSizes="[100, 250, 500, 1000]"/>
</div>