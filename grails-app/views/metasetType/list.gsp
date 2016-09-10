<%@ page import="cinnamon.MetasetType" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'metasetType.label', default: 'MetasetType')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>

<a href="#list-metasetType" class="skip" tabindex="-1">
    <g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton">
        <g:link class="create" action="create"><g:message code="metasetType.create"/></g:link>
    </span>
</div>

<div id="list-metasetType" class="content scaffold-list" role="main">
    <h1><g:message code="default.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <div id="metasetTypeList" class="list">
        <g:render template="metasetTypeList" model="[metasetTypeInstanceList: metasetTypeInstanceList]"/>
    </div>
</div>
</body>
</html>
