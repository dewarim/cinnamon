<table>
    <thead>
    <tr>
        <th><g:message code="folder.id"/></th>
        <th><g:message code="folder.name"/></th>
        <th><g:message code="folder.type"/></th>
        <th><g:message code="folder.owner"/></th>
        <th>&nbsp;</th>
    </tr>
    </thead>
    <tbody>
    <tr>
    <g:each in="${folders}" var="folder" status="i">
        <tr class="folder_row ${(i % 2) == 0 ? 'even' : 'odd'}">
            <td><g:link controller="folder" action="index" params="[folder:folder.id]">${folder.id}</g:link></td>
            <td>${folder.name}</td>
            <td>${folder.type.name}</td>
            <td>${folder.owner.name}</td>
            <td>
                <span class="addToFolderSelection_${folder.id}">
                    <a class="addToFolderSelectionLink" href="#"
                       onclick="addToFolderSelection(${folder.id}, '${folder.name.encodeAsHTML()}');
                       $('.addToFolderSelection_${folder.id}').hide();
                       return false;">
                        <g:message code="folder.select"/>
                    </a>
                </span>
            </td>
        </tr>
    </g:each>
    <tr>
        <td colspan="4">&nbsp;</td>
        <td><a href="#" onclick="$('.addToFolderSelectionLink').each(function(index){$(this).click();});return false;"><g:message code="select.all"/></a></td>
    </tr>
    </tbody>
</table>