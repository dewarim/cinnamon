<%@ page import="cinnamon.Format" %>
<g:set var="tid" value="${transformer?.id}"/>
<td valign="top" class="value">
	<div class="transformer_name ${hasErrors(bean: transformer, field: 'name', 'errors')}">
		<label for="name_${tid}"><g:message code="transformer.name"/></label> <br>
		<input type="text" name="name" id="name_${tid}" value="${transformer?.name}"/>
	</div>
	<div class="transformer_description ${hasErrors(bean: transformer, field: 'description', 'errors')}">
		<label for="description_${tid}"><g:message code="transformer.description"/></label> <br>
		<input type="text" name="description" id="description_${tid}" value="${transformer?.description}"/>
	</div>
	<br>
	<div class="transformer_transformerClass  ${hasErrors(bean: transformer, field: 'transformerClass', 'errors')}">
		<label for="transformerClass_${tid}"><g:message code="transformer.transformerClass"/></label> <br>
        <g:select id="transformerClass_${tid}" name="transformerClass" from="${transformers}" value="${transformer?.transformerClass?.name}" />
	</div>
	<label for="sFormat_${tid}"><g:message code="transformer.sourceFormat"/> </label>
	<g:select from="${Format.list()}" id="sFormat_${tid}" name="sourceFormat" optionKey="id" optionValue="name" value="${transformer?.sourceFormat?.id}"/>
		<label for="tFormat_${tid}"> <g:message code="transformer.targetFormat"/> </label>
	<g:select from="${Format.list()}" id="tFormat_${tid}" name="targetFormat" optionKey="id" optionValue="name" value="${transformer?.sourceFormat?.id}"/>

</td>
