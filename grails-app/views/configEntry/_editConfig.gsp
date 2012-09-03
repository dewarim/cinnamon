<tr class="xml_editor_row">
	<td>
		<label for="config_${configEntry?.id}"><g:message code="configEntry.config"/></label>
	</td>
	<td class="value xml_editor">
		<textarea id="config_${configEntry?.id}" style="width:100ex;border:1px black solid; " name="config" cols="120"
				  rows="10">${configEntry?.config ? configEntry.config.encodeAsHTML() : '<config />'.encodeAsHTML()}</textarea>
		<script type="text/javascript">
			createEditor($('#config_${configEntry?.id}').get(0))
		</script>
	</td>
</tr>