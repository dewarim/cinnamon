<g:if test="${acl.aclEntries.size() > 0}">
    <g:form action="show" controller="aclEntry">
        <label for="visitAclEntryId"><g:message code="acl.manage.aclEntries"/></label> <br>
        <select id="visitAclEntryId" name="id">
            <g:each in="${acl.aclEntries}" var="aclEntry">
                <option value="${aclEntry.id}">${aclEntry.group.name}</option>
            </g:each>
        </select>
        <g:submitButton name="gotoAclEntries" value="${g.message(code:'acl.visit.aclEntry')}"/>
    </g:form>
</g:if>
