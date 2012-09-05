<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="lifecycle.log.title"/></title>
</head>

<body>

<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>

</div>

<div class="content">

    <g:render template="/shared/infoMessage" model="[infoMessage: infoMessage]"/>

    <div class="create_form" id="createLifeCycle"></div>

    <h1><g:message code="lifecycle.log.title"/></h1>


    <div class="list" id="lifeCycleLogTable">
        <g:if test="${logEntries?.isEmpty()}">
            <g:message code="lifecycle.log.none"/>
        </g:if>
        <g:else>
            <div id="logTable">
                <g:render template="logTable" model="[logEntries: logEntries, pagination:pagination]"/>
            </div>
        </g:else>
    </div>
</div>


</body></html>
