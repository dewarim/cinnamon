<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="folderType.create.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="folderType.list.link"/></g:link></span>
</div>

<div class="content">
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
                    <td class="name">
                        <label for="ftName"><g:message code="folderType.name"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: folderType, field: 'name', 'errors')}">
                        <input type="text" name="name" id="ftName"
                               value="${fieldValue(bean: folderType, field: 'name')}"/>
                        <script type="text/javascript">
                            $('#ftName').focus();
                        </script>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="description"><g:message code="folderType.description"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: folderType, field: 'description', 'errors')}">
                        <g:descriptionTextArea name="description"
                                               value="${fieldValue(bean:folderType,field:'description')}"/>
                    </td>
                </tr>
                <tr class="xml_editor_row">
                    <td>
                        <label for="config_${folderType?.id}"><g:message code="folderType.config"/></label>
                    </td>
                    <td class="value xml_editor">
                        <textarea id="config_${folderType?.id}" class="xml-textarea" name="config" cols="120"
                                  rows="10">${folderType?.config ? folderType.config : '<config />'}</textarea>
                        <script type="text/javascript">
                            createEditor($('#config_${folderType?.id}').get(0))
                        </script>
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
