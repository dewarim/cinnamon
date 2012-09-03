<g:form action="save">
    <br>
    <g:if test="${errorMessage}">
        <p class="error_message">
            <g:message code="${errorMessage}"/>
        </p>
    </g:if>
    <table>

    <g:render template="fields" model="[relationResolver:relationResolver, resolvers:resolvers]"/>
    <script type="text/javascript">
        $('#name_${relationResolver?.id}').focus();
    </script>
    <td>
        <g:submitToRemote url="[action:'save', controller:'relationResolver']"
                          update="[success:'relationResolverTable', failure:'createRelationResolver']"
                          before="codeMirrorEditor.toTextArea(\$('#config_${relationResolver?.id}').get(0));"
                          value="${g.message(code:'save')}"
                          onSuccess="\$('#ajaxMessage').text('${g.message(code:'create.success')}');\$('#createRelationResolver').text('');rePaginate('paginateButtons');"
                          onSubmit="\$('#ajaxMessage').text('');\$('#errorMessage').text('');"/>
    </td>
</g:form>