<!DOCTYPE html>
<html>
<head>
    <style lang="text/css">
    body {
        margin-left: 40%;
        margin-right: 40%;
        margin-top: 15%;
    }
    </style>
    <r:layoutResources />
</head>

<body>
<h1><g:message code="error.system.failure"/></h1>

<p><g:message code="logout.info"/></p>

<g:if test="${logoutMessage}">
    <h2><g:message code="system.message"/></h2>
    <p>${logoutMessage}</p>
</g:if>

<p><em>
    <g:message code="logout.suggestion"/>
</em>
</p>

<g:if test="${session?.repositoryName}">
    <g:link controller="logout" action="index"><g:message code="logout.link" args="[session.repositoryName]"/></g:link>
</g:if>
<g:else>
    <g:link controller="logout" action="index"><g:message code="logout.link.simple"/></g:link>
</g:else>
<r:layoutResources />
</body>
</html>