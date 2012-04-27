<%@ page import="cinnamon.global.PermissionName;" %>
<table>
	<tbody>
	<tr>
		<td><g:message code="folder.name"/></td>
		<td id="f_name_${folder.id}">
				${folder.name}
		</td>
		<td class="right">
			<g:if test="${permissions.contains(PermissionName.EDIT_FOLDER)}">
				<span id="editFolderNameLink_${folder.id}" title="${message(code:'link.edit.name.title')}">
				<g:remoteLink action="editName" controller="folder"
						params="[folder:folder.id]"
						update='[success:"f_name_${folder.id}", failure:"message"]'
					onSuccess="\$('#editFolderNameLink_${folder.id}').html(' ');"
				>
					<g:message code="link.edit"/>
				</g:remoteLink>
				</span>
				<g:if test="nameChanged">
					<script type="text/javascript">
						setFolderName(${folder.id}, '${folder.name}');
					</script>
				</g:if>

			</g:if>
			<a href="#" onclick="$('#folderMeta').html(' ');
			return false;" title="${message(code: 'table.hide')}">
				<g:message code="link.close"/>
			</a>
		</td>
	</tr>
	<tr>
		<td><g:message code="folder.id"/></td>
		<td colspan="2">${folder.id}</td>
	</tr>
	<tr>
		<td><g:message code="folder.owner"/></td>
		<td id="f_owner_${folder.id}">
			${folder.owner.name}
		</td>
		<td>
			<g:if test="${permissions.contains(PermissionName.EDIT_FOLDER)}">
			<span id="editFolderOwnerLink_${folder.id}"  title="${message(code:'link.edit.owner.title')}">
				<g:remoteLink action="editOwner" controller="folder"
						params="[folder:folder.id]"
						update='[success:"f_owner_${folder.id}", failure:"message"]'
					onSuccess="\$('#editFolderOwnerLink_${folder.id}').html(' ');"
				>
					<g:message code="link.edit"/>
				</g:remoteLink>
				</span>
			</g:if> 	
		</td>
	</tr>
	<tr>
		<td><g:message code="folder.type"/></td>
		<td id="f_type_${folder.id}">
			<g:message code="${folder.type.name}"/>
		</td>
		<td>
		<g:if test="${permissions.contains(PermissionName.EDIT_FOLDER)}">
			<span id="editFolderTypeLink_${folder.id}"  title="${message(code:'link.edit.folderType.title')}">
				<g:remoteLink action="editType" controller="folder"
						params="[folder:folder.id]"
						update='[success:"f_type_${folder.id}", failure:"message"]'
					onSuccess="\$('#editFolderTypeLink_${folder.id}').html(' ');"
				>
					<g:message code="link.edit"/>
				</g:remoteLink>
				</span>
			</g:if>
		</td>
	</tr>
	<tr>
		<td><g:message code="folder.acl"/></td>
		<td id="f_acl_${folder.id}"><g:message code="${folder.acl.name}"/></td>
		<td>
			<g:if test="${permissions.contains(PermissionName.SET_ACL)}">
			<span id="editFolderAclLink_${folder.id}"  title="${message(code:'link.edit.acl.title')}">
				<g:remoteLink action="editAcl" controller="folder"
						params="[folder:folder.id]"
						update='[success:"f_acl_${folder.id}", failure:"message"]'
					onSuccess="\$('#editFolderAclLink_${folder.id}').html(' ');"
				>
					<g:message code="link.edit"/>
				</g:remoteLink>
				</span>
			</g:if>
		</td>
	</tr>
    <tr>
        <td>
            <g:message code="folder.custom.metadata"/>
        </td>
        <td>
            <div class="metadata">
                <span id="metadataSaveMessage_f"></span>
                <span id="editMetadataLink_f"></span>

                <div id="meta_folder_${folder.id}" class="metadata_view">
                    <g:if test="${folder.metadata.length() > 8}">
                        <span id="renderMetaFolder_${folder.id}">
                            <g:remoteLink action="renderMetadata" controller="folder" params="[folder:folder.id]"
                                          update="[success:'meta_folder_'+folder.id, failure:'message']"
                                          onLoading="showSpinner('meta_folder_${folder.id}');"
                                          onLoaded="hideSpinner('meta_folder_${folder.id}');">
                                <g:message code="folder.render.metadata"/>
                            </g:remoteLink>
                        </span>
                    </g:if>
                    <g:else>
                        <g:remoteLink action="editMetadata" controller="folder"
                                      params="[folder:folder.id]" update="[success:'meta_folder_'+folder.id, failure:'message']"
                                      onSuccess="createEditor( \$('#meta_folder_area_${folder.id}').get(0) ); ">
                            <g:message code="metadata.edit"/>
                        </g:remoteLink>
                    </g:else>
                </div>
            </div>
        </td>

    </tr>
	</tbody>
</table>
