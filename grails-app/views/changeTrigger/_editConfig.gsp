<tr class="xml_editor_row">
	<td>
		<label for="config_${changeTrigger?.id}"><g:message code="changeTrigger.config"/></label>
	</td>
	<td class="value xml_editor">
		<textarea id="config_${changeTrigger?.id}" style="width:100ex;border:1px black solid; " name="config" cols="120"
				  rows="10">${changeTrigger?.config ? changeTrigger.config : '<config />'}</textarea>
		<script type="text/javascript">
			createEditor($('#config_${changeTrigger?.id}').get(0))
		</script>
	</td>
</tr>