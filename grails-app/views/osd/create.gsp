<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="folder.index"/></title>

</head>

<body>
<div class="nav">
	<span class="menuButton"><a class="home" href="${resource(dir: 'folder', file: 'index')}"><g:message
			code="home"/></a></span>
</div>

<div class="body">
    <h1><g:message code="create.osd.head" args="[folder.name]"/></h1>

    	<g:if test="${flash.message}">
            <div class="message" id="message">
                ${flash.message}
            </div>
        </g:if>


<g:render template="objectContentForm" model="[folder:folder, osd:osd, nextAction:'saveObject']"/>

</div>

</body>
</html>
