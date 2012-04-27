<form id="metadataEditForm" action="">
	<input type="hidden" name="osd" value="${osd.id}">
	<textarea style="width:100ex;border:1px black solid;" id="metadata_area_${osd.id}" name="metadata" cols="120"
			  rows="20">${osd.metadata.encodeAsHTML()}</textarea>
	<g:submitToRemote url="[action:'saveMetadata', controller:'osd']" before="codeMirrorEditor.toTextArea();"
					  update="[success:'objectDetails', failure:'message']"
                      name="submit" value="${message(code:'metadata.save')}"/>
</form>
