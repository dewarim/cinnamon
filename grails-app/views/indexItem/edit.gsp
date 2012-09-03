<!DOCTYPE HTML>
<html>
<head>
	<meta name="layout" content="main"/>

        <g:set var="entityName" value="${message(code: 'indexItem.label', default: 'IndexItem')}" />
        <title><g:message code="indexItem.edit.title" /></title>
    </head>
    <body>
        
	<div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="home"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="indexItem.list.link" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="indexItem.create.link" /></g:link></span>
        </div>
        <div class="content">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${indexItemInstance}">
            <div class="errors">
                <g:renderErrors bean="${indexItemInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${indexItemInstance?.id}" />
                <g:hiddenField name="obj_version" value="${indexItemInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                       
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="label.name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${indexItemInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="fieldname"><g:message code="indexItem.fieldname.label" default="Fieldname" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'fieldname', 'errors')}">
                                    <g:textField name="fieldname" value="${indexItemInstance?.fieldname}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="forContent"><g:message code="indexItem.forContent.label" default="For Content" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'forContent', 'errors')}">
                                    <g:checkBox name="forContent" value="${indexItemInstance?.forContent}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="forMetadata"><g:message code="indexItem.forMetadata.label" default="For Metadata" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'forMetadata', 'errors')}">
                                    <g:checkBox name="forMetadata" value="${indexItemInstance?.forMetadata}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="forSysMeta"><g:message code="indexItem.forSysMeta.label" default="For Sys Meta" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'forSysMeta', 'errors')}">
                                    <g:checkBox name="forSysMeta" value="${indexItemInstance?.forSysMeta}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="indexGroup"><g:message code="indexItem.indexGroup.label" default="Index Group" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'indexGroup', 'errors')}">
                                    <g:select id="indexGroup" name="indexGroup.id" from="${cinnamon.index.IndexGroup.list()}" optionKey="id" value="${indexItemInstance?.indexGroup?.id}" optionValue="name" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="indexType"><g:message code="indexItem.indexType.label" default="Index Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'indexType', 'errors')}">
                                    <g:select id="indexType" name="indexType.id" from="${cinnamon.index.IndexType.list()}" optionKey="id" value="${indexItemInstance?.indexType?.id}" optionValue="name" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="multipleResults"><g:message code="indexItem.multipleResults.label" default="Multiple Results" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'multipleResults', 'errors')}">
                                    <g:checkBox name="multipleResults" value="${indexItemInstance?.multipleResults}" />
                                </td>
                            </tr>
                         
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="searchCondition"><g:message code="indexItem.searchCondition.label" default="Search Condition" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'searchCondition', 'errors')}">
                                    <!-- <g:textField name="searchCondition" value="${indexItemInstance?.searchCondition}" /> -->
                                    <g:xpathTextArea name="searchCondition" value="${fieldValue(bean:indexItemInstance,field:'searchCondition')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="searchString"><g:message code="indexItem.searchString.label" default="Search String" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'searchString', 'errors')}">
                                    <!-- <g:textField name="searchString" value="${indexItemInstance?.searchString}" /> -->
                                    <g:xpathTextArea name="searchString" value="${fieldValue(bean:indexItemInstance,field:'searchString')}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="systemic"><g:message code="indexItem.systemic.label" default="Systemic" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'systemic', 'errors')}">
									<g:if test="${indexItemInstance?.systemic}">
										<input type="checkbox" id="systemic" name="systemic" value="${indexItemInstance?.systemic}" checked="checked" disabled="disabled"/>
									</g:if>
                                    <g:else>
										<input type="checkbox" id="systemic" name="systemic" value="${indexItemInstance?.systemic}" disabled="disabled"/>
                                    </g:else>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="vaProviderParams"><g:message code="indexItem.vaProviderParams.label" default="Va Provider Params" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: indexItemInstance, field: 'vaProviderParams', 'errors')}">
                                    <!-- <g:textField name="vaProviderParams" value="${indexItemInstance?.vaProviderParams}" /> -->
                                    <g:xmlParamsTextArea name="vaProviderParams" value="${fieldValue(bean:indexItemInstance,field:'vaProviderParams')}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                </div>
            </g:form>
        </div>
		
    </body>
</html>
