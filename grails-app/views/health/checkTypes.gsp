<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <title><g:message code="health.checkTypes.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton"><g:link class="list" action="index" controller="health"><g:message
            code="health.to.index"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="health.checkTypes.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="dialog">
        <p>
            <g:message code="health.checkTypes.intro"/>
        </p>
        <table border="1" cellpadding="6" class="user_health">
            <tbody>
            <tr>
                <td><g:message code="health.types.objectType"/></td>
                <td>
                    <g:if test="${!viewParams['defaultObjectType']}">
                        <g:message code="health.objectType.missing"/>
                        <g:link action="fixObjectTypes" controller="health">
                            <g:message code="health.to.fixObjectTypes"/>
                        </g:link>
                    </g:if>
                    <g:else>
                        <img src="${g.resource(dir: '/images', file: 'ok.png')}" alt="${g.message(code: 'status.ok')}"/>
                    </g:else>
                </td>
            </tr>
            <tr>
                <td><g:message code="health.types.folderType"/></td>
                <td>
                    <g:if test="${!viewParams['defaultFolderType']}">
                        <g:message code="health.folderType.missing"/>
                        <g:link action="fixFolderTypes" controller="health">
                            <g:message code="health.to.fixFolderTypes"/>
                        </g:link>
                    </g:if>
                    <g:else>
                        <img src="${g.resource(dir: '/images', file: 'ok.png')}" alt="${g.message(code: 'status.ok')}"/>
                    </g:else>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="buttons">

    </div>
</div>

</body></html>
