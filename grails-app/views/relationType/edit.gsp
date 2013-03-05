<%@ page import="cinnamon.relation.RelationResolver" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="relationType.edit.title"/></title>
</head>

<body>
  <div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="relationType.list"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message
            code="relationType.create"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="relationType.edit.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${relationType}">
        <div class="errors">
            <g:renderErrors bean="${relationType}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <input type="hidden" name="id" value="${relationType?.id}"/>

        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="name"><g:message code="relationType.name"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: relationType, field: 'name', 'errors')}">
                        <input type="text" name="name" id="name"
                               value="${fieldValue(bean: relationType, field: 'name')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="leftobjectprotected"><g:message code="relationType.leftobjectprotected"/></label>
                    </td>
                    <td
                        class="value ${hasErrors(bean: relationType, field: 'leftobjectprotected', 'errors')}">
                        <g:checkBox name="leftobjectprotected"
                                    value="${relationType?.leftobjectprotected}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="rightobjectprotected"><g:message code="relationType.rightobjectprotected"/></label>
                    </td>
                    <td
                        class="value ${hasErrors(bean: relationType, field: 'rightobjectprotected', 'errors')}">
                        <g:checkBox name="rightobjectprotected"
                                    value="${relationType?.rightobjectprotected}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="cloneOnLeftCopy"><g:message code="relationType.cloneOnLeftCopy"/></label>
                    </td>
                    <td
                        class="value ${hasErrors(bean: relationType, field: 'cloneOnLeftCopy', 'errors')}">
                        <g:checkBox name="cloneOnLeftCopy"
                                    value="${relationType?.cloneOnLeftCopy}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="cloneOnRightCopy"><g:message code="relationType.cloneOnRightCopy"/></label>
                    </td>
                    <td
                        class="value ${hasErrors(bean: relationType, field: 'cloneOnRightCopy', 'errors')}">
                        <g:checkBox name="cloneOnRightCopy"
                                    value="${relationType?.cloneOnRightCopy}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="left_resolver_id"><g:message code="relationType.leftResolver"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: relationType, field: 'leftResolver', 'errors')}">
                        <g:select name="left_resolver_id" from="${RelationResolver.list()}"
                                  value="${relationType?.leftResolver?.id}"
                                  optionKey="id"
                                  optionValue="name"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="right_resolver_id"><g:message code="relationType.rightResolver"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: relationType, field: 'rightResolver', 'errors')}">
                        <g:select name="right_resolver_id" from="${RelationResolver.list()}"
                                  value="${relationType?.rightResolver?.id}"
                                  optionKey="id"
                                  optionValue="name"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="update" value="${message(code:'update')}"/></span>
        </div>
    </g:form>
</div>


</body></html>
        