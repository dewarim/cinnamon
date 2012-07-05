<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
	<title><g:message code="user.show.title"/></title>
</head>
<body>
<div class="nav">
	<g:homeButton><g:message code="home"/></g:homeButton>
	<span class="menuButton"><g:link class="list" action="list"><g:message code="user.list"/></g:link></span>
	<span class="menuButton"><g:link class="create" action="create"><g:message code="user.create"/></g:link></span>
	<span class="menuButton"><g:link class="replaceUser" action="replaceUser"><g:message code="user.replaceUser.link"/></g:link></span>
	<span class="menuButton"><g:link class="delete" action="deleteAsk"><g:message code="user.delete.link"/></g:link></span>
</div>
<div class="content">
	<h1><g:message code="user.show.title"/></h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div class="dialog">
		<table>
			<tbody>

			<tr class="prop">
				<td valign="top" class="name"><g:message code="user.id"/></td>

				<td valign="top" class="value">${fieldValue(bean: user, field: 'id')}</td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name"><g:message code="user.name"/></td>

				<td valign="top" class="value">${fieldValue(bean: user, field: 'name')}</td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name"><g:message code="user.fullname"/></td>

				<td valign="top" class="value">${fieldValue(bean: user, field: 'fullname')}</td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name"><g:message code="user.description"/></td>

				<td valign="top" class="value">${fieldValue(bean: user, field: 'description')}</td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name"><g:message code="user.language"/></td>

				<td valign="top" class="value">${fieldValue(bean: user, field: 'language.isoCode')}</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><g:message code="user.email"/></td>
				<td valign="top" class="value">${fieldValue(bean: user, field: 'email')}</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><g:message code="user.sudoer"/></td>
				<td valign="top" class="value"><g:checkBox name="sudoer" disabled="true" value="${user.sudoer}"/></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><g:message code="user.sudoable"/></td>
				<td valign="top" class="value"><g:checkBox name="sudoable" disabled="true" value="${user.sudoable}"/></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><g:message code="user.activated"/></td>
				<td valign="top" class="value"><g:checkBox name="activated" disabled="true" value="${user.activated}"/></td>
			</tr>

			<!-- link to the list of group -->
			<tr class="prop">
				<td colspan="2" align="left" class="name">
					<g:link controller='group' action='showGroupsByUser'
							id='${user.id}'><g:message code="user.show_groups"/></g:link>
				</td>

			</tr>
			</tbody>
		</table>
	</div>
	<div class="buttons">
		<g:form>
			<input type="hidden" name="id" value="${user?.id}"/>
			<span class="button"><g:actionSubmit action="edit" class="edit" value="${message(code:'edit')}"/></span>

		</g:form>
	</div>
</div>

</body></html>
