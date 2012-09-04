<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
	<title><g:message code="user.delete.title"/></title>
</head>
<body>
<div class="nav">
	<g:homeButton><g:message code="home"/></g:homeButton>
	<span class="menuButton"><g:link class="list" action="list"><g:message code="user.list"/></g:link></span>
	<span class="menuButton"><g:link class="create" action="create"><g:message code="user.create"/></g:link></span>
	<span class="menuButton"><g:link class="replaceUser" action="replaceUser"><g:message code="user.replaceUser.link"/></g:link></span>

</div>
<div class="content">
	<h1><g:message code="user.delete.title"/></h1>
	<div class="dialog" style="width:60ex;">
		<g:render template="/shared/message"/>
		<g:if test="${showTransferLink}">
			<g:link controller="userAccount" action="replaceUser">
				<g:message code="user.delete.to.replace.link"/>
			</g:link>
		</g:if>
		<p>
			<g:message code="user.delete.intro"/>
		</p>
				<g:if test="${forbidden}">
	<p class="error">
		<strong>
			<g:message code="user.delete.forbidden"/>
		</strong>
	</p>
	</g:if>
		<g:form action="doDelete" method="post">

			<table>
				<thead>
				</thead>
				<tbody>
				<tr>
					<td><g:message code="user.delete.select"/></td>
					<td><g:select name='user' from="${userList}" optionKey="id" optionValue="name"/></td>

				</tr>
				<tr>
					<td colspan="2" style="text-align:right;">
						<g:submitButton name="confirmDelete"
								onclick="return confirm('${message(code:'user.delete.confirm')?.encodeAsHTML()}');"
								value="${message(code:'user.delete.submit')}"/>
					</td>
				</tr>
				</tbody>
			</table>
		</g:form>
	</div>
</div>

</body></html>
