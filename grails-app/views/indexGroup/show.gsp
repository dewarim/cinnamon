<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <g:set var="entityName" value="${message(code: 'indexGroup.label', default: 'IndexGroup')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="indexGroup.list.link"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message
            code="indexGroup.create.link"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="default.show.label" args="[entityName]"/></h1>
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
                <td valign="top" class="name"><g:message code="label.id"/></td>

                <td valign="top" class="value">${fieldValue(bean: indexGroupInstance, field: "id")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="label.name"/></td>

                <td valign="top" class="value">${fieldValue(bean: indexGroupInstance, field: "name")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="indexGroup.items.label" default="Items"/></td>

                <td valign="top" style="text-align: left;" class="value">
                    <ul>
                        <g:each in="${indexGroupInstance.items}" var="i">
                            <li><g:link controller="indexItem" action="show" id="${i.id}">${i.name}</g:link></li>
                        </g:each>
                    </ul>
                </td>

            </tr>

            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form>
            <g:hiddenField name="id" value="${indexGroupInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="edit"
                                                 value="${message(code: 'default.button.edit.label', default: 'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" action="delete"
                                                 value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                                 onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
