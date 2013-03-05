<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="objectType.edit.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="objectType.list"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message
            code="objectType.create"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="objectType.edit.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${objectType}">
        <div class="errors">
            <g:renderErrors bean="${objectType}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <input type="hidden" name="id" value="${objectType?.id}"/>

        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="name"><g:message code="objectType.name"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: objectType, field: 'name', 'errors')}">
                        <input type="text" name="name" id="name"
                               value="${fieldValue(bean: objectType, field: 'name')}"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="config">
                            <g:message code="objectType.config" default="Config" />
                        </label>
                    </td>
                    <td>
                        <div class="fieldcontain ${hasErrors(bean: objectType, field: 'config', 'error')} ">

                            <div class="value xml_editor">
                                <textarea id="config" style="width:100ex;border:1px black solid; " name="config" cols="120"
                                          rows="10">${objectType?.config ?: '<meta />'}</textarea>
                                <script type="text/javascript">
                                    createEditor($('#config').get(0))
                                </script>
                            </div>
                        </div>
                    </td>
                </tr>
             

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'update')}"/></span>
        </div>
    </g:form>
</div>

</body></html>
