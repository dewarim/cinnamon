<td>
    <span id="fetchDetailsLink_${osd.id}" class="fetchDetailsLink">
        <g:remoteLink action="fetchObjectDetails"
                      controller="osd"
                      update="[success: 'objectDetails', failure: 'message']"
                      onSuccess="setOsdActive('osd_row_${osd.id}', ${i});"
                      onLoading="showSpinner('fetchDetailsLink_${osd.id}');"
                      onLoaded="hideSpinner('fetchDetailsLink_${osd.id}');"
                      onFailure="hideSpinner('fetchDetailsLink_${osd.id}');showClearButton();"
                      params="[osd: osd.id]">
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
<td class="center">${osd.cmnVersion}</td>
<td>${osd.format?.name ?: ''}</td>
<td>${osd.type.name}</td>
<td>${osd.contentSize ?: ''}</td>
<td>${osd.owner?.name ?: ''}</td>
<td id="osd_row_lock_${osd.id}">
    <g:render template="/folder/lockStatus" model="[osd: osd, superuserStatus: superuserStatus, user:user]"/>
</td>
<g:if test="${selectedVersion}">
    <td>
        <g:remoteLink controller="osd" action="newVersion"
                      update="[success: 'folderContent', failure: 'message']"
                      params="[osd: osd.id, versions: selectedVersion]"><g:message code="osd.create.version"/>
        </g:remoteLink>
    </td>
</g:if>
<td id="addToSelection_${osd.id}">
    <a href="#" class="addToSelectionLink" onclick="addToSelection(${osd.id}, '${osd.name.encodeAsHTML()}');
    $('#addToSelection_${osd.id}').hide();
    return false;">
        <g:message code="osd.select"/></a>
</td>