<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>

    <title><g:message code="health.checkGroups.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton"><g:link class="list" action="index" controller="health"><g:message
            code="health.to.index"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="health.checkGroups.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="dialog">
        <p>
            <g:message code="health.checkGroups.intro"/>
        </p>
        <table border="1" cellpadding="6" class="user_health">
            <tbody>
            <tr>
                <td><g:message code="health.groups.everyone"/></td>
                <td>
                    <g:if test="${!viewParams['everyoneAlias']}">
                        <g:message code="health.everyone.missing"/>
                        <g:link action="fixEveryoneAlias" controller="health">
                            <g:message code="health.to.fixEveryone"/>
                        </g:link>
                    </g:if>
                    <g:else>
                        <img src="${g.resource(dir: '/images', file: 'ok.png')}" alt="${message(code: 'status.ok')}"/>
                    </g:else>
                </td>
            </tr>
            <tr>
                <td><g:message code="health.ownerAlias"/></td>
                <td>
                    <g:if test="${!viewParams['ownerAlias']}">
                        <g:message code="health.owner.missing"/>
                        <g:link action="fixOwnerAlias" controller="health">
                            <g:message code="health.to.fixOwner"/>
                        </g:link>
                    </g:if>
                    <g:else>
                        <img src="${g.resource(dir: '/images', file: 'ok.png')}" alt="${message(code: 'status.ok')}"/>
                    </g:else>
                </td>
            </tr>
            <tr>
                <td><g:message code="health.usersGroup"/></td>
                <td>
                    <g:if test="${!viewParams['userGroup']}">
                        <g:message code="health.usersGroup.missing"/>
                        <g:link action="fixUsersGroup" controller="health">
                            <g:message code="health.to.fixUserGroup"/>
                        </g:link>
                    </g:if>
                    <g:else>
                        <img src="${g.resource(dir: '/images', file: 'ok.png')}" alt="${message(code: 'status.ok')}"/>
                    </g:else>
                </td>
            </tr>
            <tr>
                <td><g:message code="health.usersGroup.status"/> <br>
                    <g:message code="health.usersGroup.intro"/>
                </td>
                <td>
                    <g:if test="${!viewParams['allUsersInUserGroup']}">
                        <g:message code="health.usersGroup.incomplete"/>
                        <g:link action="fillUpUsersGroup" controller="health">
                            <g:message code="health.to.fillUpUsersGroup"/>
                        </g:link>
                    </g:if>
                    <g:else>
                        <img src="${g.resource(dir: '/images', file: 'ok.png')}" alt="${message(code: 'status.ok')}"/>
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
