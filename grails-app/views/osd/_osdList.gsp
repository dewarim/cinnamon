<table>
    <tr>
        <th><g:message code="osd.id"/></th>
        <th><g:message code="osd.name"/></th>
        <th><g:message code="osd.version"/></th>
        <th><g:message code="osd.format"/></th>
        <th><g:message code="osd.type"/></th>
        <th><g:message code="osd.size"/></th>
        <th><g:message code="osd.owner"/></th>
        <th><g:message code="osd.lockedBy"/></th>
        <g:if test="${selectedVersion}">
            <th>---</th>
        </g:if>
    </tr>
    <g:each in="${osdList}" var="osd" status="i">
        <tr id="osd_row_${osd.id}" class="osd_row ${(i % 2) == 0 ? 'even' : 'odd'}">
            <td>
                <span id="fetchDetailsLink_${osd.id}" class="fetchDetailsLink">
                    <g:remoteLink action="fetchObjectDetails"
                                  controller="osd"
                                  update="[success:'objectDetails', failure:'message']"
                                  onSuccess="setOsdActive('osd_row_${osd.id}', ${i});"
                                  onLoading="showSpinner('fetchDetailsLink_${osd.id}');"
                                  onLoaded="hideSpinner('fetchDetailsLink_${osd.id}');"
                                  onFailure="hideSpinner('fetchDetailsLink_${osd.id}');showClearButton();"
                                  params="[osd:osd.id]">
                        ${osd.id}
                    </g:remoteLink>
                </span>
                <g:if test="${osd.id.toString().equals(triggerOsd)}">
                    <script type="text/javascript">
                        var triggerOsd = $("#fetchDetailsLink_${osd.id} a");
                        triggerOsd.trigger('click');
                        $("#osd_row_${osd.id}").addClass('trigger_osd');

                    </script>
                </g:if>
            </td>
            <td>${osd.name}</td>
            <td class="center">${osd.version}</td>
            <td>${osd.format?.name ?: ''}</td>
            <td>${osd.type.name}</td>
            <td>${osd.contentSize ?: ''}</td>
            <td>${osd.owner?.name ?: ''}</td>
            <td>
                <g:render template="/folder/lockStatus" model="[osd:osd, superuserStatus:superuserStatus]"/>
            </td>
            <g:if test="${selectedVersion}">
                <td>
                    <g:remoteLink controller="osd" action="newVersion"
                                  update="[success:'folderContent', failure:'message']"
                                  params="[osd:osd.id, versions:selectedVersion]"><g:message code="osd.create.version"/>
                    </g:remoteLink>
                </td>
            </g:if>
            <td id="addToSelection_${osd.id}">
                <a href="#" class="addToSelectionLink" onclick="addToSelection(${osd.id}, '${osd.name.encodeAsHTML()}');
                $('#addToSelection_${osd.id}').hide();
                return false;">
                    <g:message code="osd.select"/></a>
            </td>
        </tr>

    </g:each>
    <tr>
        <td colspan="8">&nbsp;</td>
        <td style="text-align: right;"><a href="#" onclick="$('.addToSelectionLink').each(function(index) {
            $(this).click();
        });
        return false;"><g:message code="select.all"/></a></td>
    </tr>
</table>