<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <title><g:message code="folderType.show.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="folderType.list.link"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message
            code="folderType.create.link"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="folderType.show.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tr class="prop">
                <td valign="top" class="name"><g:message code="folderType.name"/></td>
                <td valign="top" class="value">${fieldValue(bean: folderType, field: 'name')}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="folderType.description"/></td>
                <td valign="top" class="value">${fieldValue(bean: folderType, field: 'description')}</td>
            </tr>

        </table>

    </div>

    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${folderType?.id}"/>
            <span class="button"><g:actionSubmit action="edit" class="edit" value="${message(code: 'edit')}"/></span>
            <span class="button"><g:actionSubmit action="delete" class="delete"
                                                 onclick="return confirm('${message(code: 'folderType.confirm.delete')?.encodeAsHTML()}');"
                                                 value="${message(code: 'delete')}"/></span>
        </g:form>
    </div>
</div>


</body></html>
