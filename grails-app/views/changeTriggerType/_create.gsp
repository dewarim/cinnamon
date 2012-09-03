<g:form action="save">
    <br>
    <g:if test="${errorMessage}">
        <p class="error_message">
            <g:message code="${errorMessage}"/>
        </p>
    </g:if>
    <table>

        <g:render template="fields" model="[changeTriggerType:changeTriggerType, triggers:triggers]"/>
        <script type="text/javascript">
            $('#name_${changeTriggerType?.id}').focus();
        </script>
        <td>
            <g:submitToRemote url="[action:'save', controller:'changeTriggerType']"
                              update="[success:'changeTriggerTypeList', failure:'createChangeTriggerType']"
                              value="${g.message(code:'save')}"
                              onSuccess="\$('#ajaxMessage').text('${g.message(code:'create.success')}');\$('createChangeTriggerType').text('');rePaginate('paginateButtons');"
                              onSubmit="\$('#ajaxMessage').text('');\$('#errorMessage').text('');"/>
        </td>
    </table>
</g:form>