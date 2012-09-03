<td>${transformer.id}</td>
<td colspan="5">
	<g:form action="update">
		 <g:if test="${errorMessage}">
			 <p class="error_message">
				 <g:message code="${errorMessage}"/>
			 </p>
		 </g:if>
		<input type="hidden" name="id" value="${transformer?.id}"/>

		<table>

			<g:render template="fields" model="[transformer:transformer, transformers:transformers]"/>

			<td>

				<g:submitToRemote url="[action:'update', controller:'transformer	']"
						update="[success:'transformer_'+transformer.id, failure:'transformer_'+transformer.id]"
						value="${g.message(code:'save')}"/>
			</td>
		</table>
	</g:form>
</td>
<td>
	<g:form>
		<input type="hidden" name="id" value="${transformer.id}">
		<g:submitToRemote url="[action:'cancelEdit', controller:'transformer']"
				update="[success:'transformer_'+transformer.id, failure:'message']"
				value="${g.message(code:'cancel')}"/>
	</g:form>

</td>