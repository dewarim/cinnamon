<tr class="xml_editor_row">
	<td>
		<label for="metadata"><g:message code="language.metadata"/></label>
	</td>
	<td class="value xml_editor">
		<textarea id="metadata" style="width:100ex;border:1px black solid; "
                  name="metadata" cols="120"
                  rows="10"><%= language?.metadata ? language.metadata.encodeAsHTML() : '<meta></meta>'.encodeAsHTML() %></textarea>
		<script type="text/javascript">
			createEditor($('#metadata').get(0))
		</script>
	</td>
</tr>