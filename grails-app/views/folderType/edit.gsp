<!DOCTYPE HTML>
<html>
<head>
	<g:render template="/shared/header"/>
	<title><g:message code="folderType.edit.title"/></title>
    </head>
    <body>
        <g:render template="/shared/logo"/>  <div class="nav">
			<g:homeButton><g:message code="home"/></g:homeButton>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="folderType.list.link"/></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="folderType.create.link"/></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="folderType.edit.title"/></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${folderType}">
            <div class="errors">
                <g:renderErrors bean="${folderType}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${folderType?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="folderType.name"/></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:folderType,field:'name','errors')}">
                                    <input type="text" name="name" id="name" value="${fieldValue(bean:folderType,field:'name')}" />
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description"><g:message code="folderType.description"/></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:folderType,field:'description','errors')}">
                                	<g:descriptionTextArea name="description" value="${fieldValue(bean:folderType,field:'description')}" />
                         		</td>
                            </tr> 
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="${message(code:'update')}" /></span>
                </div>
            </g:form>
        </div>

<g:render template="/shared/footer"/>
</body></html>
        