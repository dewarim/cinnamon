<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'indexItem.label', default: 'IndexItem')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
  <div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="home"/></a></span>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="indexItem.list.link"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="default.create.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${indexItemInstance}">
        <div class="errors">
            <g:renderErrors bean="${indexItemInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:if test="${flash.error}">
        <div class="errors">
            <ul class="errors">
                <li class="errors">${flash.error}</li>
            </ul>
        </div>
    </g:if>
    <g:form action="save" method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="name"><g:message code="indexItem.name.label" default="Name"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexItemInstance, field: 'name', 'errors')}">
                        <g:textField name="name" value="${indexItemInstance?.name}"/>
                        <script type="text/javascript">
                            $('#name').focus();
                        </script>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="fieldname"><g:message code="indexItem.fieldname.label" default="Fieldname"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexItemInstance, field: 'fieldname', 'errors')}">
                        <g:textField name="fieldname" value="${indexItemInstance?.fieldname}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="forContent"><g:message code="indexItem.forContent.label"
                                                           default="For Content"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexItemInstance, field: 'forContent', 'errors')}">
                        <g:checkBox name="forContent" value="${indexItemInstance?.forContent}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="forMetadata"><g:message code="indexItem.forMetadata.label"
                                                            default="For Metadata"/></label>
                    </td>
                    <td
                        class="value ${hasErrors(bean: indexItemInstance, field: 'forMetadata', 'errors')}">
                        <g:checkBox name="forMetadata" value="${indexItemInstance?.forMetadata}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="forSysMeta"><g:message code="indexItem.forSysMeta.label"
                                                           default="For Sys Meta"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexItemInstance, field: 'forSysMeta', 'errors')}">
                        <g:checkBox name="forSysMeta" value="${indexItemInstance?.forSysMeta}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="indexGroup"><g:message code="indexItem.indexGroup.label"
                                                           default="Index Group"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexItemInstance, field: 'indexGroup', 'errors')}">
                        <g:select id="indexGroup" name="indexGroup.id" from="${cinnamon.index.IndexGroup.list()}"
                                  optionKey="id" value="${indexItemInstance?.indexGroup?.id}" optionValue="name"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="indexType"><g:message code="indexItem.indexType.label"
                                                          default="Index Type"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexItemInstance, field: 'indexType', 'errors')}">
                        <g:select id="indexType" name="indexType.id" from="${cinnamon.index.IndexType.list()}"
                                  optionKey="id" value="${indexItemInstance?.indexType?.id}" optionValue="name"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="multipleResults"><g:message code="indexItem.multipleResults.label"
                                                                default="Multiple Results"/></label>
                    </td>
                    <td
                        class="value ${hasErrors(bean: indexItemInstance, field: 'multipleResults', 'errors')}">
                        <g:checkBox name="multipleResults" value="${indexItemInstance?.multipleResults}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="searchCondition"><g:message code="indexItem.searchCondition.label"
                                                                default="Search Condition"/></label>
                    </td>
                    <td
                        class="value ${hasErrors(bean: indexItemInstance, field: 'searchCondition', 'errors')}">
                        <!-- <g:textField name="searchCondition" value="${indexItemInstance?.searchCondition}"/> -->
                        <g:xpathTextArea name="searchCondition"
                                         value="${fieldValue(bean:indexItemInstance,field:'searchCondition')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="searchString"><g:message code="indexItem.searchString.label"
                                                             default="Search String"/></label>
                    </td>
                    <td
                        class="value ${hasErrors(bean: indexItemInstance, field: 'searchString', 'errors')}">
                        <!-- <g:textField name="searchString" value="${indexItemInstance?.searchString}"/> -->
                        <g:xpathTextArea name="searchString"
                                         value="${fieldValue(bean:indexItemInstance,field:'searchString')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="systemic"><g:message code="indexItem.systemic.label" default="Systemic"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexItemInstance, field: 'systemic', 'errors')}">
                        <g:checkBox name="systemic" value="${indexItemInstance?.systemic}"/>
                    </td>
                </tr>
                
                <tr class="prop">
                    <td class="name">
                        <label for="store"><g:message code="indexItem.storeField.label" default="Store content"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexItemInstance, field: 'storeField', 'errors')}">
                        <g:checkBox name="storeField" value="${indexItemInstance?.storeField}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="vaProviderParams"><g:message code="indexItem.vaProviderParams.label"
                                                                 default="Va Provider Params"/></label>
                    </td>
                    <td
                        class="value ${hasErrors(bean: indexItemInstance, field: 'vaProviderParams', 'errors')}">
                        <!-- <g:textField name="vaProviderParams" value="${indexItemInstance?.vaProviderParams}"/> -->
                        <g:xmlParamsTextArea name="vaProviderParams"
                                             value="${fieldValue(bean:indexItemInstance,field:'vaProviderParams')}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:submitButton name="create" class="save"
                                                 value="${message(code: 'default.button.create.label', default: 'Create')}"/></span>
        </div>
    </g:form>
</div>

</body>
</html>
