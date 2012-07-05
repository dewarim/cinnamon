<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="user.list.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="user.create"/></g:link></span>
    <span class="menuButton"><g:link class="replaceUser" action="replaceUser"><g:message
            code="user.replaceUser.link"/></g:link></span>
    <span class="menuButton"><g:link class="delete" action="deleteAsk"><g:message
            code="user.delete.link"/></g:link></span>

</div>

<div class="content">
    <h1><g:message code="user.list.title"/></h1>
    <g:render template="/shared/message"/>

    <div class="list" id="userList">
        <g:render template="userList" model="[userList: userList]"/>
    </div>
</div>

</body></html>
