<%@ page import="cinnamon.relation.RelationType" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code:'relationType.id')}"/>
        <g:sortableColumn property="name" title="${message(code:'relationType.name')}"/>
        <g:sortableColumn property="description" title="${message(code:'relationType.description')}"/>
        <g:sortableColumn property="leftobjectprotected" title="${message(code:'relationType.leftobjectprotected')}"/>
        <g:sortableColumn property="rightobjectprotected" title="${message(code:'relationType.rightobjectprotected')}"/>
        <g:sortableColumn property="cloneOnLeftCopy" title="${message(code:'relationType.cloneOnLeftCopy')}"/>
        <g:sortableColumn property="cloneOnRightCopy" title="${message(code:'relationType.cloneOnRightCopy')}"/>
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

            <td>${fieldValue(bean: relationType, field: 'description')}</td>

            <td class="center"><g:if test="${relationType.leftobjectprotected}">
                <img src="<g:resource dir='/images' file='ok.png'/>" alt="<g:message code="input.enabled"/>">
            </g:if>
                <g:else>
                    <img src="<g:resource dir='/images' file='no.png'/>" alt="<g:message code="input.disabled"/>">
                </g:else>
            </td>

            <td class="center">
                <g:if test="${relationType.rightobjectprotected}">
                    <img src="<g:resource dir='/images' file='ok.png'/>" alt="<g:message code="input.enabled"/>">
                </g:if>
                <g:else>
                    <img src="<g:resource dir='/images' file='no.png'/>" alt="<g:message code="input.disabled"/>">
                </g:else>
            </td>

            <td class="center">
                <g:if test="${relationType.cloneOnLeftCopy}">
                    <img src="<g:resource dir='/images' file='ok.png'/>" alt="<g:message code="input.enabled"/>">
                </g:if>
                <g:else>
                    <img src="<g:resource dir='/images' file='no.png'/>" alt="<g:message code="input.disabled"/>">
                </g:else>
            </td>
            <td class="center">
                <g:if test="${relationType.cloneOnRightCopy}">
                    <img src="<g:resource dir='/images' file='ok.png'/>" alt="<g:message code="input.enabled"/>">
                </g:if>
                <g:else>
                    <img src="<g:resource dir='/images' file='no.png'/>" alt="<g:message code="input.disabled"/>">
                </g:else>
            </td>

            <td>${relationType.leftResolver.name}</td>
            <td>${relationType.rightResolver.name}</td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
    <util:remotePaginate controller="relationType" action="updateList" total="${RelationType.count()}"
                         update="relationTypeList" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>
</div>