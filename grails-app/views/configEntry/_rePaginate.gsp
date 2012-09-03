<%@ page import="cinnamon.ConfigEntry" %>
<util:remotePaginate controller="configEntry" action="updateList" total="${ConfigEntry.count()}"
                     update="configEntryList" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>