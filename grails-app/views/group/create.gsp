<%@ page import="cinnamon.CmnGroup" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="group.list"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="group.list"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="group.create.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${group}">
        <div class="errors">
            <g:renderErrors bean="${group}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save" method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="groupName"><g:message code="group.name"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: group, field: 'name', 'errors')}">
                        <input type="text" name="name" id="groupName" value="${fieldValue(bean: group, field: 'name')}"/>
                        <script type="text/javascript">
                            $('#groupName').focus();
                        </script>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="description"><g:message code="group.description"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: group, field: 'description', 'errors')}">
                        <!-- <input type="text" name="description" id="description" value="${fieldValue(bean: group, field: 'description')}" /> -->
                        <g:descriptionTextArea name="description"
                                               value="${fieldValue(bean:group,field:'description')}"/>
                    </td>
                </tr>



                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="parent"><g:message code="group.parent"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: group, field: 'parent', 'errors')}">
                        <g:select id="parent" optionKey="id"
                                  from="${CmnGroup.list()}"
                                  name="parent.id"
                                  optionValue="name"
                                  noSelection="${['null': message(code:'group.parent.none_selected') ]}"
                                  value="${group?.parent?.id}"></g:select>
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
