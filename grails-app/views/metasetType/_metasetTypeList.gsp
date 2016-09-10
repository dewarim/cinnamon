<%@ page import="cinnamon.MetasetType" %>
<table>
    <thead>
    <tr>

        <th><g:message code="id"/></th>

        <g:sortableColumn property="config"
                          title="${message(code: 'metasetType.config.label', default: 'Config')}"/>

        <g:sortableColumn property="name" title="${message(code: 'metasetType.name.label', default: 'Name')}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${metasetTypeInstanceList}" status="i" var="metasetTypeInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show"
                        id="${metasetTypeInstance.id}">${fieldValue(bean: metasetTypeInstance, field: "id")}</g:link></td>

            <td>${fieldValue(bean: metasetTypeInstance, field: "config")}</td>

            <td>${fieldValue(bean: metasetTypeInstance, field: "name")}</td>

        </tr>
    </g:each>
    </tbody>
</table>


<div class="paginateButtons">
    <util:remotePaginate controller="metasetType" action="updateList" total="${MetasetType.count()}"
                         update="metasetTypeList" max="100" pageSizes="[100, 250, 500, 1000]"/>
</div>