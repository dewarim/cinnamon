<%@ page import="cinnamon.lifecycle.LifeCycle" %>
<g:set var="cid" value="${lcs?.id}"/>
<td class="value">
	<div class="lcs_name ${hasErrors(bean: lcs, field: 'name', 'errors')}">
		<label for="name_${cid}"><g:message code="lcs.name"/></label> <br>
		<input type="text" name="name" id="name_${cid}" value="${lcs?.name}"/>
	</div>
	<br>
	<div class="lcs_stateClass  ${hasErrors(bean: lcs, field: 'stateClass', 'errors')}">
		<label for="stateClass_${cid}"><g:message code="lcs.stateClass"/></label> <br>
        <g:select id="stateClass_${cid}" name="stateClass" from="${stateClasses}" value="${lcs?.stateClass?.name}" />
	</div>
</td>
<td class="value ">
	<div class="lcs_lifeCycle ${hasErrors(bean: lcs, field: 'lifeCycle', 'errors')}">
		<label for="lifeCycle_${cid}"><g:message code="lcs.lifeCycle"/></label> <br>
		<g:if test="${LifeCycle.count() > 0}">
			<g:select from="${LifeCycle.list()}" id="lifeCycle_${cid}" name="lifeCycle"
					noSelection="${[0:'---']}" value="${lcs?.lifeCycle?.id}"
					optionKey="id" optionValue="name"/>
		</g:if>
		<g:else>
			<g:message code="lcs.lifeCycle.list.empty"/>
		</g:else>
	</div>
	<br>
	<div class="lcs_copyState ${hasErrors(bean: lcs, field: 'lifeCycleStateForCopy', 'errors')}">
		<label for="lifeCycleStateForCopy_${cid}"><g:message code="lcs.lifeCycleStateForCopy"/></label> <br>
		<g:if test="${copyStates?.size() > 0}">
			<g:select from="${copyStates}" id="lifeCycleStateForCopy_${cid}" name="lifeCycleStateForCopy"
					noSelection="${[0:'---']}" value="${lcs?.lifeCycleStateForCopy?.id}"
					optionKey="id" optionValue="name"/>
		</g:if>
		<g:else>
			<g:message code="lcs.copyStates.empty"/>
		</g:else>
	</div>

</td>
<g:render template="editConfig" model="[lcs:lcs]"/>