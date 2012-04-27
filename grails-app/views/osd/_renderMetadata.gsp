<div class="rendered_metadata">
	<g:form name="nullForm_${osd.id}">
		<g:textArea id="renderText_${osd.id}" name="render_metadata" rows="6" cols="20">${osd.metadata}</g:textArea>
		<script type="text/javascript">
            var uiOptions = { path : '<g:resource dir="js/CodeMirrorUI/" file="js" />/', searchMode: 'popup' };
            var cmOptions = {
						mode:'application/xml',
						readOnly:true
					};
			var renderMirror = new CodeMirrorUI($('#renderText_${osd.id}').get(0), uiOptions, cmOptions);
		</script>
	</g:form>
</div>
<g:remoteLink action="editMetadata" controller="osd"
			  params="[osd:osd.id]" update="[success:'metadataView_'+osd.id, failure:'message']"
			  onSuccess="createEditor( \$('#metadata_area_${osd.id}').get(0) ); ">
	<g:message code="metadata.edit"/>
</g:remoteLink>