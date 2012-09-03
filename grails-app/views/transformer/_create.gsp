<g:form action="save">
    <br>
    <g:if test="${errorMessage}">
        <p class="error_message">
            <g:message code="${errorMessage}"/>
        </p>
    </g:if>
    <table>

        <g:render template="fields" model="[transformer:transformer, transformers:transformers]"/>
        <script type="text/javascript">
            $('#name_${transformer?.id}').focus();
        </script>
        <td>
            <g:submitToRemote url="[action:'save', controller:'transformer']"
                              update="[success:'transformerTable', failure:'createTransformer']"
                              value="${g.message(code:'save')}"
                              onSuccess="\$('#infoMessage').text('${g.message(code:'create.success')}').show();\$('#createTransformer').text('');rePaginate('paginateButtons');"
                              onSubmit="\$('#infoMessage').text('');\$('#errorMessage').text('');"/>
        </td>
    </table>
</g:form>