<g:remoteLink controller="folder" action="loadSelectionFolders"
              params="[id: folder.parent.id, folderType: folderType, osd:osd?.id]"
              onSuccess="loadPreviews('${folder.parent.id}', '${folderType}', '${osd?.id ?: 0}');"
              update="[success: success, failure: failure]"><img src="${resource(dir: 'images/icons', file: 'up.png')}"
                                                                 alt="<g:message code="editor.folder.up"/>"
                                                                 title="<g:message
                                                                         code="editor.folder.up"/>"></g:remoteLink>