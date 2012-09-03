<%@ page import="cinnamon.trigger.ChangeTriggerType" %>
<util:remotePaginate controller="changeTriggerType" action="updateList" total="${ChangeTriggerType.count()}"
                     update="changeTriggerTypeList" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>