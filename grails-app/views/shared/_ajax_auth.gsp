<style>
#ajaxLogin {
	margin: 15px 0;
	padding: 0;
	text-align: center;
	display: none;
	position: absolute;
}

#ajaxLogin .inner {
	width: 260px;
	margin: 0 auto;
	text-align: left;
	padding: 10px;
	border-top: 1px dashed #499ede;
	border-bottom: 1px dashed #499ede;
	background-color: #EEF;
}

#ajaxLogin .inner .fheader {
	padding: 4px;
	margin: 3px 0 3px 0;
	color: #2e3741;
	font-size: 14px;
	font-weight: bold;
}

#ajaxLogin .inner .cssform p {
	clear: left;
	margin: 0;
	padding: 5px 0 8px 0;
	padding-left: 105px;
	border-top: 1px dashed gray;
	margin-bottom: 10px;
	height: 1%;
}

#ajaxLogin .inner .cssform input[type='text'] {
	width: 120px;
}

#ajaxLogin .inner .cssform label {
	font-weight: bold;
	float: left;
	margin-left: -105px;
	width: 100px;
}

#ajaxLogin .inner .login_message {
	color: red;
}

#ajaxLogin .inner .text_ {
	width: 120px;
}

#ajaxLogin .inner .chk {
	height: 12px;
}

.errorMessage {
	color: red;
}
</style>

<div id='ajaxLogin'>
	<div class='inner'>
		<form action='${request.contextPath}/j_spring_security_check' method='POST' id='ajaxLoginForm' name='ajaxLoginForm' class='cssform'>
			<input type="hidden" name="environment" value="${environment}"/>
			<input type="hidden" name="submitForm" value="1"/>
			<p>
				<label for='username'><g:message code="login.username"/></label>
				<input type='text' class='text_' name='j_username' id='username'/>
			</p>
			<p>
				<label for='password'><g:message code="login.password"/></label>
				<input type='password' class='text_' name='j_password' id='password'/>
			</p>
			<p>
				<label for='remember_me'><g:message code="login.remember_me"/></label>
				<input type='checkbox' class='chk' name='${rememberMeParameter}' id='remember_me'
					<g:if test='${hasCookie}'>checked='checked'</g:if>/>
			</p>
			<p>
				<input type='submit' value='${message(code: "login")}'/>
			</p>
		</form>
	</div>
</div>

<script type='text/javascript'>

	// center the form
	Event.observe(window, 'load', function() {
		var ajaxLogin = $('ajaxLogin');
		$('ajaxLogin').style.left = ((document.body.getDimensions().width - ajaxLogin.getDimensions().width) / 2) + 'px';
		$('ajaxLogin').style.top = ((document.body.getDimensions().height - ajaxLogin.getDimensions().height) / 2) + 'px';
	});

	function showLogin() {
		$('ajaxLogin').style.display = 'block';
	}

	function cancelLogin() {
		Form.enable(document.ajaxLoginForm);
		Element.hide('ajaxLogin');
	}

	function authAjax() {
		Form.enable(document.ajaxLoginForm);
		Element.update('loginMessage', 'Sending request ...');
		Element.show('loginMessage');

		var form = document.ajaxLoginForm;
		var params = Form.serialize(form);
		Form.disable(form);
		new Ajax.Request(form.action, { method: 'POST', postBody: params, onSuccess: function(response) {
			Form.enable(document.ajaxLoginForm);
			var responseText = response.responseText || '[]';
			var json = responseText.evalJSON();
			if (json.success) {
				Element.hide('ajaxLogin');
				$('message').update("${message(code:'login.success')}");
			} else if (json.error) {
				Element.update('message', "<span class='errorMessage'>" + json.error + '</error>');
			}
			else {
				Element.update('message', responseText);
			}
		} });
	}
</script>