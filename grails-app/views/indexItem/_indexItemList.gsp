<%@ page import="cinnamon.index.IndexItem" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code: 'id')}"/>

        <g:sortableColumn property="name" title="${message(code: 'label.name')}"/>

        <g:sortableColumn property="fieldname"
                          title="${message(code: 'indexItem.fieldname.label', default: 'Fieldname')}"/>

        <g:sortableColumn property="forContent"
                          title="${message(code: 'indexItem.forContent.label', default: 'For Content')}"/>

        <g:sortableColumn property="forMetadata"
                          title="${message(code: 'indexItem.forMetadata.label', default: 'For Metadata')}"/>

        <g:sortableColumn property="forSysMeta"
                          title="${message(code: 'indexItem.forSysMeta.label', default: 'For Sys Meta')}"/>

        <th><g:message code="indexItem.indexGroup.label" default="Index Group"/></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${indexItemList}" status="i" var="indexItemInstance">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show"
                        id="${indexItemInstance.id}">${fieldValue(bean: indexItemInstance, field: "id")}</g:link></td>

            <td>${indexItemInstance.name}</td>

            <td>${fieldValue(bean: indexItemInstance, field: "fieldname")}</td>

            <td><g:checkBox name="forContent" disabled="true" value="${indexItemInstance.forContent}"/></td>

            <td><g:checkBox name="forMetadata" disabled="true" value="${indexItemInstance.forMetadata}"/></td>

            <td><g:checkBox name="forSysMeta" disabled="true" value="${indexItemInstance.forSysMeta}"/></td>

            <td>${indexItemInstance.indexGroup?.name}</td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
    <util:remotePaginate controller="indexItem" action="updateList" total="${IndexItem.count()}"
                         update="indexItemList" max="100" pageSizes="[100, 250, 500, 1000]"/>
</div>