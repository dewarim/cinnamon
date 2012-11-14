<BooleanQuery minimumNumberShouldMatch="1">
    <Clause occurs="should">
        <g:if test="${query?.matches('^\\w+$')}">
            <WildcardQuery fieldName="name">${query.toLowerCase()}*</WildcardQuery>
        </g:if>
        <g:else>
            <TermQuery fieldName="name">${query?.toLowerCase()}</TermQuery>
        </g:else>
    </Clause>
    <Clause occurs="should">
        <g:if test="${query?.matches('^\\w+$')}">
            <WildcardQuery fieldName="content">${query.toLowerCase()}*</WildcardQuery>
        </g:if>
        <g:else>
            <TermQuery fieldName="content">${query?.toLowerCase()}</TermQuery>
        </g:else>
    </Clause>
</BooleanQuery>