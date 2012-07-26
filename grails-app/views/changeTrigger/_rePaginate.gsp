<%@ page import="cinnamon.trigger.ChangeTrigger" %>
<util:remotePaginate controller="changeTrigger" action="updateList" total="${ChangeTrigger.count()}"
                     update="changeTriggerList" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>