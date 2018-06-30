<%@ page import="cinnamon.i18n.UiLanguage" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="user.edit.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="user.list"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="user.create"/></g:link></span>
    <span class="menuButton"><g:link class="delete" action="deleteAsk"><g:message
            code="user.delete.link"/></g:link></span>
    <span class="menuButton"><g:link class="replaceUser" action="replaceUser"><g:message
            code="user.replaceUser.link"/></g:link></span>

</div>

<div class="content">
    <h1><g:message code="user.edit.title"/></h1>

    <g:render template="/shared/message"/>
    <g:render template="/shared/errors" bean="${user}"/>

    <g:form method="post">
        <input type="hidden" name="id" value="${user?.id}"/>

        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="name"><g:message code="user.name"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: user, field: 'name', 'errors')}">
                        <input type="text" name="name" id="name" value="${fieldValue(bean: user, field: 'name')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="fullname"><g:message code="user.fullname"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: user, field: 'fullname', 'errors')}">
                        <input type="text" name="fullname" id="fullname"
                               value="${fieldValue(bean: user, field: 'fullname')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="description"><g:message code="user.description"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: user, field: 'description', 'errors')}">
                        <!-- <input type="text" name="description" id="description" value="${fieldValue(bean: user, field: 'description')}" /> -->
                        <g:descriptionTextArea name="description"
                                               value="${fieldValue(bean: user, field: 'description')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="email"><g:message code="user.email"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: user, field: 'email', 'errors')}">
                        <input type="text" name="email" id="email" value="${fieldValue(bean: user, field: 'email')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="name"><g:message code="user.language"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: user, field: 'language', 'errors')}">
                        <g:select name="language.id" from="${UiLanguage.list()}" optionKey="id"
                                  value="${user?.language?.id}" optionValue="isoCode"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="pwd"><g:message code="user.password"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: user, field: 'pwd', 'errors')}">
                        <input type="password" name="pwd" id="pwd" value=""
                            <g:if test="${!pwdChangeAllowed}">disabled="disabled" title="<g:message code="user.password.denied.logintype"/>"</g:if>
                        />
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name"><g:message code="user.login.type"/></td>
                    <td class="value">${fieldValue(bean:user,field:'loginType')}</td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="activated"><g:message code="user.activated"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: user, field: 'activated', 'errors')}">
                        <input type="checkbox" id="activated" name="activated" value="true"
                               <g:if test="${user.activated}">checked</g:if>>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="sudoer"><g:message code="user.sudoer"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: user, field: 'sudoer', 'errors')}">
                        <input type="checkbox" id="sudoer" name="sudoer" value="true"
                               <g:if test="${user.sudoer}">checked</g:if>>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="sudoable"><g:message code="user.sudoable"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: user, field: 'sudoable', 'errors')}">
                        <input type="checkbox" id="sudoable" name="sudoable" value="true"
                               <g:if test="${user.sudoable}">checked</g:if>>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="changeTracking"><g:message code="user.changeTracking"/></label>

                        <p><g:message code="user.changeTracking.help"/></p>
                    </td>
                    <td class="value ${hasErrors(bean: user, field: 'changeTracking', 'errors')}">
                        <input type="checkbox" name="changeTracking" id="changeTracking" autocomplete="off" <g:if
                                test="${user.changeTracking}">checked="checked"</g:if>>
                    </td>
                </tr>

                <!-- link to the list of group -->
                <tr class="prop">
                    <td colspan="2" class="name left">
                        <g:link controller='group' action='showGroupsByUser'
                                id='${user.id}'><g:message code="user.show_groups"/></g:link>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:actionSubmit action="update" class="save"
                                                 value="${message(code: 'update')}"/></span>
        </div>
    </g:form>
</div>
</body></html>
