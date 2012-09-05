<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
	<title><g:message code="transformer.title"/></title>

</head>
<body>
  <div class="nav">
	<g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
	<span class="menuButton">
		<g:remoteLink class="create" action="create" controller="transformer"
				update="[success:'createTransformer', failure:'message']">
			<g:message code="transformer.create"/></g:remoteLink>
	</span>
</div>
<div class="content">

	<g:render template="/shared/infoMessage" model="[infoMessage:infoMessage]"/>

	<div class="create_form" id="createTransformer"></div>

	<h1><g:message code="transformer.title"/></h1>


	<g:if test="${transformerList?.isEmpty()}">
			<div class="list" id="transformerTable">
		<g:message code="transformer.none.defined"/>
		</div>
	</g:if>
	<g:else>

		<div class="list" id="transformerTable">

			<g:render template="list_table" model="[transformerList:transformerList]"/>

		</div>

	</g:else>
</div>


</body></html>
