<%@ page import="cinnamon.lifecycle.LifeCycleState" %>
<util:remotePaginate controller="lifeCycleState" action="updateList" total="${LifeCycleState.count()}"
                     update="lcsTable" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>