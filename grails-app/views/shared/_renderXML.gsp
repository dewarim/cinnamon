<div class="rendered_xml" style="height:10ex;">
<g:form name="nullForm_${renderId}">
	<g:textArea id="render_xml_${renderId}" name="render_xml" rows="6" cols="20"><%= xml %></g:textArea>
	<script type="text/javascript">
		var renderMirror = CodeMirror.fromTextArea($('#render_xml_${renderId}').get(0), {
					mode:'application/xml',
					readOnly:true
				});
	</script>
</g:form>