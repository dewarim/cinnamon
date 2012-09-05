<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="format.create.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="format.list"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="format.create.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${format}">
        <div class="errors">
            <g:renderErrors bean="${format}" as="list"/>
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
                        <label for="name"><g:message code="format.name"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: format, field: 'name', 'errors')}">
                        <input type="text" name="name" id="name" value="${fieldValue(bean: format, field: 'name')}"/>
                        <script type="text/javascript">
                            $('#name').focus();
                        </script>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="contenttype"><g:message code="format.contenttype"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: format, field: 'contenttype', 'errors')}">
                        <input type="text" name="contenttype" id="contenttype"
                               value="${fieldValue(bean: format, field: 'contenttype')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="description"><g:message code="format.description"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: format, field: 'description', 'errors')}">
                        <!-- <input type="text" name="description" id="description" value="${fieldValue(bean: format, field: 'description')}" /> -->
                        <g:descriptionTextArea name="description"
                                               value="${fieldValue(bean:format,field:'description')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="extension"><g:message code="format.extension"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: format, field: 'extension', 'errors')}">
                        <input type="text" name="extension" id="extension"
                               value="${fieldValue(bean: format, field: 'extension')}"/>
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
