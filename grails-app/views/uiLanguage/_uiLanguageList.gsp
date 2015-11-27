<%@ page import="cinnamon.i18n.UiLanguage" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code:'id')}"/>

        <g:sortableColumn property="isoCode" title="${message(code:'uiLanguage.isoCode').encodeAsHTML()}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${uiLanguageList}" status="i" var="language">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show" id="${language.id}">${fieldValue(bean: language, field: 'id')}</g:link></td>

            <td>${fieldValue(bean: language, field: 'isoCode')?.encodeAsHTML()}</td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
      <util:remotePaginate controller="uiLanguage" action="updateList" total="${UiLanguage.count()}"
                         update="uiLanguageList" max="100" pageSizes="[100, 250, 500, 1000]"/>
</div>