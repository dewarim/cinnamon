<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <g:set var="entityName" value="${message(code: 'indexType.label')}"/>
    <title><g:message code="indexType.edit.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="indexType.list.label"
                                                                           args="[entityName]"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="indexType.new.label"
                                                                               args="[entityName]"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="indexType.edit.label" args="[entityName]"/></h1>
    <g:render template="/shared/message"/>

    <g:hasErrors bean="${indexTypeInstance}">
        <div class="errors">
            <g:renderErrors bean="${indexTypeInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <g:hiddenField name="id" value="${indexTypeInstance?.id}"/>
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="name"><g:message code="indexType.name.label"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexTypeInstance, field: 'name', 'errors')}">
                        <g:textField name="name" value="${indexTypeInstance?.name}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="dataType"><g:message code="indexType.dataType.label"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexTypeInstance, field: 'dataType', 'errors')}">
                        <g:select name="dataType" from="${dataTypeValues}"
                                  value="${indexTypeInstance?.dataType}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="indexerClass"><g:message code="indexType.indexerClass.label"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexTypeInstance, field: 'indexerClass', 'errors')}">
                        <g:select name="indexerClassName" from="${indexers}"
                                  value="${indexTypeInstance?.indexerClass?.name}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="vaProviderClass"><g:message code="indexType.vaProviderClass.label"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexTypeInstance, field: 'vaProviderClass', 'errors')}">
                        <g:select id="vaProviderClassName" name="vaProviderClassName" from="${valueAssistanceProviders}"
                                  value="${indexTypeInstance?.vaProviderClass?.name}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="update"
                                                 value="${message(code: 'default.button.update.label')}"/></span>
        </div>
    </g:form>
</div>

</body></html>
