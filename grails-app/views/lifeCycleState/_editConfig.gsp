<td class="value xml_editor">
	<label for="config_${lcs?.id}"><g:message code="lcs.config"/></label>
	<textarea id="config_${lcs?.id}" style="width:100ex;border:1px black solid; " name="config" cols="120"
			  rows="10">${lcs?.config ? lcs.config.encodeAsHTML() : '<config />'.encodeAsHTML()}</textarea>
	<script type="text/javascript">
		createEditor($('#config_${lcs?.id}').get(0))
	</script>
</td>