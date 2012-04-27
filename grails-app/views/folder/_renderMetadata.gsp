<div class="rendered_metadata">
	<g:form name="nullForm_${folder.id}">
		<g:textArea id="renderText_${folder.id}" name="render_metadata" rows="6" cols="20">${folder.metadata}</g:textArea>
		<script type="text/javascript">
            var uiOptions = { path : '<g:resource dir="js/CodeMirrorUI/" file="js" />/', searchMode: 'popup' };
            var cmOptions = {
						mode:'application/xml',
						readOnly:true
					};
			var renderMirror = new CodeMirrorUI($('#renderText_${folder.id}').get(0), uiOptions, cmOptions);
		</script>
	</g:form>
</div>
<g:remoteLink action="editMetadata" controller="folder"
			  params="[folder:folder.id]" update="[success:'meta_folder_'+folder.id, failure:'message']"
			  onSuccess="createEditor( \$('#meta_folder_area_${folder.id}').get(0) ); ">
	<g:message code="metadata.edit"/>
</g:remoteLink>