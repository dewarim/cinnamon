<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <g:set var="entityName" value="${message(code: 'indexGroup.label', default: 'IndexGroup')}"/>
    <title><g:message code="indexGroup.list.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="create" action="create"><g:message
            code="indexGroup.create.link"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="indexGroup.list.h1"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list" id="indexGroupList">
        <g:render template="indexGroupList" model="[indexGroupList: indexGroupList]"/>
    </div>

</div>
</body>
</html>
