<%@ page import="org.apache.commons.lang.math.RandomUtils" %>
<g:set var="pid" value="${RandomUtils.nextInt()}"/>
<g:form onSubmit="\$('#submit_${pid}').click();return false;" name="filterForm_${pid}">
    <input type="hidden" name="updateId" value="${pagination.updateId}">
    <input type="hidden" name="aliasAction" value="${pagination.aliasAction}">

    <label for="maxBottom_${pid}"><g:message code="paginate.max"/></label>
    <g:select id="maxBottom_${pid}" from="${[50,100,500]}" name="max" value="${pagination.max}"
              onchange="\$('#pageBottom_${pid}').val('1');\$('#submit_${pid}').click();"/>
    <g:if test="${pagination.pageCount > 0}">
        <label for="pageBottom_${pid}"><g:message code="paginate.page"/></label>
        <g:select id="pageBottom_${pid}" from="${(1..(pagination.pageCount))}" name="offset"
                  value="${pagination.offset}" optionKey="${{ (it-1) * pagination.max}}"
                  onchange="\$('#submit_${pid}').click();"/>
    </g:if>

    <label for="filter_${pid}"><g:message code="log.filter"/></label>
    <input id="filter_${pid}" name="filter" type="text" value="${pagination.filter}">

    <g:submitToRemote id="submit_${pid}" url="[controller:'lifecycleLog', action:pagination.remoteAction]"
                      on401="showLogin();"
                      update="[success:pagination.updateId, failure:'message']"
                      value="${message(code:'log.submit.filter')}"/>
</g:form>
<br>
[<g:message code="paginate.itemCount" args="[pagination.itemCount]"/>]