<!DOCTYPE HTML>
<html>
<head>
	<g:render template="/shared/header"/>

        <title><g:message code="folderType.show.title"/></title>
    </head>
    <body>
        <g:render template="/shared/logo"/>
	<div class="nav">
			<g:homeButton><g:message code="home"/></g:homeButton>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="folderType.list.link"/></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="folderType.create.link"/></g:link></span>
        </div>
        <div class="body">
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
                    <input type="hidden" name="id" value="${folderType?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="${message(code:'edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${message(code:'folderType.confirm.delete')?.encodeAsHTML()}');" value="${message(code:'delete')}" /></span>
                </g:form>
            </div>
        </div>

<g:render template="/shared/footer"/>
</body></html>
