<%@ page import="cinnamon.relation.RelationResolver" %>
<table>
	<tbody>

	<tr class="prop">
		<td class="name"><g:message code="id"/></td>

		<td class="value">${fieldValue(bean: relationType, field: 'id')}</td>

	</tr>

	<tr class="prop">
		<td class="name"><g:message code="relationType.name"/></td>

		<td class="value">${fieldValue(bean: relationType, field: 'name')}</td>

	</tr>

	<tr class="prop">
		<td class="name"><g:message code="relationType.description"/></td>

		<td class="value">${fieldValue(bean: relationType, field: 'description')}</td>

	</tr>

	<tr class="prop">
		<td class="name"><g:message code="relationType.leftobjectprotected"/></td>

		<td class="value">
			<g:if test="${relationType.leftobjectprotected}">
				<r:img uri="/images/ok.png" alt="${message(code: "input.disabled")}"/>
			</g:if>
			<g:else>
                <r:img uri="/images/no.png" alt="${message(code: "input.enabled")}"/>
			</g:else>
		</td>

	</tr>

	<tr class="prop">
		<td class="name"><g:message code="relationType.rightobjectprotected"/></td>

		<td class="value">
			<g:if test="${relationType.rightobjectprotected}">
                <r:img uri="/images/ok.png" alt="${message(code: "input.disabled")}"/>
            </g:if>
            <g:else>
                <r:img uri="/images/no.png" alt="${message(code: "input.enabled")}"/>
			</g:else>
		</td>
	</tr>
		<tr class="prop">
			<td>
				<g:message code="relationType.leftResolver"/>
			</td>
					<td class="value">
						${relationType.leftResolver.name}
					</td>
				</tr>
				<tr class="prop">
					<td>
						<g:message code="relationType.rightResolver"/>
					</td>
					<td class="value">
						${relationType.rightResolver.name}
					</td>
				</tr>


	</tbody>
</table>