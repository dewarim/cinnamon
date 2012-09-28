<h2><g:message code="selection.h"/></h2>
<g:form name="selectionForm" controller="osd" action="iterate">
    <input type="hidden" id="selectedFolder" name="selectedFolder" value=""/>
    <h3><g:message code="select.objects"/></h3>
    <div id="selectionOsd">
    </div>
    <a href="#" onclick="$('.deselectObject').each(function(index){$(this).click();});return false;"><g:message code="select.deselect.all.objects"/></a>
    <h3><g:message code="select.folders"/></h3>
    <div id="selectionFolder">
    </div>
    <a href="#" onclick="$('.deselectFolder').each(function(index){$(this).click();});return false;"><g:message code="select.deselect.all.folders"/></a>
    <br>
    <label for="versions">
        <g:message code="osd.target.versions"/>
    </label>
    <g:select name="versions" from="${cinnamon.VersionType.values()}" optionKey="${{it.name()}}" optionValue="${{message(code:it.name)}}"/>
    <br>
    
    <g:submitButton class="buttons" name="delete" value="${message(code:'osd.delete')}"/>
    <g:submitButton disabled="disabled"  class="buttons" id="moveHere" name="move" value="${message(code:'osd.move.into.unknown')}"/>
    <g:submitButton disabled="disabled" class="buttons" id="copyHere" name="copy" value="${message(code: 'osd.copy.into.unknown')}"/>
    
</g:form>
<script type="text/javascript">
    
</script>
