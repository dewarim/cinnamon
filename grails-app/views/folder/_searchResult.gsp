<h2><g:message code="search.results"/></h2>
<h3><g:message code="search.results.folders"/></h3>
<g:if test="${folders?.size() > 0}">
    <g:render template="folderList" model="[folders:folders]"/>
</g:if>
<g:else>
    <g:message code="search.no.folders"/>
</g:else>
<h3><g:message code="search.results.objects"/></h3>
<g:if test="${objects?.size() > 0}">
    <g:render template="/osd/osdList" model="[osdList:objects]"/>
</g:if>
<g:else>
    <g:message code="search.no.objects"/>
</g:else>

<script type="text/javascript">
    // remove folderContent
    $('#folderContent').html('');
    $('#msgList').html(' ');
</script>