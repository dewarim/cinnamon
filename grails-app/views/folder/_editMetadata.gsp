<form id="metadataEditForm" action="">
	<input type="hidden" name="folder" value="${folder.id}">
    <g:if test="${saveMetaError}">
        <div class="error">
            <p><r:img uri="/images/skin/exclamation.png" alt="${message(code:'alt.exclamation')}"/>
                ${saveMetaError}
            </p>
        </div>
    </g:if>
	<textarea style="width:100ex;border:1px black solid;" id="meta_folder_area_${folder.id}" name="metadata" cols="120"
			  rows="20"><%= metadata ?: folder.metadata %></textarea>
	<g:submitToRemote url="[action:'saveMetadata', controller:'folder']" 
                      before="codeMirrorEditor.toTextArea();"
        onSuccess="createEditor(\$('#meta_folder_area_${folder.id}').get(0));"
					  update="[success:'meta_folder_'+folder.id, failure:'message']" value="${message(code:'metadata.save')}"/>
</form>
