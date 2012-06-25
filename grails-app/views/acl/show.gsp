<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
	<g:render template="/shared/header"/>
	<title><g:message code="acl.show.title"/></title>
</head>
<body>
<div class="nav">
	<g:homeButton><g:message code="home"/></g:homeButton>
	<span class="menuButton"><g:link class="list" action="list"><g:message code="acl.list"/></g:link></span>
	<span class="menuButton"><g:link class="create" action="create"><g:message code="acl.create"/></g:link></span>
	<span class="menuButton"><g:link action="list" controller="aclEntry" params="[aclId:acl?.id]">
		<g:message code="aclEntry.link.show"/></g:link></span>
</div>
<div class="body">
	<h1><g:message code="acl.show.title"/></h1>
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
				<td valign="top" class="name"><g:message code="acl.id"/></td>

				<td valign="top" class="value">${fieldValue(bean: acl, field: 'id')}</td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name"><g:message code="acl.name"/></td>

				<td valign="top" class="value">${fieldValue(bean: acl, field: 'name')}</td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name"><g:message code="acl.description"/></td>

				<td valign="top" class="value">${fieldValue(bean: acl, field: 'description')}</td>

			</tr>

			<tr class="prop">
				<td colspan="2">
					<g:render template="gotoAclEntries" model="[acl:acl]"/>
				</td>
			</tr>
            			<tr>
				<td>
					<span class="button"><g:link action="edit" controller="acl" params="[id:acl.id]"><g:message code="edit"/></g:link></span>
				</td>
				<td>

					<span class="button"><g:link action="delete" controller="acl" params="[id:acl.id]" onclick="return confirm('Are you sure?');"><g:message code="delete"/></g:link></span>

				</td>
			</tr>
			</tbody>
		</table>
	</div>

</div>

</body></html>