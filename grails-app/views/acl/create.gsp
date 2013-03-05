<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

        <title><g:message code="acl.create.title"/></title>
    </head>
    <body>
        <div class="nav">
			<g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="acl.list"/></g:link></span>
        </div>
        <div class="content">
            <h1><g:message code="acl.create.title"/></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${acl}">
            <div class="errors">
                <g:renderErrors bean="${acl}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td class="name">
                                    <label for="aclName"><g:message code="acl.name"/></label>
                                </td>
                                <td class="value ${hasErrors(bean:acl,field:'name','errors')}">
                                    <input type="text" name="name" id="aclName" value="${fieldValue(bean:acl,field:'name')}" />
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="${message(code:'create')}" /></span>
                </div>
            </g:form>
            <script type="text/javascript">
                $('#aclName').focus()
            </script>
        </div>

</body></html>