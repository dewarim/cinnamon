<hr class="bottom_line clear">
<p style="padding-left: 1em;padding-bottom: 0.75ex;">
	<sec:ifLoggedIn>
		<g:link controller="logout" action="index"><g:message code="logout.link" args="[session.repositoryName]"/></g:link>
	</sec:ifLoggedIn>
	<sec:ifNotLoggedIn>
		<g:link controller="login" action="auth"><g:message code="login.link"/></g:link>
	</sec:ifNotLoggedIn>
</p>
