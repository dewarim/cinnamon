<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <title><g:message code="user.list.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton"><g:link class="list" controller="group" action="list"><g:message
            code="group.list"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="group.create"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="usersByGroup.descendant.list" args="${[ancestorGroup.name]}"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="id" title="${message(code: 'user.id')}"/>

                <g:sortableColumn property="description" title="${message(code: 'user.description')}"/>

                <g:sortableColumn property="fullname" title="${message(code: 'user.fullname')}"/>

                <g:sortableColumn property="name" title="${message(code: 'user.name')}"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${users}" status="i" var="user">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${user.id}">${fieldValue(bean: user, field: 'id')}</g:link></td>

                    <td>${fieldValue(bean: user, field: 'description')}</td>

                    <td>${fieldValue(bean: user, field: 'fullname')}</td>

                    <td>${fieldValue(bean: user, field: 'name')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${users.size()}"/>
    </div>

    <div class="link">
        <g:link action="showUsersByGroup" controller="user" params="[id: ancestorGroup.id]">
            <g:message code="link.to.showUsersByGroup" args="[ancestorGroup.name]"/>
        </g:link>
    </div>

</div>
</body></html>
