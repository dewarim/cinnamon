<g:form action="save">
    <br>
    <g:if test="${errorMessage}">
        <p class="error_message">
            <g:message code="${errorMessage}"/>
        </p>
    </g:if>
    <table>

        <g:render template="fields" model="[lcs:lcs, copyStates:copyStates, stateClasses:stateClasses]"/>
        <script type="text/javascript">
            $('#name_${lcs?.id}').focus();
        </script>
        <td>
            <g:submitToRemote url="[action:'save', controller:'lifeCycleState']"
                              update="[success:'lcsTable', failure:'createlcs']"
                              value="${g.message(code:'save')}"
                              before="codeMirrorEditor.toTextArea(\$('#config_${lcs?.id}').get(0));"
                              onSuccess="\$('#infoMessage').text('${g.message(code:'create.success')}').show();\$('#createlcs').text('');rePaginate('paginateButtons');"
                              onSubmit="\$('#infoMessage').text('');\$('#errorMessage').text('');"/>
        </td>
    </table>
</g:form>