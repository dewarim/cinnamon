<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="objectType.show.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="objectType.list"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message
            code="objectType.create"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="objectType.show.title"/></h1>
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
                <td valign="top" class="name"><g:message code="objectType.id"/></td>

                <td valign="top" class="value">${fieldValue(bean: objectType, field: 'id')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="objectType.name"/></td>

                <td valign="top" class="value">${fieldValue(bean: objectType, field: 'name')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="objectType.description"/></td>

                <td valign="top" class="value">${fieldValue(bean: objectType, field: 'description')}</td>

            </tr>

            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${objectType?.id}"/>
            <span class="button"><g:actionSubmit class="edit" value="${message(code: 'edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');"
                                                 value="${message(code: 'delete')}"/></span>
        </g:form>
    </div>
</div>

</body></html>
