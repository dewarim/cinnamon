<g:set var="cid" value="${relationResolver.id}"/>
<td valign="top" class="value ${hasErrors(bean: relationResolver, field: 'name', 'errors')}">
	<label for="name_${cid}"><g:message code="relationResolver.name"/></label> <br>
	<input type="text" name="name" id="name_${cid}" value="${relationResolver?.name}"/>
</td>
<g:render template="editConfig" model="[relationResolver:relationResolver]"/>
<td valign="top" class="value ${hasErrors(bean: relationResolver, field: 'resolverClass', 'errors')}">
	<label for="resolverClass_${cid}"><g:message code="relationResolver.resolverClass"/></label> <br>
     <g:select id="resolverClass_${cid}" name="resolverClass" from="${resolvers}" value="${relationResolver?.resolverClass?.name}" />
</td>
