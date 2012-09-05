<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
	<title><g:message code="group.edit.title"/></title>
</head>
<body>
<div class="nav">
	<g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
	<span class="menuButton"><g:link class="list" action="list"><g:message code="group.list"/></g:link></span>
	<span class="menuButton"><g:link class="create" action="create"><g:message code="group.create"/></g:link></span>
</div>
<div class="content">
	<h1><g:message code="group.edit.title"/></h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<g:hasErrors bean="${group}">
		<div class="errors">
			<g:renderErrors bean="${group}" as="list"/>
		</div>
	</g:hasErrors>
	<g:form method="post" controller="group" action="update">
		<input type="hidden" name="id" value="${group?.id}"/>
		<div class="dialog">
			<table>
				<tbody>

				<tr class="prop">
					<td class="name">
						<label for="groupId"><g:message code="group.id"/></label>
					</td>
					<td class="value ${hasErrors(bean: group, field: 'id', 'errors')}">
						<input type="text" disabled="disabled" name="id" id="groupId" value="${fieldValue(bean: group, field: 'id')}"/>
					</td>
				</tr>

				<tr class="prop">
					<td class="name">
						<label for="description"><g:message code="group.description"/></label>
					</td>
					<td class="value ${hasErrors(bean: group, field: 'description', 'errors')}">
						<!-- <input type="text" name="description" id="description" value="${fieldValue(bean: group, field: 'description')}" /> -->
						<g:descriptionTextArea name="description" value="${fieldValue(bean:group,field:'description')}"/>
					</td>
				</tr>

				<tr class="prop">
					<td class="name">
						<label for="name"><g:message code="group.name"/></label>
					</td>
					<td class="value ${hasErrors(bean: group, field: 'name', 'errors')}">
						<input type="text" name="name" id="name" value="${fieldValue(bean: group, field: 'name')}"/>
					</td>
				</tr>

				<tr class="prop">
					<td class="name">
						<label for="parent"><g:message code="group.parent"/></label>
					</td>
					<td class="value ${hasErrors(bean: group, field: 'parent', 'errors')}">
						<g:select id="parent" optionKey="id"
								from="${parentList}"
								name="parent.id"
								optionValue="name"
								noSelection="${['null':'No parent']}"
								value="${group?.parent?.id}"/>
					</td>
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

				</tbody>
			</table>
		</div>
		<div class="buttons">
			<span class="button"><g:submitButton name="save" class="save" value="${message(code:'update')}"/></span>
		</div>
	</g:form>
</div>

</body></html>
