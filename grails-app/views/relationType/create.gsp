<%@ page import="cinnamon.relation.RelationResolver" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <title><g:message code="relationType.create.title"/></title>
</head>

<body>

<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="relationType.list"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="relationType.create.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${relationType}">
        <div class="errors">
            <g:renderErrors bean="${relationType}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save" method="post">
        <div class="dialog">
            <g:render template="form" model="[relationType:relationType]"/>
        </div>

        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="${message(code: 'create')}"/></span>
        </div>
    </g:form>
</div>


</body></html>
