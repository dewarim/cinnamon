<div id="previewFolderFail-${folderType}"></div>
<g:set var="failId" value="${folderType}-ajax-fail"/>
<div id="${failId}"></div>

<div class="clear">&nbsp;</div>

<div id="uploaded-files-${folderType}">
    <ul style="list-style-image: url('<g:resource dir="images" file="ok.png"/>');"></ul>
</div>
<ul id="content-folder-files-${folderType}" style="list-style-type: none;">
    <li><g:render template="/folder/folderUp" 
                  model="[folder: currentFolder,
                          osd: osd,
                          folderType: folderType, success: folderType + 'FolderSelect', failure: failId]"/>
    <g:message code="editor.current.folder" args="[currentFolder.name]"/>
    </li>
    <g:if test="${folders?.isEmpty()}">
    %{--<li><g:message code="no.sub.folders"/> </li>--}%
    </g:if>
    <g:else>

        <g:each in="${folders}" var="folder">
            <li>
                <g:remoteLink controller="folder" action="loadSelectionFolders"
                              params="[id: folder.id, folderType: folderType]"
                              onSuccess="loadFolderContent('${folder.id}', '${folderType}', '${osd?.id ?: 0}');"
                              update="[success: folderType + 'FolderSelect', failure: failId]">
                    ${folder.name}
                </g:remoteLink>
            </li>
        </g:each>
    </g:else>
</ul>
