<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="format.show.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="format.list"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="format.create"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="format.show.title"/></h1>
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
                <td valign="top" class="name"><g:message code="format.id"/></td>

                <td valign="top" class="value">${fieldValue(bean: format, field: 'id')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="format.name"/></td>

                <td valign="top" class="value">${fieldValue(bean: format, field: 'name')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="format.contenttype"/></td>

                <td valign="top" class="value">${fieldValue(bean: format, field: 'contenttype')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="format.description"/></td>

                <td valign="top" class="value">${fieldValue(bean: format, field: 'description')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="format.extension"/></td>

                <td valign="top" class="value">${fieldValue(bean: format, field: 'extension')}</td>

            </tr>

            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form controller="format" action="edit">
            <input type="hidden" name="id" value="${format?.id}"/>
            <span class="button"><g:submitButton name="edit" class="edit"  value="${message(code: 'edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" action="delete"
                                                 onclick="return confirm('Are you sure?');"
                                                 value="${message(code: 'delete')}"/></span>
        </g:form>
    </div>
</div>


</body></html>
