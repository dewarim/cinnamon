<%@ page import="cinnamon.ConfigEntry" %>
<util:remotePaginate controller="configEntry" action="updateList" total="${ConfigEntry.count()}"
                     update="configEntryList" max="100" pageSizes="[100, 250, 500, 1000]"/>