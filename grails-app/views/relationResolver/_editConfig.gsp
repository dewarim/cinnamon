<td class="value xml_editor">
	<label for="config_${relationResolver?.id}"><g:message code="relationResolver.config"/></label>
	<textarea id="config_${relationResolver?.id}" style="width:100ex;border:1px black solid; " name="config" cols="120"
			  rows="10">${relationResolver?.config ? relationResolver.config.encodeAsHTML() : '<config />'.encodeAsHTML()}</textarea>
	<script type="text/javascript">
		createEditor($('#config_${relationResolver?.id}').get(0))
	</script>
</td>
