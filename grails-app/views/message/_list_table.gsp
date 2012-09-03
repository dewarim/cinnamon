<%@ page import="cinnamon.i18n.Message" %>
<table>
    <thead>
    <tr>
        <th><strong>${message(code: 'message.message')}</strong></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${distinctMessages}" status="i" var="messageId">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <td><g:link action="show" params="[id:Message.findByMessage(messageId).id]">${messageId.encodeAsHTML()}</g:link></td>
        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
    <util:remotePaginate controller="message" action="updateList" total="${messageCount}"
                         update="messageList" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>
</div>
