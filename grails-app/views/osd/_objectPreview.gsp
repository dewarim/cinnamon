<div class="osd_content">
    <g:if test="${ctype =~ '^image/.*'}">
        <img src="<g:resource dir="/osd/imageLoader" file="${ String.valueOf(osd.id)}"/>" alt="${osd.name}">
    </g:if>
    <g:elseif test="${ctype =~ 'xml'}">
        <pre>${osdContent?.encodeAsHTML()}</pre>
    </g:elseif>
    <g:elseif test="${ctype =~ '^text/plain'}">
        <pre>${osdContent?.encodeAsHTML()}</pre>
    </g:elseif>
    <g:else>
        <g:message code="osd.no.format.renderer" args="[ctype]"/>
    </g:else>
</div>
