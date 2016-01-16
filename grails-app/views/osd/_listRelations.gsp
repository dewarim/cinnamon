<g:if test="${leftRelations}">
    <div id="left-relations-table">
    <h3><g:message code="osd.relation.outward"/></h3>
    <table class="relation_table" border="1">
        <tr>
            <th><g:message code="osd.relation.type"/></th>
            <th><g:message code="osd.relation.name"/></th>
            <th><g:message code="osd.relation.object_type"/></th>
            <th><g:message code="osd.relation.version"/></th>
            <th><g:message code="relation.metadata"/></th>
            <th><g:message code="relation.delete"/></th>
        </tr>
        <g:each in="${leftRelations}" var="relation" status="i">
            <tr class="relation_row ${(i % 2) == 0 ? 'even' : 'odd'}" id="a-relation-${relation.id}">
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
                <td><g:message code="${relation.rightOSD.cmnVersion}"/></td>
                <td>
                    <div class="relationMetadata">
                        ${relation.metadata}
                    </div>
                </td>
                <td>
                    <button class="btn delete-relation item-delete-button left-relations" data-id='${relation.id}'>
                        <g:yesNoIcon ok="false" alt="icon.no" title="relation.delete"/>
                    </button>
                </td>
            </tr>
        </g:each>
    </table>
    </div>
</g:if>
<g:if test="${rightRelations}">
    <div id="right-relations-table">
    <h3><g:message code="osd.relation.incoming"/></h3>
    <table class="relation_table" border="1">
        <tr>
            <th><g:message code="osd.relation.type"/></th>
            <th><g:message code="osd.relation.name"/></th>
            <th><g:message code="osd.relation.object_type"/></th>
            <th><g:message code="osd.relation.version"/></th>
            <th><g:message code="relation.metadata"/></th>
            <th><g:message code="relation.delete"/></th>
        </tr>
        <g:each in="${rightRelations}" var="relation" status="i">
            <tr class="relation_row ${(i % 2) == 0 ? 'even' : 'odd'}" id="b-relation-${relation.id}">
                <td><a href="#" onclick="showRelationType(${relation.type.id});">
                    <g:message code="${relation.type.name}"/>
                </a>
                </td>
                <td><span id="fetchRelationLink_${osd.id}" class="fetchDetailsLink">
                        <g:link action="index"
                                controller="folder"
                                params="[folder:relation.leftOSD.parent.id, osd:relation.leftOSD.id]">
                            ${relation.leftOSD.name ?: message(code: 'osd.no.name')}
                        </g:link>
                    </span>
                </span>
                </td>
                <td><g:message code="${relation.leftOSD.type.name}"/></td>
                <td><g:message code="${relation.leftOSD.cmnVersion}"/></td>
                <td>
                    <div class="relationMetadata">
                        ${relation.metadata}
                    </div>
                </td>
                <td>
                    <button class="btn delete-relation item-delete-button right-relations" data-id='${relation.id}'>
                        <g:yesNoIcon ok="false" alt="icon.no" title="relation.delete"/>
                     </button>
                </td>
            </tr>
        </g:each>
    </table>
    </div>
</g:if>

<div id="relationtype-dialog" class="">
    
</div>
<script type="text/javascript">
    $('.delete-relation').each(function(i,element){
        $(element).one('click', function(){
                    var rel = $(this);
                    var id = rel.attr('data-id');
                    $.ajax({                        
                        url:'<g:createLink controller="relation" action="delete"/>/'+id,
                        success: function(){
                            $('#a-relation-'+id).remove();
                            $('#b-relation-'+id).remove();
                            if($('.right-relations').length < 1){
                                $('#right-relations-table').remove();
                            }
                            if($('.left-relations').length < 1){
                                $('#left-relations-table').remove();
                            }
                        },
                        error:function(jqXHR, textStatus, errorThrown){
                            alert(errorThrown);
                        }
                    })
                }
        )
    });
</script>