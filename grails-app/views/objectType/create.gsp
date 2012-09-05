<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="objectType.create.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="objectType.list"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="objectType.create.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${objectType}">
        <div class="errors">
            <g:renderErrors bean="${objectType}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save" method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><g:message code="objectType.name"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: objectType, field: 'name', 'errors')}">
                        <input type="text" name="name" id="name"
                               value="${fieldValue(bean: objectType, field: 'name')}"/>
                        <script type="text/javascript">
                            $('#name').focus();
                        </script>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="description"><g:message code="objectType.description"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: objectType, field: 'description', 'errors')}">
                        <!-- <input type="text" name="description" id="description" value="${fieldValue(bean: objectType, field: 'description')}" /> -->
                        <g:descriptionTextArea name="description"
                                               value="${fieldValue(bean: objectType, field: 'description')}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="${message(code: 'create')}"/></span>
        </div>
    </g:form>
</div>

</body></html>
