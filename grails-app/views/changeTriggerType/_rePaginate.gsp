<%@ page import="cinnamon.trigger.ChangeTriggerType" %>
<util:remotePaginate controller="changeTriggerType" action="updateList" total="${ChangeTriggerType.count()}"
                     update="changeTriggerTypeList" max="100" pageSizes="[100, 250, 500, 1000]"/>