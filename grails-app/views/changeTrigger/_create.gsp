<g:form action="save">
    <br>
    <g:if test="${errorMessage}">
        <p class="error_message">
            <g:message code="${errorMessage}"/>
        </p>
    </g:if>
    <table>

        <g:render template="fields" model="[changeTrigger:changeTrigger]"/>
        <script type="text/javascript">
            $('#command_${changeTrigger?.id}').focus()
        </script>
        <tr>
            <td colspan="6" class="right">
                <g:submitToRemote url="[action:'save', controller:'changeTrigger']"
                                  update="[success:'changeTriggerList', failure:'createChangeTrigger']"
                                  value="${g.message(code:'save')}"
                                  before="codeMirrorEditor.toTextArea(\$('#config_${changeTrigger?.id}').get(0));"
                                  onSuccess="\$('#ajaxMessage').text('${g.message(code:'create.success')}');\$('#createChangeTrigger').text('');rePaginate('paginateButtons');"
                                  onSubmit="\$('#ajaxMessage').text('');\$('#errorMessage').text('');"/>
            </td>
        </tr>
    </table>
</g:form>