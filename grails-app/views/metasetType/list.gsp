<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'metasetType.label', default: 'MetasetType')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-metasetType" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-metasetType" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>

                        <th><g:message code="id"/></th>

						<g:sortableColumn property="config" title="${message(code: 'metasetType.config.label', default: 'Config')}" />
					
						<g:sortableColumn property="description" title="${message(code: 'metasetType.description.label', default: 'Description')}" />
					
						<g:sortableColumn property="name" title="${message(code: 'metasetType.name.label', default: 'Name')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${metasetTypeInstanceList}" status="i" var="metasetTypeInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

						<td><g:link action="show" id="${metasetTypeInstance.id}">${fieldValue(bean: metasetTypeInstance, field: "id")}</g:link></td>

                        <td>${fieldValue(bean: metasetTypeInstance, field: "config")}</td>

						<td>${fieldValue(bean: metasetTypeInstance, field: "description")}</td>
					
						<td>${fieldValue(bean: metasetTypeInstance, field: "name")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${metasetTypeInstanceTotal}" />
			</div>
		</div>
	</body>
</html>
