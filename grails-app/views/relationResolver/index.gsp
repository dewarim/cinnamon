<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
	<title><g:message code="relationResolver.list.title"/></title>

</head>

<body>
  <div class="nav">
	<g:homeButton><g:message code="home"/></g:homeButton>
	<span class="menuButton">
		<g:remoteLink class="create" action="create"
					  update="[success:'createRelationResolver', failure:'message']">
			<g:message code="relationResolver.create"/></g:remoteLink>
	</span>
</div>

<div class="content">

	<g:render template="/shared/infoMessage" model="[infoMessage:infoMessage]"/>

	<div class="create_form" id="createRelationResolver"></div>

	<h1><g:message code="relationResolver.list.title"/></h1>

	<g:if test="${relationResolverList?.isEmpty()}">
		<g:message code="relationResolver.none.defined"/>
	</g:if>
	<g:else>

		<div class="list" id="relationResolverTable">
			<g:render template="list_table" model="[relationResolverList:relationResolverList]"/>
		</div>

	</g:else>
</div>


</body></html>
