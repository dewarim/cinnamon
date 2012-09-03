<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="language.list.title"/></title>
</head>

<body>


<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="language.create"/></g:link></span>
</div>

<div class="content">
    <h1><g:message code="language.list.title"/></h1>

    <g:render template="/shared/message"/>

    <div class="list" id="languageList">
        <g:render template="languageList" model="[languageList:languageList]"/>
    </div>

</div>


</body></html>