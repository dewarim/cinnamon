<%@ page import="cinnamon.lifecycle.LifeCycleState" %>
<util:remotePaginate controller="lifeCycleState" action="updateList" total="${LifeCycleState.count()}"
                     update="lcsTable" max="100" pageSizes="[100, 250, 500, 1000]"/>