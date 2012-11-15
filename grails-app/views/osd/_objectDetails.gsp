<%@ page import="cinnamon.global.PermissionName" %>
<h2><g:message code="osd.h" args="[osd.id.toString(), osd.name?.encodeAsHTML()]"/></h2>

<h3><g:message code="osd.system.meta.h"/></h3>
<a href="#" onclick="$('#objectDetails').html(' ');
return false;" title="${message(code: 'table.hide')}">
    <g:message code="link.close"/>
</a>
<table>
    <tr class="odd">
        <td><g:message code="id"/></td>
        <td>${osd.id}</td>
        <td>&nbsp;</td>
    </tr>
    <tr class="even">
        <td><g:message code="osd.name"/></td>
        <td id="o_name_${osd.id}">${osd.name}</td>
        <td>
            <g:if test="${permissions.contains(PermissionName.WRITE_OBJECT_SYS_METADATA)}">
                <span id="editNameLink_${osd.id}" title="${message(code: 'link.edit.name.title')}">
                    <g:remoteLink action="editName" controller="osd"
                                  params="[osd: osd.id]"
                                  update='[success: "o_name_${osd.id}", failure: "message"]'
                                  onSuccess="\$('#editNameLink_${osd.id}').html(' ');">
                        <g:message code="link.edit"/>
                    </g:remoteLink>
                </span>
            </g:if>
        </td>
    </tr>
    <tr class="odd">
        <td><g:message code="osd.version.number"/></td>
        <td>${osd.cmnVersion}</td>
        <td>&nbsp;</td>
    </tr>
    <tr class="even">
        <td><g:message code="osd.format"/></td>
        <td>${osd.format?.name ?: ''}</td>
        <td>&nbsp;</td>
    </tr>
    <tr class="odd">
        <td><g:message code="osd.type"/></td>
        <td id="o_type_${osd.id}">${osd.type.name}</td>
        <td>
            <g:if test="${permissions.contains(PermissionName.WRITE_OBJECT_SYS_METADATA)}">
                <span id="editTypeLink_${osd.id}" title="${message(code: 'link.edit.objectType.title')}">
                    <g:remoteLink action="editType" controller="osd"
                                  params="[osd: osd.id]"
                                  update='[success: "o_type_${osd.id}", failure: "message"]'
                                  onSuccess="\$('#editTypeLink_${osd.id}').html(' ');">
                        <g:message code="link.edit"/>
                    </g:remoteLink>
                </span>
            </g:if>
        </td>
    </tr>
    <tr class="even">
        <td><g:message code="osd.size"/></td>
        <td>${osd.contentSize ?: ''}</td>
        <td>&nbsp;</td>
    </tr>
    <tr class="odd">
        <td><g:message code="osd.owner"/></td>
        <td id="o_owner_${osd.id}">${osd.owner?.name ?: ''}</td>
        <td>
            <g:if test="${permissions.contains(PermissionName.WRITE_OBJECT_SYS_METADATA)}">
                <span id="editOwnerLink_${osd.id}" title="${message(code: 'link.edit.owner.title')}">
                    <g:remoteLink action="editOwner" controller="osd"
                                  params="[osd: osd.id]"
                                  update='[success: "o_owner_${osd.id}", failure: "message"]'
                                  onSuccess="\$('#editOwnerLink_${osd.id}').html(' ');">
                        <g:message code="link.edit"/>
                    </g:remoteLink>
                </span>
            </g:if>
        </td>
    </tr>
    <tr class="even">
        <td><g:message code="osd.lockedBy"/></td>
        <td>${osd.locker?.name ?: ''}</td>
        <td>&nbsp;</td>
    </tr>
    <tr class="odd">
        <td><g:message code="osd.language"/></td>
        <td id="o_language_${osd.id}">${osd.language.isoCode}</td>
        <td>
            <g:if test="${permissions.contains(PermissionName.WRITE_OBJECT_SYS_METADATA)}">
                <span id="editLanguageLink_${osd.id}" title="${message(code: 'link.edit.language.title')}">
                    <g:remoteLink action="editLanguage" controller="osd"
                                  params="[osd: osd.id]"
                                  update='[success: "o_language_${osd.id}", failure: "message"]'
                                  onSuccess="\$('#editLanguageLink_${osd.id}').html(' ');">
                        <g:message code="link.edit"/>
                    </g:remoteLink>
                </span>
            </g:if>
        </td>
    </tr>
    <tr class="even">
        <td><g:message code="osd.created"/></td>
        <td><g:formatDate format="yyyy-MM-dd HH:mm:ss" date="${osd.created}"/>&nbsp;&nbsp;(${osd.creator.name})</td>
        <td>&nbsp;</td>
    </tr>
    <tr class="odd">
        <td><g:message code="osd.modified"/></td>
        <td><g:formatDate format="yyyy-MM-dd HH:mm:ss" date="${osd.modified}"/>&nbsp;&nbsp;(${osd.modifier.name})</td>
        <td>&nbsp;</td>
    </tr>
    <tr class="even">
        <td><g:message code="osd.acl"/></td>
        <td id="o_acl_${osd.id}">${osd.acl.name}</td>
        <td>
            <g:if test="${permissions.contains(PermissionName.SET_ACL)}">
                <span id="editAclLink_${osd.id}" title="${message(code: 'link.edit.acl.title')}">
                    <g:remoteLink action="editAcl" controller="osd"
                                  params="[osd: osd.id]"
                                  update='[success: "o_acl_${osd.id}", failure: "message"]'
                                  onSuccess="\$('#editAclLink_${osd.id}').html(' ');">
                        <g:message code="link.edit"/>
                    </g:remoteLink>
                </span>
            </g:if>
        </td>
    </tr>
    <tr class="odd">
        <td><g:message code="osd.version.status"/></td>
        <td>
            <g:if test="${osd.latestHead}">
                <g:message code="osd.is.latestHead"/>
            </g:if>
            <g:else>
                <g:message code="osd.not.latestHead"/>
            </g:else>
            <br>
            <g:if test="${osd.latestBranch}">
                <g:message code="osd.is.latestBranch"/>
            </g:if>
            <g:else>
                <g:message code="osd.not.latestBranch"/>
            </g:else>
        </td>
    </tr>

</table>

<h2><g:message code="osd.content.h"/></h2>

<div id="objectPreview" class="object_preview">
    <g:if test="${osd.contentSize != null && osd.contentSize > 0}">
        <span id="objectPreviewLink_${osd.id}">
            <g:remoteLink action="renderPreview" controller="osd"
                          params="[osd: osd.id]" update="[success: 'objectPreview', failure: 'message']"
                          onLoading="showSpinner('objectPreviewLink_${osd.id}');"
                          onLoaded="hideSpinner('objectPreviewLink_${osd.id}');"
                          onFailure="hideSpinner('objectPreviewLink_${osd.id}');showClearButton();">
                <g:message code="folder.render.preview"/>
            </g:remoteLink>
        </span>
    </g:if>
    <g:else>
        <g:message code="osd.no.content"/>
    </g:else>
</div>

<p>
    <g:link controller="osd" action="setContent" params="[osd: osd.id]" class="setContentLink">
        <g:message code="osd.setContent.link"/>
    </g:link>
    <g:if test="${osd.contentSize > 0}">
        &nbsp;|&nbsp;
        <g:link controller="osd" action="getContent" params="[osd: osd.id, folder: osd.parent.id]"
                class="getContentLink">
            <g:message code="osd.getContent.link"/>
        </g:link>
    </g:if>
</p>

<div class="metadata">
    <h2><g:message code="osd.metadata"/></h2>
    <span id="metadataSaveMessage"></span>
    <span id="editMetadataLink"></span>

    <div id="metadataView_${osd.id}" class="metadata_view">
        <g:if test="${osd.fetchMetasets().size() > 0}">
            <span id="renderMetadata_${osd.id}">
                <g:remoteLink action="renderMetadata" controller="osd" params="[osd: osd.id]"
                              update="[success: 'metadataView_' + osd.id, failure: 'message']"
                              onLoading="showSpinner('metadataView_${osd.id}');"
                              onLoaded="hideSpinner('metadataView_${osd.id}');">
                    <g:message code="folder.render.metadata"/>
                </g:remoteLink>
            </span>
        </g:if>
        <g:else>
            <g:remoteLink action="editMetadata" controller="osd"
                          params="[osd: osd.id]" update="[success: 'metadataView_' + osd.id, failure: 'message']"
                          onSuccess="createEditor( \$('#metadata_area_${osd.id}').get(0) ); ">
                <g:message code="metadata.edit"/>
            </g:remoteLink>

        </g:else>
    </div>
</div>

<div id="relations">
    <h2><g:message code="osd.relations"/></h2>

    <div id="relationList" class="relation_list">
        <g:if test="${hasRelations}">
            <g:remoteLink action="listRelations" controller="osd" params="[osd: osd.id]"
                          update="[success: 'relationList', failure: 'message']">
                <g:message code="osd.relations.link"/>
            </g:remoteLink>
        </g:if>
        <g:else>
            <g:message code="osd.no.relations"/>
        </g:else>
    </div>
</div>