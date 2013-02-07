<div class="osd_content">
    <g:if test="${ctype =~ '^image/.*'}">
        <img src="<g:createLink controller="image" action="imageLoader" id="${osd.id}"/>" alt="${osd.name}">
    </g:if>
    <g:elseif test="${ctype =~ 'xml'}">
        <pre>${osdContent}</pre>
    </g:elseif>
    <g:elseif test="${ctype =~ '^text/plain'}">
        <pre>${osdContent}</pre>
    </g:elseif>
    <g:else>
        <g:message code="osd.no.renderer" args="[ctype]"/>
    </g:else>
</div>
