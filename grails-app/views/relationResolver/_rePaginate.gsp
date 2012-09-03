<%@ page import="cinnamon.relation.RelationResolver" %>
<util:remotePaginate controller="relationResolver" action="updateList" total="${RelationResolver.count()}"
                     update="relationResolverTable" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>