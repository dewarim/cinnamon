<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
    <title>Login</title>

    <style type='text/css' media='screen'>
    #login {
        margin: 15px 0;
        padding: 0;
        text-align: center;
    }

    #login .inner {
        width: 260px;
        margin: 0 auto;
        text-align: left;
        padding: 10px;
        border-top: 1px dashed #499ede;
        border-bottom: 1px dashed #499ede;
        background-color: #EEF;
    }

    #login .inner .fheader {
        padding: 4px;
        margin: 3px 0 3px 0;
        color: #2e3741;
        font-size: 14px;
        font-weight: bold;
    }

    #login .inner .cssform p {
        clear: left;
        margin: 0;
        padding: 5px 0 8px 0;
        padding-left: 105px;
        border-top: 1px dashed gray;
        margin-bottom: 10px;
        height: 1%;
    }

    #login .inner .cssform input[type='text'] {
        width: 120px;
    }

    #login .inner .cssform label {
        font-weight: bold;
        float: left;
        margin-left: -105px;
        width: 100px;
    }

    h1.appName {
        text-align: center;
    }

    h2.title {
        text-align: center;
        margin-bottom: 2ex;
    }

    #login .inner .login_message {
        color: red;
    }

    #login .inner .text_ {
        width: 120px;
    }

    #login .inner .chk {
        height: 12px;
    }
    </style>


    <script type="text/javascript">
        function showForm() {
            document.getElementById('loginForm').style.display = 'block';
            document.getElementById('username').focus();
        }
    </script>
    <r:layoutResources />

</head>

<body onLoad="showForm();">
<div class="login_logo" style="text-align:center; margin-top:2ex;">
    <img src="${resource(dir: 'images', file: 'cinnamon-screen.jpg')}" alt="Cinnamon">
</div>

<div id='login'>
    <div class='inner'>
        <g:set var="localAppName" value="app.${grailsApplication.metadata['app.name']}"/>
        <g:if test="${ message(code:localAppName) == localAppName}">
            <h1 class="title"><g:message code="login.title"/></h1>
        </g:if>
        <g:else>
            <h1 class="appName"><g:message code="app.${grailsApplication.metadata['app.name']}"/></h1>
            <h2 class="title"><g:message code="login.title"/></h2>
        </g:else>

        <g:if test='${flash.message}'>
            <div class='login_message'>${flash.message}</div>
        </g:if>
        <noscript>
            <g:message code="enable.javascript"/>
        </noscript>

        <form action='${postUrl}' method='POST' id='loginForm' class='cssform' style="display:none;">
            <p>
                <label for="environment"><g:message code="login.repository"/></label>
                <g:select name="environment" from="${repositories}" optionValue="prefix" optionKey="id"/>
            </p>

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
                <input type="hidden" name="submitForm" value="1">
                <input type='submit' value='${message(code: "login")}'/>
            </p>
        </form>
    </div>

    <div class="links">
        <p>
            <a href="http://cinnamon-cms.de"><g:message code="cinnamon.homepage"/></a> |
            <a href="http://www.gnu.org/licenses/lgpl-2.1.html"><g:message code="license.homepage"/></a>
        </p>

        <p>
            <a href="http://cinnamon-cms.de/cinnamonserver/license"><g:message code="license.overview.link"/></a>
        </p>
    </div>

</div>
<script type='text/javascript'>
    <!--
    (function() {
        document.forms['loginForm'].elements['j_username'].focus();
    })();
    // -->
</script>
<r:layoutResources />
</body>
</html>