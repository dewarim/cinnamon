<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <g:set var="entityName" value="${message(code: 'indexGroup.label', default: 'IndexGroup')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="indexGroup.list.link"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="indexGroup.create.h1"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${indexGroupInstance}">
        <div class="errors">
            <g:renderErrors bean="${indexGroupInstance}" as="list"/>
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
                    <td valign="top" class="name">
                        <label for="name"><g:message code="label.name"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: indexGroupInstance, field: 'name', 'errors')}">
                        <g:textField name="name" value="${indexGroupInstance?.name}"/>
                        <script type="text/javascript">
                            $('#name').focus();
                        </script>
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
