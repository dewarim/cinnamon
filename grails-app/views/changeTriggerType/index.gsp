<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
	<title><g:message code="changeTriggerType.list.title"/></title>
</head>
<body>
<div class="nav">
	<g:homeButton><g:message code="home"/></g:homeButton>
	<span class="menuButton">
		<g:remoteLink class="create" action="create"
				update="[success:'createChangeTriggerType', failure:'message']">
			<g:message code="changeTriggerType.create"/></g:remoteLink>
	</span>
</div>
<div class="content">
	<g:render template="/shared/infoMessage" model="[infoMessage:infoMessage]"/>

	<div class="create_form" id="createChangeTriggerType"></div>

	<h1><g:message code="changeTriggerType.list.title"/></h1>


	<g:if test="${changeTriggerTypeList?.isEmpty()}">
		<g:message code="changeTriggerType.none.defined"/>
	</g:if>
	<g:else>

		<div class="list" id="changeTriggerTypeList">
			<g:render template="list_table" model="[changeTriggerTypeList:changeTriggerTypeList]"/>
		</div>

	</g:else>
</div>

</body></html>
