<td>${relationResolver.id}</td>
<td colspan="6">
	<g:form action="update">
		 <g:if test="${errorMessage}">
			 <p class="error_message">
				 <g:message code="${errorMessage}"/>
			 </p>
		 </g:if>
		<input type="hidden" name="id" value="${relationResolver?.id}"/>

		<table>

			<g:render template="fields" model="[relationResolver:relationResolver, resolvers:resolvers]"/>

			<td>

				<g:submitToRemote url="[action:'update', controller:'relationResolver']"
					before="codeMirrorEditor.toTextArea(\$('#config_${relationResolver.id}').get(0));"
						update="[success:'relationResolver_'+relationResolver.id, failure:'relationResolver_'+relationResolver.id]"
						value="${g.message(code:'save')}"/>
			</td>
		</table>
	</g:form>
</td>
<td>
	<g:form>
		<input type="hidden" name="id" value="${relationResolver.id}">
		<g:submitToRemote url="[action:'cancelEdit', controller:'relationResolver']"
				update="[success:'relationResolver_'+relationResolver.id, failure:'message']"
				value="${g.message(code:'cancel')}"/>
	</g:form>

</td>