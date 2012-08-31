<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="admin.title"/></title>
</head>

<body>
<div class="nav">
    <g:homeButton><g:message code="home"/></g:homeButton>
    <span class="menuButton"><g:message code="link.to.administration"/></span>
</div>

<div class="content">

    <h1 style="margin-left:20px;"><g:message code="admin.title"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:if test="${flash.error}">
        <div class="errors">
            <ul class="errors">
                <li class="errors">${flash.error}</li>
            </ul>
        </div>
    </g:if>

    <h2><g:message code="admin.server.objects"/></h2>

    <!-- // TODO: implement all admin controllers -->

    <div class="object_controllers main_index">
        <h3><g:message code="server.objects"/></h3>
        <ul>

            <li class="controller"><g:link controller="format"><g:message
                    code="index.manage.format"/></g:link></li>
            <li class="controller">
                <g:link controller="folderType" action="list"><g:message
                        code="index.manage.folderTypes"/></g:link>
            </li>

            <li class="controller"><g:link controller="objectType"><g:message
                    code="index.manage.objectType"/></g:link></li>

            <li class="controller"><g:link controller="relationType"><g:message
                    code="index.manage.relationType"/></g:link></li>
            <li class="controller"><g:link controller="changeTrigger" action="index"><g:message
                    code="index.manage.changeTrigger"/></g:link></li>
            <li class="controller"><g:link controller="changeTriggerType" action="index"><g:message
                    code="index.manage.changeTriggerType"/></g:link></li>
            <li class="controller"><g:link controller="relationResolver" action="index"><g:message
                    code="index.manage.relationResolver"/></g:link></li>
            <li class="controller"><g:link controller="transformer" action="index"><g:message
                    code="index.manage.transformer"/></g:link></li>
            <li class="controller"><g:link controller="metasetType" action="index"><g:message
                    code="index.manage.metasetType"/></g:link></li>
        </ul>
    </div>

    <div class="permission_controllers main_index">
        <h3><g:message code="server.rights"/></h3>
        <ul>
            <li class="controller"><g:link controller="acl"><g:message code="index.manage.acl"/></g:link></li>
            <li class="controller"><g:link controller="userAccount" action="list"><g:message
                    code="index.manage.user"/></g:link></li>
            <li class="controller"><g:link controller="aclEntry"><g:message
                    code="index.manage.aclEntry"/></g:link></li>
            <li class="controller"><g:link controller="group" action="list"><g:message
                    code="index.manage.group"/></g:link></li>

        </ul>
    </div>

    <div class="index_controllers main_index">
        <h3><g:message code="server.indexing"/></h3>
        <ul>
            <li class="controller"><g:link controller="indexType"><g:message
                    code="index.manage.indexType"/></g:link></li>
            <li class="controller"><g:link controller="indexItem"><g:message
                    code="index.manage.indexItem"/></g:link></li>
            <li class="controller"><g:link controller="indexGroup"><g:message
                    code="index.manage.indexGroup"/></g:link></li>
        </ul>
    </div>

    <div class="i18n_controllers main_index">
        <h3><g:message code="server.i18n"/></h3>
        <ul>
            <li class="controller"><g:link controller="uiLanguage" action="list"><g:message
                    code="index.manage.uiLanguage"/></g:link></li>
            <li class="controller"><g:link controller="language" action="list"><g:message
                    code="index.manage.language"/></g:link></li>
            <li class="controller"><g:link controller="message" action="list"><g:message
                    code="index.manage.message"/></g:link></li>
        </ul>
    </div>

    <div class="workflow_controllers main_index">
        <h3><g:message code="server.workflow_lifecycle"/></h3>
        <ul>
            <li class="controller"><g:link controller="lifeCycle">
                <g:message code="index.manage.lifeCycle"/></g:link></li>
            <li class="controller"><g:link controller="lifeCycleState">
                <g:message code="index.manage.lifeCycleState"/></g:link></li>
            <li class="controller"><g:link controller="lifecycleLog">
                <g:message code="index.manage.lifecycleLog"/></g:link></li>
        </ul>
    </div>

    <div class="config_controllers main_index">
        <h3><g:message code="server.configuration"/></h3>
        <ul>
            <li class="controller"><g:link controller="health"><g:message
                    code="index.manage.health"/></g:link></li>
            <li class="controller"><g:link controller="configEntry"><g:message
                    code="index.manage.configEntry"/></g:link></li>
        </ul>
    </div>

    <div class="custom_admin_controllers main_index">       
        <h3><g:message code="server.custom.admin"/></h3>        
        <g:if test="${grailsApplication.config.customAdminController}">
        <%-- Custom the customAdminController should generate HTML 
             code which lists application specific administration features. --%>
                    <div id="customAdminControllers"> </div>
            <script type="text/javascript">
                $('#customAdminControllers').load("${
                createLink(action: grailsApplication.config.customAdminController, 
                controller: grailsApplication.config.customAdminAction)}")
            </script>
        </g:if>
        <g:else>
            <g:message code="server.no.custom.admin.controller"/>
        </g:else>
    </div>
    
</div>

</body>
</html>
