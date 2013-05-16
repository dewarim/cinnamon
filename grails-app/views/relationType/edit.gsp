<%@ page import="cinnamon.relation.RelationResolver" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="relationType.edit.title"/></title>
</head>

<body>
  <div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="relationType.list"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message
            code="relationType.create"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="relationType.edit.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${relationType}">
        <div class="errors">
            <g:renderErrors bean="${relationType}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <input type="hidden" name="id" value="${relationType?.id}"/>

        <div class="dialog">
            <g:render template="form" model="[relationType:relationType]"/>
        </div>

        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="update" value="${message(code:'update')}"/></span>
        </div>
    </g:form>
</div>


</body></html>
        