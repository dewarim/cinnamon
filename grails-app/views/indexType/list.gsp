<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <g:set var="entityName" value="${message(code: 'indexType.label', default: 'IndexType')}"/>
    <title><g:message code="indexType.list.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton">
        <g:link class="create" action="create">
            <g:message code="indexType.new.label" args="[entityName]"/>
        </g:link>
    </span>
</div>

<div class="content">
    <h1><g:message code="indexType.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list" id="indexTypeList">
        <g:render template="indexTypeList" model="[indexTypeList: indexTypeList]"/>
    </div>

</div>

</body></html>
