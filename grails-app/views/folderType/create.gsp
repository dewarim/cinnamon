<!DOCTYPE HTML>
<html>
<head>
    <g:render template="/shared/header"/>

    <title><g:message code="folderType.create.title"/></title>
</head>

<body>
<g:render template="/shared/logo"/>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="folderType.list.link"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="folderType.create.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${folderType}">
        <div class="errors">
            <g:renderErrors bean="${folderType}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save" method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="ftName"><g:message code="folderType.name"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: folderType, field: 'name', 'errors')}">
                        <input type="text" name="name" id="ftName"
                               value="${fieldValue(bean: folderType, field: 'name')}"/>
                        <script type="text/javascript">
                            $('#ftName').focus();
                        </script>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="description"><g:message code="folderType.description"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: folderType, field: 'description', 'errors')}">
                        <g:descriptionTextArea name="description"
                                               value="${fieldValue(bean:folderType,field:'description')}"/>
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

<g:render template="/shared/footer"/>
</body></html>
