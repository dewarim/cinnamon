<%@ page import="cinnamon.i18n.Language" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code:'language.id')}"/>

        <g:sortableColumn property="isoCode" title="${message(code:'language.isoCode').encodeAsHTML()}"/>
        <td><g:message code="language.metadata"/></td>
    </tr>
    </thead>
    <tbody>
    <g:each in="${languageList}" status="i" var="language">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show"
                        id="${language.id}">${fieldValue(bean: language, field: 'id')}</g:link></td>

            <td>${fieldValue(bean: language, field: 'isoCode')?.encodeAsHTML()}</td>
            <td><g:render template="/shared/renderXML"
                          model="[renderId:language.id, xml:language.metadata]"/></td>
        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
    <util:remotePaginate controller="language" action="updateList" total="${Language.count()}"
                         update="languageList" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>
</div>