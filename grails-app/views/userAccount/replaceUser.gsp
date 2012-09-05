<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
	<title><g:message code="user.replaceUser.title"/></title>
</head>
<body>
<div class="nav">
	<g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
	<span class="menuButton"><g:link class="list" action="list"><g:message code="user.list"/></g:link></span>
	<span class="menuButton"><g:link class="create" action="create"><g:message code="user.create"/></g:link></span>
	<span class="menuButton"><g:link class="delete" action="deleteAsk"><g:message code="user.delete.link"/></g:link></span>

</div>
<div class="content">
	<h1><g:message code="user.replaceUser.title"/></h1>
	<div class="dialog" style="width:60ex;">
		<g:render template="/shared/message"/>
		<p>
			<g:message code="user.replaceUser.intro"/>
		</p>
		<g:if test="${forbidden}">
			<p class="error">
				<strong>
					<g:message code="user.replaceUser.forbidden"/>
				</strong>
			</p>
		</g:if>

		<g:form action="transferAssets">

			<table>
				<thead>
				</thead>
				<tbody>
				<tr>
					<td><g:message code="user.delete.select"/></td>
					<td><g:select name='sourceId' from="${userList}" optionKey="id" optionValue="name"/></td>

				</tr>
				<tr>
					<td><g:message code="user.replaceUser.target"/></td>
					<td><g:select name='targetId' from="${userList}" optionKey="id" optionValue="name"/></td>
					<td></td>
				</tr>
				<tr><td colspan="2" style="text-align:right;">
					<g:submitButton name="submitReplacement" value="${message(code:'user.replaceUser.submit')}"/>

				</td>
				</tr>
				</tbody>
			</table>
		</g:form>
	</div>
</div>

</body></html>
