<h2><g:message code="selection.h"/></h2>
<g:form name="selectionForm" controller="osd" action="iterate">
    <h3><g:message code="select.objects"/></h3>
    <div id="selectionOsd">
    </div>
    <a href="#" onclick="$('.deselectObject').each(function(index){$(this).click();});return false;"><g:message code="select.deselect.all.objects"/></a>
    <h3><g:message code="select.folders"/></h3>
    <div id="selectionFolder">
    </div>
    <a href="#" onclick="$('.deselectFolder').each(function(index){$(this).click();});return false;"><g:message code="select.deselect.all.folders"/></a>
    <br>
    <g:submitButton name="delete" value="${message(code:'osd.delete')}"/>
    <g:submitButton name="deleteAll" value="${message(code:'osd.delete.all.versions')}"/>
</g:form>

