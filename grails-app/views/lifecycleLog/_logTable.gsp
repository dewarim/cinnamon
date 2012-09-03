<table>
    <thead>
    <tr>
        <th><g:message code="id"/></th>
        <th><g:message code="log.object.id"/></th>
        <th><g:message code="log.folder.path"/></th>
        <th><g:message code="log.osd.name"/></th>
        <th><g:message code="log.username"/></th>
        <th><g:message code="log.user.id"/></th>
        <th><g:message code="log.lifecycle"/> </th>
        <th><g:message code="log.old.state.name"/> </th>
        <th><g:message code="log.old.state.id"/> </th>
        <th><g:message code="log.new.state.name"/> </th>
        <th><g:message code="log.new.state.id"/> </th>
        <th><g:message code="log.created"/></th>
    </tr>
    </thead>
    <g:each in="${logEntries}" var="entry">
        <tr>
            <td>${entry.id}</td>
            <td>${entry.hibernateId}</td>
            <td>${entry.folderPath}</td>
            <td>${entry.name}</td>
            <td>${entry.userName}</td>
            <td>${entry.userId}</td>
            <td>${entry.lifecycleName}</td>
            <td>${entry.oldStateName}</td>
            <td>${entry.oldStateId}</td>
            <td>${entry.newStateName}</td>
            <td>${entry.newStateId}</td>
            <td>
                <g:formatDate date="${entry.dateCreated}" format="yyyy-MM-dd hh:mm:sss"/> 
            </td>
        </tr>
    </g:each>
</table>

<div class="paginateButtons">
    <g:render template="pagination" model="[pagination:pagination]"/>
</div>