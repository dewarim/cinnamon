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
       <g:render template="/osd/osdListEntry" model="[osd:osd, triggerOsd:triggerOsd,
               selectedVersion:selectedVersion, superuserStatus:superuserStatus, user:user]"/>
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