<CinnamonIdList>
    <accessToken/>
    <g:if test="${ids}">
        <ids>
            <g:each in="${ids}" var="id">
                <id>${id}</id>
            </g:each>
        </ids>
    </g:if>
    <g:else>

    </g:else>
</CinnamonIdList>