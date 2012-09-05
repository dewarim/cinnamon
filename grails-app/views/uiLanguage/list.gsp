<!DOCTYPE HTML>
<html>
<head>
<meta name="layout" content="main"/>

        <title><g:message code="uiLanguage.list.title"/></title>
    </head>
    <body>
    	
 
        <div class="nav">
            <g:homeButton><g:message code="home"/></g:homeButton><g:adminButton/>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="uiLanguage.create"/></g:link></span>
        </div>
        <div class="content">
            <h1><g:message code="uiLanguage.list.title"/></h1>
            
            <g:render template="/shared/message"/>

            <div class="list" id="uiLanguageList">
                <g:render template="uiLanguageList" model="[uiLanguageList:uiLanguageList]"/>
            </div>

        </div>


</body></html>