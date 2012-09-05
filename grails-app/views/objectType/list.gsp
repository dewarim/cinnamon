<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="objectType.list.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="create" action="create"><g:message
            code="objectType.create"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="objectType.list.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div id="objectTypeList" class="list">
        <g:render template="objectTypeList" model="[objectTypeList: objectTypeList]"/>
    </div>
</div>
</body></html>
