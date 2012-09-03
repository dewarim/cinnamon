<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="group.list"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="group.list"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="group.create"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="group.subgroup.list.title" args="[group.name]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="id" title="${message(code: 'group.id')}"/>

                <g:sortableColumn property="description" title="${message(code: 'group.description')}"/>

                <g:sortableColumn property="name" title="${message(code: 'group.name')}"/>

                <th>${message(code: 'group.parent')}</th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${groupList}" status="i" var="group">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${group.id}">${fieldValue(bean: group, field: 'id')}</g:link></td>

                    <td>${fieldValue(bean: group, field: 'description')}</td>

                    <td>${fieldValue(bean: group, field: 'name')}</td>

                    <td>${group?.parent?.name ?: "-"}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="link">
        <g:link action="show" controller="group" params="[id: group.id]">
            <g:message code="link.to.show.group" args="[group.name]"/>
        </g:link>
    </div>
</div>

</body></html>
