<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
	<g:set var="entityName" value="${message(code: 'indexItem.label', default: 'IndexItem')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
	<div class="nav">
            <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="indexItem.list.link" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="indexItem.create.link" /></g:link></span>
        </div>
        <div class="content">
            <h1><g:message code="indexItem.show.h1" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:if test="${flash.error}">
            	<div class="errors">
	            	<ul class="errors">
	           			<li class="errors">${flash.error}</li>
	            	</ul>
            	</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="indexItem.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: indexItemInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="indexItem.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: indexItemInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="indexItem.fieldname.label" default="Fieldname" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: indexItemInstance, field: "fieldname")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><abbr title="${message(code: 'indexItem.forContent.description')}"><g:message code="indexItem.forContent.label" default="For Content" /></abbr></td>
                            
                            <td valign="top" class="value"><g:checkBox name="forContent" disabled="true" value="${indexItemInstance?.forContent}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><abbr title="${message(code: 'indexItem.forMetadata.description')}"><g:message code="indexItem.forMetadata.label" default="For Metadata" /></abbr></td>
                            
                            <td valign="top" class="value"><g:checkBox name="forContent" disabled="true" value="${indexItemInstance?.forMetadata}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><abbr title="${message(code: 'indexItem.forSysMeta.description')}"><g:message code="indexItem.forSysMeta.label" default="For Sys Meta" /></abbr></td>
                            
                            <td valign="top" class="value"><g:checkBox name="forContent" disabled="true" value="${indexItemInstance?.forSysMeta}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="indexItem.indexGroup.label" default="Index Group" /></td>
                            
                            <td valign="top" class="value"><g:link controller="indexGroup" action="show" id="${indexItemInstance?.indexGroup?.id}">${indexItemInstance?.indexGroup?.name}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="indexItem.indexType.label" default="Index Type" /></td>
                            
                            <td valign="top" class="value"><g:link controller="indexType" action="show" id="${indexItemInstance?.indexType?.id}">${indexItemInstance?.indexType?.name}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><abbr title="${message(code: 'indexItem.multipleResults.description')}"><g:message code="indexItem.multipleResults.label" default="Multiple Results" /></abbr></td>
                            
                            <td valign="top" class="value"><g:checkBox name="multipleResults" disabled="true" value="${indexItemInstance?.multipleResults}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="indexItem.searchCondition.label" default="Search Condition" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: indexItemInstance, field: "searchCondition")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="indexItem.searchString.label" default="Search String" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: indexItemInstance, field: "searchString")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><abbr title="${message(code: 'indexItem.systemic.description')}"><g:message code="indexItem.systemic.label" default="Systemic" /></abbr></td>
                            
                            <td valign="top" class="value"><g:checkBox name="systemic" disabled="true" value="${indexItemInstance?.systemic}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="indexItem.vaProviderParams.label" default="Va Provider Params" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: indexItemInstance, field: "vaProviderParams")}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${indexItemInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
