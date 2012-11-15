<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="user.list.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" controller="group" action="list"><g:message
            code="group.list"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="usersByGroup.list.label" args="[group.name]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <g:link controller="group" action="show" id="${group?.id}">
        <g:message code="link.to.show.group" args="[group.name]"/>
    </g:link>

    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="id" title="${message(code: 'id')}"/>

                <g:sortableColumn property="description" title="${message(code: 'user.description')}"/>

                <g:sortableColumn property="fullname" title="${message(code: 'user.fullname')}"/>

                <g:sortableColumn property="name" title="${message(code: 'user.name')}"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${userList}" status="i" var="user">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${user.id}">${fieldValue(bean: user, field: 'id')}</g:link></td>

                    <td>${fieldValue(bean: user, field: 'description')}</td>

                    <td>${fieldValue(bean: user, field: 'fullname')}</td>

                    <td>${fieldValue(bean: user, field: 'name')}</td>

                    <td>
                        <g:link controller="group" action="removeUser" id="${user.id}" params="[groupId: group.id]">
                            <g:message code="user.remove_from_group"/></g:link>
                    </td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${userList.size()}"/>
    </div>

    <p>
        <g:message code="users.system_group.warning"/>
    </p>

<!-- Combobox with all users and an add button -->
    <g:if test="${addList?.size() > 0}">
        <div class="buttons">
            <g:form name="add.user.form" action="addUser" controller="group">
                <input type="hidden" name="id" value="${group.id}"/>
                <g:select from="${addList}" name="user_list" optionValue="name" optionKey="id"/>
                <span class="button"><g:submitButton name="submitAddUser" value="${message(code: 'user.add_to_group')}"
                                                     action="addUser"/></span>
            </g:form>

        </div>
    </g:if>

    <g:if test="${hasSubGroups}">
        <div class="link">
            <g:link action="showDescendantGroupUsers" controller="group" params="[id: group.id]">
                <g:message code="user.link.to.showDescendantGroupUsers"/>
            </g:link>
        </div>

        <div class="link">
            <g:link action="showSubGroups" controller="group" params="[id: group.id]">
                <g:message code="link.to.showSubGroups"/>
            </g:link>
        </div>
    </g:if>

</div>

</body></html>
