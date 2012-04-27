<%@ page import="cinnamon.Format; cinnamon.ObjectType" %>
<g:form controller="osd" action="$nextAction" method="POST" enctype="multipart/form-data" name="$nextAction">
	<input type="hidden" name="folder" value="${folder.id}">
	<g:if test="${osd}">
		<input type="hidden" name="osd" value="${osd.id}">
	</g:if>
	<table>
		<tbody>
		<tr>
			<td>
				<label for="create_name_${folder.id}">
					<g:message code="create.name"/>
				</label>
			</td>
			<td>
				<g:if test="${osd}">
					${osd.name}
				</g:if>
				<g:else>
					<g:textField name="name" id="create_name_${folder.id}"/>
				</g:else>
			</td>
		</tr>
		<tr>
			<td>
				<label for="create_type_${folder.id}">
					<g:message code="objectType"/>
				</label>
			</td>
			<td>
				<g:if test="${osd}">
					${osd.type.name}
				</g:if>
				<g:else>
					<g:select from="${ObjectType.list()}" optionKey="id" optionValue="${{message(code:it.name)}}"
							  name="objectType" id="create_type_${folder.id}"/>
				</g:else>

			</td>
		</tr>
		<tr>
			<td>
				<label for="create_file_${folder.id}">
					<g:message code="file.upload"/>
				</label>
			</td>
			<td>
				<input type="file" name="file" id="create_file_${folder.id}">
			</td>
		</tr>
		<tr>
			<td>
				<label for="create_format_${folder.id}">
					<g:message code="create.format"/>
				</label>
			</td>
			<td>
				<g:select from="${Format.list()}" optionKey="id" optionValue="${{message(code:it.name)}}"
						  name="format" id="create_format_${folder.id}"
						  noSelection="${['0':'---']}" value="${osd?.format?.id}"/>
			</td>
		</tr>
		<tr>
			<td>
				&nbsp;
			</td>
			<td>
				<g:if test="${nextAction?.equals('saveObject')}">
					<g:submitButton name="createSubmit"
									onSuccess="\$('#${nextAction}')[0].reset();"
									value="${message(code:'create.object')}"/>
				</g:if>
				<g:else>
					<g:submitButton name="setContent"
									onSuccess="\$('#${nextAction}')[0].reset();"
									value="${message(code:'osd.setContent')}"/>
				</g:else>
			</td>
		</tr>
		</tbody>
	</table>

</g:form>