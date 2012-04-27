<ul style="list-style-type:none;">

    <g:each in="${children}" var="folder">
        <li id="folder_${folder.id}" class="folder_row">
            <g:if test="${ grandChildren.get(folder)}">
                <span id="hideChildren_${folder.id}" style="display:none;">
                    <a href="#" onClick="hideChildren('${folder.id}');
                    return false;">
                        <r:img uri="/images/icons/folder_open.png" height="16" width="16" plugin="humulus"
                               alt="${message(code: 'folder.close.view')}"
                               title="${message(code: 'folder.close.view')}"/>
                    </a>
                </span>
                <span id="fetchLink_${folder.id}">
                    <g:remoteLink action="fetchFolder"
                                  controller="folder"
                                  update='[success:"children_of_${folder.id}", failure:"message"]'
                                  params="[folder:folder.id]"
                                  onSuccess="showHideLink('${folder.id}');"
                                  onLoading="showSpinner('folder_${folder.id}');"
                                  onLoaded="hideSpinner('folder_${folder.id}');"
                                  onFailure="hideSpinner('folder_${folder.id}');showClearButton();">
                        <r:img uri="/images/icons/folder.png"  plugin="humulus" height="16" width="16"
                               alt="${message(code: 'folder.open.view')}"
                               title="${message(code: 'folder.open.view')}"/>
                    </g:remoteLink>
                </span>
                <g:if test="${triggerSet?.contains('fetchLink_'+folder.id)}">
                    <script type="text/javascript">
                        $("#fetchLink_${folder.id} a").trigger('click');
                    </script>
                </g:if>
            </g:if>
            <span id="fetchFolderContentLink_${folder.id}">

                <g:remoteLink action="fetchFolderContent"
                              controller="folder"
                              update='[success:"folderContent", failure:"message"]'
                              params="[folder:folder.id]"
                              onSuccess="setLinkActive('folderName_${folder.id}');"
                              onLoading="showSpinner('folderName_${folder.id}');"
                              onLoaded="hideSpinner('folderName_${folder.id}');"
                              onFailure="hideSpinner('folderName_${folder.id}');showClearButton();">
                    <g:if test="${contentSet.contains(folder) || grandChildren.get(folder)}">
                        <span class="folder_name_content" id="folderName_${folder.id}">${folder.name}</span></g:if>
                    <g:else>
                        <span class="folder_name_no_content"
                              id="folderName_${folder.id}">${folder.name}</span></g:else></g:remoteLink>

            </span>
            <g:if test="${folder.id.toString().equals(triggerFolder)}">
                <script type="text/javascript">
                    $("#fetchFolderContentLink_${folder.id} a").trigger('click');
                </script>
            </g:if>

            <span id="fetchFolderMetaLink_${folder.id}" class="fetchFolderMeta">
                <g:remoteLink action="fetchFolderMeta"
                              controller="folder"
                              update="[success:'folderMeta', failure:'message']"
                              params="[folder:folder.id]"
                              onLoading="showSpinner('fetchFolderMetaLink_${folder.id}');"
                              onLoaded="hideSpinner('fetchFolderMetaLink_${folder.id}');"
                              onFailure="hideSpinner('fetchFolderMetaLink_${folder.id}');showClearButton();">
                    <r:img class="folder_info"
                         uri="/images/skin/information.png"
                         alt="${message(code: 'icon.info.link')}"
                         title="${message(code: 'folder.info.title')}"/>
                </g:remoteLink>
            </span>
            <span class="addToFolderSelection_${folder.id}">
                <a href="#" onclick="addToFolderSelection(${folder.id}, '${folder.name.encodeAsHTML()}');
                $('.addToFolderSelection_${folder.id}').hide();
                return false;">
                    <g:message code="folder.select"/>
                </a>
            </span>

            <div class="grand_children" id="children_of_${folder.id}" style="display:inline;">&nbsp;</div>
        </li>
    </g:each>
</ul>