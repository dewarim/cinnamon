<%@ page import="cinnamon.relation.RelationType" %>
<h3><g:message code="relation.create"/> </h3>
<g:form>
    <input type="hidden" name="osd" value="${osd.id}"/>
    <label for="relationType">
        <g:message code="relationType.label"/>
    </label>
    <g:select id="relationType" name="type" from="${RelationType.list()}"
        optionKey="id"
        optionValue="${{message(code:it.name)}}"
    />
    <div id="relationFolderContent">
        <g:render template="/folder/folderContent/relationFolderContent" model="[osds:candidates]"/>
    </div>   
    <div id="relationFolderSelect">
        <g:render template="/folder/contentFolder"
                  model="[folders: folders, currentFolder: osd.parent,
                          folderType: folderType, osd:osd]"/>
    </div>
    <g:submitToRemote url="[controller: 'relation', action: 'save']"
                      value="${message(code: 'relation.save')}"
                      update="[success: 'relationList', failure: 'create-relation-fail']"
                      onSuccess="jQuery('#create-relation-link').show();jQuery('#create-relation').hide();"/>
</g:form>

