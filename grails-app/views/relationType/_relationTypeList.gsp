<%@ page import="cinnamon.relation.RelationType" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code:'id')}"/>
        <g:sortableColumn property="name" title="${message(code:'relationType.name')}"/>
        <g:sortableColumn property="leftobjectprotected" title="${message(code:'relationType.leftobjectprotected')}"/>
        <g:sortableColumn property="rightobjectprotected" title="${message(code:'relationType.rightobjectprotected')}"/>
        <g:sortableColumn property="cloneOnLeftCopy" title="${message(code:'relationType.cloneOnLeftCopy')}"/>
        <g:sortableColumn property="cloneOnLeftVersion" title="${message(code:'relationType.cloneOnLeftVersion')}"/>
        <g:sortableColumn property="cloneOnRightCopy" title="${message(code:'relationType.cloneOnRightCopy')}"/>
        <g:sortableColumn property="cloneOnRightVersion" title="${message(code:'relationType.cloneOnRightVersion')}"/>
        <g:sortableColumn property="leftResolver" title="${message(code:'relationType.leftResolver')}"/>
        <g:sortableColumn property="rightResolver" title="${message(code:'relationType.rightResolver')}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${relationTypeList}" status="i" var="relationType">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show"
                        id="${relationType.id}">${fieldValue(bean: relationType, field: 'id')}</g:link></td>

            <td>${fieldValue(bean: relationType, field: 'name')}</td>

            <td class="center">
                <g:enabledDisabledIcon test="${relationType.leftobjectprotected}"/>
            </td>

            <td class="center">
                <g:enabledDisabledIcon test="${relationType.rightobjectprotected}"/>
            </td>

            <td class="center">
                <g:enabledDisabledIcon test="${relationType.cloneOnLeftCopy}"/>
            </td>
            <td class="center">
                <g:enabledDisabledIcon test="${relationType.cloneOnRightCopy}"/>
            </td>
            <td class="center">
                <g:enabledDisabledIcon test="${relationType.cloneOnLeftVersion}"/>
            </td>
            <td class="center">
                <g:enabledDisabledIcon test="${relationType.cloneOnRightVersion}"/>
            </td>
            <td>${relationType.leftResolver.name}</td>
            <td>${relationType.rightResolver.name}</td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
    <util:remotePaginate controller="relationType" action="updateList" total="${RelationType.count()}"
                         update="relationTypeList" max="100" pageSizes="[100, 250, 500, 1000]"/>
</div>