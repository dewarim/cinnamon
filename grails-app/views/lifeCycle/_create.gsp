<g:form action="save">
    <br>
    <g:if test="${errorMessage}">
        <p id="errorMessage" class="error_message">
            <g:message code="${errorMessage}"/>
        </p>
    </g:if>
    <table>

        <g:render template="fields" model="[lifeCycle:lifeCycle, states:states, defaultStates:defaultStates]"/>
        <script type="text/javascript">
            $('#name_${lifeCycle?.id}').focus();
        </script>
        <td>
            <g:submitToRemote url="[action:'save', controller:'lifeCycle']"
                              update="[success:'lifeCycleTable', failure:'createLifeCycle']"
                              value="${g.message(code:'save')}"
                              onSuccess="\$('#infoMessage').text('${g.message(code:'create.success')}');\$('#createLifeCycle').text('');"
                              onSubmit="\$('#infoMessage').text('');\$('#errorMessage').text('');"/>
        </td>
    </table>
</g:form>