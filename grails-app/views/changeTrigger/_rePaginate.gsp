<%@ page import="cinnamon.trigger.ChangeTrigger" %>
<util:remotePaginate controller="changeTrigger" action="updateList" total="${ChangeTrigger.count()}"
                     update="changeTriggerList" max="100" pageSizes="[100, 250, 500, 1000]"/>