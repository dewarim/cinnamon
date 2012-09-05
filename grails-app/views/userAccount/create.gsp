<%@ page import="cinnamon.i18n.UiLanguage" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="user.list.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="user.list"/></g:link></span>
    <span class="menuButton"><g:link class="delete" action="deleteAsk"><g:message
            code="user.delete.link"/></g:link></span>
    <span class="menuButton"><g:link class="replaceUser" action="replaceUser"><g:message
            code="user.replaceUser.link"/></g:link></span>

</div>

<div class="content">
    <h1><g:message code="user.create.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${user}">
        <div class="errors">
            <g:renderErrors bean="${user}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save" method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="name"><g:message code="user.name"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: user, field: 'name', 'errors')}">
                        <input type="text" name="name" id="name" value="${fieldValue(bean: user, field: 'name')}"/>
                        <script type="text/javascript">
                            $('#name').focus();
                        </script>
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
                        <g:descriptionTextArea name="description" value="${fieldValue(bean:user,field:'description')}"/>
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
                        <input type="text" name="pwd" id="pwd" value="${fieldValue(bean: user, field: 'pwd')}"/>
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

