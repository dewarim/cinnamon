<%@ page import="cinnamon.lifecycle.LifeCycle" %>
<table>
	<thead>
	<tr>
		<g:sortableColumn property="id" title="${message(code:'id')}"/>
		<g:sortableColumn property="name" title="${message(code:'lifeCycle.name')}"/>
		<g:sortableColumn property="defaultState" title="${message(code:'lifeCycle.defaultState')}"/>
		<g:sortableColumn property="states" title="${message(code:'lifeCycle.states')}"/>
		<th class="center"><g:message code="lifeCycle.options"/></th>
	</tr>
	</thead>
	<tbody>
	<g:each in="${lifeCycleList}" status="i" var="lifeCycle">
		<tr id="lifeCycle_${lifeCycle.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
			<g:render template="row" model="[lifeCycle:lifeCycle]"/>
		</tr>
	</g:each>
	</tbody>
</table>
<div class="paginateButtons">
    <util:remotePaginate controller="lifeCycle" action="updateList" total="${LifeCycle.count()}"
                         update="lifeCycleTable" max="100" pageSizes="[100, 250, 500, 1000]"/>
</div>