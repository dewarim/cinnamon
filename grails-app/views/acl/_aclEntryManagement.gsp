<tr class="prop">
	<td>
		<label for="aclEntryId">
			<g:message code="acl.aclEntries"/>
		</label>
	</td>
	<td id="aclEntryList">
		<g:if test="${acl?.aclEntries?.size() > 0}">

			<g:form>
				<input type="hidden" name="id" value="${acl.id}">

				<select id="aclEntryId" name="aclEntryId">
					<g:each in="${acl.aclEntries}" var="aclEntry">
						<option value="${aclEntry.id}">${aclEntry.group.name}</option>
					</g:each>
				</select>
				<g:submitToRemote
						update="[success:'aclEntryManagement', failure:'message']"
						url="[action:'removeAclEntry', controller:'acl']"
						value="${g.message(code:'acl.remove.group')}"/>
			</g:form>
		</g:if>
		<g:else>
			<g:message code="acl.no.aclEntries"/>
		</g:else>
	</td>
</tr>
<tr class="prop">
	<td>
		<label for="groupId">
			<g:message code="acl.groups"/>
		</label>
	</td>
	<td id="freeGroupList">
		<g:if test="${freeGroups?.size() > 0}">

			<g:form>
				<input type="hidden" name="id" value="${acl.id}">
				<g:select name="groupId" from="${freeGroups}" optionKey="id" optionValue="name"/>

				<g:submitToRemote
						update="[success:'aclEntryManagement', failure:'message']"
						url="[action:'addAclEntry', controller:'acl']"
						value="${g.message(code:'acl.add.group')}">
				</g:submitToRemote>
			</g:form>
		</g:if>
		<g:else>
			<g:message code="acl.no.free.groups"/>
		</g:else>
	</td>
</tr>
<tr class="prop">
	<td colspan="2">
		<g:render template="gotoAclEntries" model="[acl:acl]"/>
	</td>
</tr>