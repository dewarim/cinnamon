<%@ page import="cinnamon.transformation.Transformer" %>
<util:remotePaginate controller="transformer" action="updateList" total="${Transformer.count()}"
                     update="transformerTable" max="100" pageSizes="[100, 250, 500, 1000]"/>