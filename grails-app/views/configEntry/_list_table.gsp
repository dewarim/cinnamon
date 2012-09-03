<table>
    <thead>
    <tr>
        <g:sortableColumn action="index" property="id" title="${message(code:'id')}"/>
        <g:sortableColumn action="index" property="name" title="${message(code:'configEntry.name')}"/>
        <th class="center"><g:message code="configEntry.config"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${configEntryList}" status="i" var="configEntry">
        <tr id="configEntry_${configEntry.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <g:render template="row" model="[configEntry:configEntry]"/>
        </tr>
    </g:each>
    </tbody>
</table>

<div id="paginateButtons" class="paginateButtons">
    <g:render template="rePaginate"/>
</div>