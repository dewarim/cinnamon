<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="lifeCycle.list.title"/></title>
</head>

<body>

<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
    <span class="menuButton">
        <g:remoteLink class="create" action="create"
                      update="[success: 'createLifeCycle', failure: 'message']">
            <g:message code="lifeCycle.create"/></g:remoteLink>
    </span>
    <span class="menuButton">
        <g:link class="list" action="index" controller="lifeCycleState">
            <g:message code="lcs.list"/></g:link>
    </span>
</div>

<div class="content">

    <g:render template="/shared/infoMessage" model="[infoMessage: infoMessage]"/>

    <div class="create_form" id="createLifeCycle"></div>

    <h1><g:message code="lifeCycle.list.title"/></h1>


    <div class="list" id="lifeCycleTable">
        <g:if test="${lifeCycleList?.isEmpty()}">
            <g:message code="lifeCycle.none.defined"/>
        </g:if>
        <g:else>
            <g:render template="list_table" model="[lifeCycleList: lifeCycleList]"/>
        </g:else>
    </div>
</div>


</body></html>
