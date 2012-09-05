<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

	<title><g:message code="language.edit.title"/></title>
</head>

<body>


<div class="nav">
	<g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
	<span class="menuButton"><g:link class="list" action="list"><g:message code="language.list"/></g:link></span>
	<span class="menuButton"><g:link class="create" action="create"><g:message code="language.create"/></g:link></span>
</div>

<div class="content">
	<h1><g:message code="language.edit.title"/></h1>
	<g:render template="/shared/message"/>
	<g:render template="/shared/errors" bean="${language}"/>

	<g:form method="post" action="update" controller="language" onSubmit="codeMirrorEditor.toTextArea();">
		<input type="hidden" name="id" value="${language?.id}"/>

		<div class="dialog">
			<table>
				<tbody>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="name"><g:message code="language.isoCode"/></label>
					</td>
					<td valign="top" class="value ${hasErrors(bean: language, field: 'isoCode', 'errors')}">
						<input id="name" type="text" name="isoCode" id="isoCode"
							   value="${fieldValue(bean: language, field: 'isoCode')}"/>
					</td>
				</tr>

	            <g:render template="editMetadata" model="[language:language]"/>

				</tbody>
			</table>
		</div>

		<div class="buttons">
			<span class="button">
				<input type="submit" name="save" value="${message(code: 'save.changes')}"/>
			</span>
		</div>
	</g:form>
</div>
</body></html>
