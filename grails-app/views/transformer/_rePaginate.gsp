<%@ page import="cinnamon.transformation.Transformer" %>
<util:remotePaginate controller="transformer" action="updateList" total="${Transformer.count()}"
                     update="transformerTable" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>