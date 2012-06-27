<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:render template="/shared/header"/>

    <title><g:message code="aclEntry.list.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <g:if test="${showListAll}">
        <span class="menuButton"><g:link class="list" action="list"><g:message code="aclEntry.list"/></g:link></span>
    </g:if>
    <span class="menuButton"></span>
</div>

<div class="content">
    <h1><g:message code="aclEntry.list.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <table>
        <thead>
        <tr>

            <g:sortableColumn property="id" title="${message(code: 'aclEntry.id')}"/>
            <g:sortableColumn property="acl.name" title="${message(code: 'aclEntry.acl')}"/>
            <g:sortableColumn property="group.name" title="${message(code: 'aclEntry.group')}"/>

        </tr>
        </thead>
        <tbody>
        <g:each in="${aclEntries}" status="i" var="aclEntry">
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                <td><g:link action="show" id="${aclEntry.id}">${fieldValue(bean: aclEntry, field: 'id')}</g:link></td>
                <td>${fieldValue(bean: aclEntry, field: 'acl.name')}</td>
                <td>${fieldValue(bean: aclEntry, field: 'group.name')}</td>

            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="paginateButtons">
        <g:paginate total="${entryCount}"/>
    </div>
</div>

</body></html>
