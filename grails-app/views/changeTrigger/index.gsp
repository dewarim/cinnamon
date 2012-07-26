<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
	<title><g:message code="changeTrigger.list.title"/></title>
</head>
<body>
<div class="nav">
	<g:homeButton><g:message code="home"/></g:homeButton>
	<span class="menuButton">
		<g:remoteLink class="create" action="create"
				update="[success:'createChangeTrigger', failure:'message']">
			<g:message code="changeTrigger.create"/></g:remoteLink>
	</span>
</div>
<div class="content">
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div id="ajaxMessage" style="margin-top:1ex;"></div>

	<div class="create_form" id="createChangeTrigger"></div>

	<h1><g:message code="changeTrigger.list.title"/></h1>


	<g:if test="${changeTriggerList?.isEmpty()}">
		<g:message code="changeTrigger.none.defined"/>
	</g:if>
	<g:else>

		<div id="changeTriggerList" class="list">
			<g:render template="list_table" model="[changeTriggerList:changeTriggerList]"/>
		</div>

	</g:else>
</div>

</body></html>
