<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="folderType.list.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="create" action="create"><g:message
            code="folderType.create.link"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="folderType.list.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div id="folderTypeList" class="list">
        <g:render template="folderTypeList" model="[folderTypeList: folderTypeList]"/>
    </div>

</div>

</body></html>
