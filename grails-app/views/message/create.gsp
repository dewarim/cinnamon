<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <title><g:message code="message.create.title"/></title>
</head>

<body>


<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="message.list"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="message.create.title"/></h1>

    <g:render template="/shared/message"/>
    <g:render template="/shared/errors" bean="${message}"/>

    <g:form action="save" method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="language"><g:message code="message.language"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: message, field: 'language', 'errors')}">
                        <input id="language" type="hidden" name="languageId" value="${defaultLanguage.id}"/>
                        <g:message code="lang.${defaultLanguage.isoCode}" default="${defaultLanguage.isoCode}"/>
                        <script type="text/javascript">
                            $('#language').focus();
                        </script>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="message"><g:message code="message.message"/></label>
                    </td>
                    <td class="value">
                        <input type="text" name="messageId" id="message" value="${messageId}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="translation"><g:message code="message.translation"/></label>
                    </td>
                    <td class="value">
                        <input type="text" name="translation" id="translation" value="${translation?.encodeAsHTML()}"/>
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

