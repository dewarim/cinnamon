<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="format.edit.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="format.list"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="format.create"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="format.edit.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${format}">
        <div class="errors">
            <g:renderErrors bean="${format}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <input type="hidden" name="id" value="${format?.id}"/>

        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><g:message code="format.name"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: format, field: 'name', 'errors')}">
                        <input type="text" name="name" id="name" value="${fieldValue(bean: format, field: 'name')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="contenttype"><g:message code="format.contenttype"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: format, field: 'contenttype', 'errors')}">
                        <input type="text" name="contenttype" id="contenttype"
                               value="${fieldValue(bean: format, field: 'contenttype')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="description"><g:message code="format.description"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: format, field: 'description', 'errors')}">
                        <!-- <input type="text" name="description" id="description" value="${fieldValue(bean: format, field: 'description')}" /> -->
                        <g:descriptionTextArea name="description"
                                               value="${fieldValue(bean: format, field: 'description')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="extension"><g:message code="format.extension"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: format, field: 'extension', 'errors')}">
                        <input type="text" name="extension" id="extension"
                               value="${fieldValue(bean: format, field: 'extension')}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" value="${message(code: 'update')}"/></span>
        </div>
    </g:form>
</div>


</body></html>
