<%@ page import="cinnamon.global.PermissionName" %>

<h2><g:message code="folder.content.folders"/></h2>
<g:if test="${permissions.contains(PermissionName.CREATE_FOLDER)}">
    <div id="createFolder" class="create_folder"></div>
   <g:remoteLink controller="folder" action="create" params="[parent:folder.id]"
       update="[success:'createFolder', failure:'message']"
   onLoaded="\$('#createFolder').show();">
    <g:message code="link.create.folder" args="[folder.name]"/>
    </g:remoteLink>
    <script type="text/javascript">$('#createFolder').hide();</script>
</g:if>
<br>
<g:if test="${folders.size() > 0 || osdList.size() > 0}">
    <g:link controller="folder" action="zipFolder" params="[folder:folder.id, versions:selectedVersion]" class="zipFolderLink">
        <g:message code="folder.zipFolder.link"/>
    </g:link>
</g:if>

<g:if test="${folders.size() > 0}">
    <g:render template="/folder/folderList" model="[folders:folders]"/>
</g:if>
<g:else>
    <p><g:message code="folder.no.children"/></p>
</g:else>

<br>

<h2><g:message code="folder.content.objects"/></h2>
<div class="object_version_select">
    <g:form action="fetchFolderContent" controller="folder">
        <input type="hidden" name="folder" value="${folder.id}"/>
        <select id="osdVersions" name="versions"
                onchange="${remoteFunction(action:'fetchFolderContent', controller:'folder', id:folder.id, update:[success:'folderContent', failure:'message'], params:'\'versions=\'+document.getElementById(\'osdVersions\').value')}">
            <g:each in="${versions.keySet()}" var="version">
                <g:if test="${version.equals(selectedVersion)}">
                    <option value="${version}" selected><g:message code="${versions.get(version)}"/></option>
                </g:if>
                <g:else>
                    <option value="${version}"><g:message code="${versions.get(version)}"/></option>
                </g:else>
            </g:each>
        </select>
    </g:form>
</div>
<g:if test="${permissions.contains(PermissionName.CREATE_OBJECT)}">
   <g:link controller="osd" action="create" params="[folder:folder.id]">
    <g:message code="link.create.object" args="[folder.name]"/>
    </g:link>
</g:if><br>

<g:if test="${osdList?.size() > 0}">
    <g:render template="/osd/osdList" model="[osdList:osdList, previews:previews, selectedVersion:selectedVersion, superuserStatus:superuserStatus]"/>
</g:if>
<g:else>
    <p>
        <g:message code="folder.no.objects"/>
    </p>
</g:else>

<div class="copy_paste_links">
    <div id="currentSelectedFolder" style="display:none">${folder.id}</div>
</div>

<script type="text/javascript">
	$('#objectDetails').html(' ');
    $('#searchResults').html(' ');
    $('#selectedFolder').val(${folder.id});
    $('#moveHere').val("${message(code: 'osd.move.into', args: [folder.name])}");
    $('#moveHere').removeAttr('disabled');
    $('#copyHere').val("${message(code: 'osd.copy.into', args: [folder.name])}");
    $('#copyHere').removeAttr('disabled');
</script>
