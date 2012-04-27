<%@ page import="cinnamon.FolderType" %>
<g:form controller="folder" action="save" method="POST">
	<input type="hidden" name="parent" value="${parent.id}">
	<table>
		<tbody>
		<tr>
			<td>
				<label for="create_name_${parent.id}">
					<g:message code="create.name"/>
				</label>
			</td>
			<td>
					<g:textField name="name" id="create_name_${parent.id}"/>
			</td>
		</tr>
		<tr>
			<td>
				<label for="create_type_${parent.id}">
					<g:message code="folder.type"/>
				</label>
			</td>
			<td>
					<g:select from="${FolderType.list()}" optionKey="id" optionValue="${{message(code:it.name)}}"
							  name="folderType" id="create_type_${parent.id}"/>

			</td>
		</tr>

		<tr>
			<td>
				&nbsp;
			</td>
			<td>
					<g:submitButton name="createSubmit" value="${message(code:'create.folder')}"/>
			</td>
		</tr>
		</tbody>
	</table>
</g:form>