<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <g:set var="entityName" value="${message(code: 'indexType.label')}"/>
    <title><g:message code="indexType.create.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="indexType.list.label"
                                                                           args="[entityName]"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="indexType.create.label" args="[entityName]"/></h1>
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
    <g:hasErrors bean="${indexTypeInstance}">
        <div class="errors">
            <g:renderErrors bean="${indexTypeInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save" method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="name"><g:message code="indexType.name.label"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexTypeInstance, field: 'name', 'errors')}">
                        <g:textField name="name" value="${indexTypeInstance?.name}"/>
                        <script type="text/javascript">
                            $('#name').focus();
                        </script>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="dataType"><g:message code="indexType.dataType.label"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: indexTypeInstance, field: 'dataType', 'errors')}">
                        <g:select name="dataType" from="${cinnamon.index.DataType?.values()}"
                                  value="${indexTypeInstance?.dataType}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="indexerClass"><g:message code="indexType.indexerClass.label"/></label>
                    </td>
                    <td
                        class="value ${hasErrors(bean: indexTypeInstance, field: 'indexerClass', 'errors')}">
                        <g:select name="indexerClass" from="${indexers}"
                                  value="${indexTypeInstance?.indexerClass?.name}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="vaProviderClass"><g:message code="indexType.vaProviderClass.label"/></label>
                    </td>
                    <td
                        class="value ${hasErrors(bean: indexTypeInstance, field: 'vaProviderClass', 'errors')}">
                        <g:select id="vaProviderClass" name="vaProviderClass" from="${valueAssistanceProviders}"
                                  value="${indexTypeInstance?.vaProviderClass?.name}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:submitButton name="create" class="save"
                                                 value="${message(code: 'default.button.create.label')}"/></span>
        </div>
    </g:form>
</div>

</body></html>
