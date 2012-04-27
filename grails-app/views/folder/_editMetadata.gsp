<form id="metadataEditForm" action="">
	<input type="hidden" name="folder" value="${folder.id}">
	<textarea style="width:100ex;border:1px black solid;" id="meta_folder_area_${folder.id}" name="metadata" cols="120"
			  rows="20">${folder.metadata.encodeAsHTML()}</textarea>
	<g:submitToRemote url="[action:'saveMetadata', controller:'folder']" before="codeMirrorEditor.toTextArea();"
					  update="[success:'folderMeta', failure:'message']" value="${message(code:'metadata.save')}"/>
</form>
