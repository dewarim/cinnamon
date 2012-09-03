<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="configEntry.list.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton">
        <g:remoteLink class="create" action="create"
                      update="[success:'createConfigEntry', failure:'message']">
            <g:message code="configEntry.create"/></g:remoteLink>
    </span>
</div>

<div class="content">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div id="ajaxMessage" style="margin-top:1ex;"></div>

    <div class="create_form" id="createConfigEntry"></div>

    <h1><g:message code="configEntry.list.title"/></h1>

<div class="list" id="configEntryList">
    <g:if test="${configEntryList?.isEmpty()}">
    <g:message code="configEntry.none.defined"/>
    </g:if>
    <g:else>

        <g:render template="list_table" model="[configEntryList:configEntryList]"/>

        </div>

    </g:else>
</div>

</body>
</html>