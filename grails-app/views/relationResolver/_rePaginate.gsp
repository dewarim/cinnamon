<%@ page import="cinnamon.relation.RelationResolver" %>
<util:remotePaginate controller="relationResolver" action="updateList" total="${RelationResolver.count()}"
                     update="relationResolverTable" max="100" pageSizes="[100, 250, 500, 1000]"/>