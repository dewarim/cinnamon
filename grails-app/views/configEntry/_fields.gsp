<g:set var="cid" value="${configEntry?.id}"/>
<td><label for="name_${cid}"><g:message code="configEntry.name"/></label></td>
<td valign="top" class="value">
	<input type="text" name="name" id="name_${cid}" value="${configEntry?.name}"/>
</td>

<g:render template="editConfig" model="[configEntry:configEntry]"/>