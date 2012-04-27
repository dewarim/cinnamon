<g:if test="${leftRelations}">
    <h3><g:message code="osd.relation.outward"/></h3>
    <table class="relation_table" border="1">
        <tr>
            <th><g:message code="osd.relation.type"/></th>
            <th><g:message code="osd.relation.name"/></th>
            <th><g:message code="osd.relation.object_type"/></th>
            <th><g:message code="osd.relation.version"/></th>
            <th><g:message code="relation.metadata"/></th>
        </tr>
        <g:each in="${leftRelations}" var="relation" status="i">
            <tr class="relation_row ${(i % 2) == 0 ? 'even' : 'odd'}">
                <td><a href="#" onclick="showRelationType(${relation.type.id});">
                    <g:message code="${relation.type.name}"/>
                </a>
                </td>
                <td>
                    <span id="fetchRelationLink_${osd.id}" class="fetchDetailsLink">
                        <g:link action="index"
                                controller="folder"
                                params="[folder:relation.rightOSD.parent.id, osd:relation.rightOSD.id]">
                            ${relation.rightOSD.name ?: message(code: 'osd.no.name')}
                        </g:link>
                    </span>
                </td>
                <td><g:message code="${relation.rightOSD.type.name}"/></td>
                <td><g:message code="${relation.rightOSD.version}"/></td>
                <td>
                    <div class="relationMetadata">
                        ${relation.metadata.encodeAsHTML()}
                    </div>
                </td>
            </tr>
        </g:each>
    </table>
</g:if>
<g:if test="${rightRelations}">
    <h3><g:message code="osd.relation.incoming"/></h3>
    <table class="relation_table" border="1">
        <tr>
            <th><g:message code="osd.relation.type"/></th>
            <th><g:message code="osd.relation.name"/></th>
            <th><g:message code="osd.relation.object_type"/></th>
            <th><g:message code="osd.relation.version"/></th>
            <th><g:message code="relation.metadata"/></th>
        </tr>
        <g:each in="${rightRelations}" var="relation" status="i">
            <tr class="relation_row ${(i % 2) == 0 ? 'even' : 'odd'}">
                <td><g:message code="${relation.type.name}"/></td>
                <td><span id="fetchRelationLink_${osd.id}" class="fetchDetailsLink">
                    <span id="fetchRelationLink_${osd.id}" class="fetchDetailsLink">
                        <g:link action="index"
                                controller="folder"
                                params="[folder:relation.leftOSD.parent.id, osd:relation.leftOSD.id]">
                            ${relation.leftOSD.name ?: message(code: 'osd.no.name')}
                        </g:link>
                    </span>
                </span>
                </td>
                <td><g:message code="${relation.leftOSD.type.name}"/></td>
                <td><g:message code="${relation.leftOSD.version}"/></td>
                <td>
                    <div class="relationMetadata">
                        ${relation.metadata.encodeAsHTML()}
                    </div>
                </td>
            </tr>
        </g:each>
    </table>
</g:if>