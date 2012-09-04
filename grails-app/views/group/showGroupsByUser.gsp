<%@ page import="cinnamon.global.Constants" %>
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
    <h1><g:message code="group.list.title"/></h1>
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

                    <td>
                        <g:if test="${group.name.startsWith('_') && group.name != Constants.GROUP_SUPERUSERS}">
                            ---
                        </g:if>
                        <g:else>
                            <g:link controller="userAccount" action="removeGroup" id="${group.id}"
                                    params="[userId: user.id]"><g:message code="group.remove_from_user"/></g:link>
                        </g:else>
                    </td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${groupList.size()}"/>
    </div>

<!-- Combobox with all groups and an add button -->
    <g:if test="${addList?.size() > 0}">
        <div class="buttons">
            <g:form name="add.group.form" action="addGroup" controller="userAccount">
                <input type="hidden" name="userId" value="${user.id}"/>
                <g:select from="${addList}" name="group_list" optionValue="name" optionKey="id"></g:select>

                <span class="button"><g:actionSubmit value="${message(code: 'group.add_to_user')}"
                                                     action="addGroup"/></span>
            </g:form>
        </div>
    </g:if>

</body></html>

