<div class="fieldcontain ${hasErrors(bean: metasetTypeInstance, field: 'name', 'error')} ">
    <label for="name">
        <g:message code="metasetType.name.label" default="Name" />

    </label>
    <g:textField name="name" value="${metasetTypeInstance?.name}" />
</div>

<div class="fieldcontain ${hasErrors(bean: metasetTypeInstance, field: 'description', 'error')} ">
    <label for="description">
        <g:message code="metasetType.description.label" default="Description" />

    </label>
    <g:textField name="description" value="${metasetTypeInstance?.description}" />
</div>

<div class="fieldcontain ${hasErrors(bean: metasetTypeInstance, field: 'config', 'error')} ">
<label for="config_${metasetTypeInstance?.id}">
    <g:message code="metasetType.config.label" default="Config" />
</label>
        <div class="value xml_editor">
            <textarea id="config_${metasetTypeInstance?.id}" style="width:100ex;border:1px black solid; " name="config" cols="120"
                      rows="10">${metasetTypeInstance.config ? metasetTypeInstance.config.encodeAsHTML() : '<metaset />'.encodeAsHTML()}</textarea>
            <script type="text/javascript">
                createEditor($('#config_${metasetTypeInstance?.id}').get(0))
            </script>
        </div>
</div>



