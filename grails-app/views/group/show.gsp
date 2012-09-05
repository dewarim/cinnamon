<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="group.show.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="group.list"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="group.create"/></g:link></span>
    <span class="menuButton"><g:link action="list" controller="aclEntry" params="[groupId: group?.id]">
        <g:message code="aclEntry.link.show"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="group.show.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <g:if test="${flash.error}">
        <div class="errors">
            <ul class="errors">
                <li class="errors">${flash.error}</li>
            </ul>
        </div>
    </g:if>

    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td class="name"><g:message code="group.id"/></td>

                <td class="value">${fieldValue(bean: group, field: 'id')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:message code="group.description"/></td>

                <td class="value">${fieldValue(bean: group, field: 'description')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:message code="group.name"/></td>

                <td class="value">${fieldValue(bean: group, field: 'name')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:message code="group.parent"/></td>

                <td class="value"><g:link controller="group" action="show"
                                                       id="${group?.parent?.id}">${group?.parent?.name}</g:link></td>

            </tr>

            <g:if test="${!group.groupOfOne}">
                <!-- display a link to the list of acls -->
                <tr class="prop">
                    <td colspan="2" class="name left">
                        <g:link controller='acl' action='showAclsByGroup'
                                id='${group.id}'><g:message code="group.show_acls"/></g:link>
                    </td>

                </tr>
            </g:if>

            <!-- display a link to the list of users -->
            <tr class="prop">
                <td colspan="2" class="name left">
                    <g:link controller='userAccount' action='showUsersByGroup'
                            id='${group.id}'><g:message code="group.show_users"/></g:link>
                </td>

            </tr>

            <g:if test="${hasChildren}">

                <!-- display a link to the list of sub-groups-->
                <tr class="prop">
                    <td colspan="2" class="name left">
                        <g:link controller='group' action='showSubGroups'
                                id='${group.id}'><g:message code="link.to.showSubGroups"/></g:link>
                    </td>

                </tr>
            </g:if>

            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form controller="group" action="edit">
            <input type="hidden" name="id" value="${group?.id}"/>
            <span class="button"><g:submitButton name="edit" class="edit" value="${message(code: 'edit')}"/></span>
            <g:if test="${!group.groupOfOne}">
                <span class="button"><g:actionSubmit class="delete" action="delete" onclick="return confirm('Are you sure?');"
                                                     value="${message(code: 'delete')}"/></span>
            </g:if>
        </g:form>
    </div>
</div>
</body></html>
    