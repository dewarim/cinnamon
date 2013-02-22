<div class="rendered_xml">
<g:form name="nullForm_${renderId}">
	<g:textArea id="render_xml_${renderId}" class="xml-textarea" name="render_xml" rows="6" cols="40"><%= xml %></g:textArea>
	<script type="text/javascript">
        createEditor($('#render_xml_${renderId}').get(0), false);
	</script>
</g:form>