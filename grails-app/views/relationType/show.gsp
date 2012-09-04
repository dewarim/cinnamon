<!DOCTYPE HTML>
<html>
<head>
	<meta name="layout" content="main"/>

        <title><g:message code="relationType.show.title"/></title>
    </head>
    <body>
        
	<div class="nav">
			<g:homeButton><g:message code="home"/></g:homeButton>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="relationType.list"/></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="relationType.create"/></g:link></span>
        </div>
        <div class="content">
            <h1><g:message code="relationType.show.title"/></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
				
				<g:render template="/relationType/relationTypeTable" model="[relationType:relationType]"/>
 
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${relationType?.id}" />
                    <span class="button"><g:actionSubmit action="edit" class="edit" value="${message(code:'edit')}" /></span>
                    <span class="button"><g:actionSubmit action="delete" class="delete" onclick="return confirm('${message(code:'relationType.confirm.delete')?.encodeAsHTML()}');" value="${message(code:'delete')}" /></span>
                </g:form>
            </div>
        </div>


</body></html>
