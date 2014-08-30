<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><g:layoutTitle default="Grails"/></title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <asset:javascript src="jquery-2.1.1.js"/>
    <asset:javascript src="cinnamon.js"/>
    <asset:javascript src="jquery-ui-1.11.1/jquery-ui.js"/>
    
    <asset:javascript src="codemirror/lib/codemirror.js"/>
    <asset:javascript src="codemirror/mode/xml.js"/>
    <asset:javascript src="codemirror/mode/hmlmixed.js"/>
    <asset:javascript src="codemirror-ui/js/codemirror-ui.js"/>
    <asset:stylesheet src="codemirror/lib/codemirror.css"/>
    <asset:stylesheet src="codemirror-ui/css/codemirror-ui.css"/>
    <asset:stylesheet src="jquery-ui-1.11.1/jquery-ui.css"/>
    <asset:stylesheet src="main.css"/>
    <asset:link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
    
<script type="text/javascript">
 
    function showInfoMessage(info){
        var infoElement = $('#infoMessage');
        infoElement.text(info);
        if(infoElement.text().length > 0){
            infoElement.show();
        }
    }

    function rePaginate(id) {
        $.post('${createLink(controller: controllerName, action:'rePaginate')}', function(data) {
            $('#' + id).html(data);
        });
    }

</script>

<g:layoutHead/>

<asset:script>
    $.ajaxSetup({
        type:'POST'
    });
    var addAll = false;

    function replaceAIfExistsB(a, replacement, b) {
        if ($("#" + b).length > 0) {
            $("#" + a).html(replacement);
        }
    }
    function replaceAIfExists(a, replacement) {
        if ($("#" + a).length > 0) {
            $("#" + a).html(replacement);
        }
    }

    function addClassIfExistsB(a, newClass, b) {
        if ($("#" + b).length > 0) {
            $("#" + a).addClass(newClass);
        }
    }

    function showInfoMessage(info) {
        var infoElement = $('#infoMessage');
        infoElement.text(info);
        if (infoElement.text().length > 0) {
            infoElement.show();
        }
    }

</asset:script>

<g:render template="/shared/variables"/>
    
    <asset:script type="text/javascript">

  

    $.ajaxSetup({
        type:'POST'
    });

    var codeMirrorEditor;
    
    function createEditor(id, readOnly) {
        var uiOptions = { path:'<g:createLink uri="/assets/codemirror-ui/js" />/', searchMode:'popup' };
        var cmOptions = {
            mode:'application/xml',
            lineNumbers:true,
            readOnly: readOnly === undefined ? false : readOnly 
        };
        codeMirrorEditor = new CodeMirrorUI(id, uiOptions, cmOptions);
        codeMirrorEditor.mirror.refresh();
    }
 
    function showSpinner(id) {
        $("#" + id).prepend('<img src="<g:createLink uri="/assets/images/spinner.gif"/>" alt="<g:message code="message.loading"/>" id="' + id + '_spinner">');
    }

    function hideSpinner(id) {
        $("#" + id + "_spinner").detach();
    }

    function showClearButton() {
        var msg = $("#message");

        msg.append('<a class="close_button" href="#" onclick="hideClearButton();return false;">' +
'<asset:image border="0" src="no.png" alt="${message(code:"message.clear").encodeAsJavaScript()}"/>' +
'</a> ');
        msg.addClass('error_message');

    }

    function hideClearButton() {
        var msg = $("#message");
        msg.html('&nbsp;');
        msg.removeClass("error_message");
    }
   
    
    function addToSelection(id, name) {
        var wasSelected = $('#selected_div_' + id);
        if (wasSelected.length) {
            // alert(id+" was already selected!");
            return;
        }
        var selection = $('#selectionOsd');
        var s1 = '<div id="selected_div_' + id + '">';
var s2 = '<input id="selected_input_' + id + '" type="hidden" name="osd" value="' + id + '">';
var s3 = '<a href="#" class="deselectObject" onclick="$(\'#selected_div_' + id + '\').remove();\$(\'#addToSelection_' + id + '\').show();return false;"> ';
var s4 = '#' + id + ': ' + name + '</a></div>';
        selection.prepend(s1 + s2 + s3 + s4);
        $('#objectSelection').show();
    }

    function addToFolderSelection(id, name) {
        var wasSelected = $('#selected_folder_div_' + id);
        if (wasSelected.length) {
            // alert(id+" was already selected!");
            return;
        }
        var selection = $('#selectionFolder');
        var s1 = '<div id="selected_folder_div_' + id + '">';
var s2 = '<input id="sourceFolder_input_' + id + '" type="hidden" name="folder" value="' + id + '">';
var s3 = '<a href="#" class="deselectFolder" onclick="$(\'#selected_folder_div_' + id + '\').remove();\$(\'.addToFolderSelection_' + id + '\').show();return false;"> ';
var s4 = '#' + id + ': ' + name + '</a></div>';
        selection.prepend(s1 + s2 + s3 + s4);
        $('#objectSelection').show();
    }


</asset:script>
</head>
<body>
<header>
    <div id="header">
        <a href="${resource(dir: 'folder', file: 'index')}">
            <asset:image src="illicium_100.jpg" alt="${message(code: 'app.illicium')}" border="0"/>
        </a>

        <h1 id="TITLE"><g:message code="${ headline ?: grailsApplication.config.appName}"/></h1>

        <div class="searchForm">
            <g:form onsubmit="\$('#searchFormSubmit').click();return false;" name="simpleSearchForm"
                    id="simpleSearchForm">
                <label for="simpleSearch" style="display:none;"><g:message code="search.label"/></label>
                <input name="query" placeholder="<g:message code="search.placeholder"/>" id="simpleSearch">
                <g:submitToRemote id="searchFormSubmit"
                                  update="[success:'searchResults', failure:'message']"
                                  url="[controller:'folder', action:'searchSimple']"
                                  onLoading="\$('#message').html('');"
                                  value="${message(code:'search.submit')}"/></g:form>
        </div>

    </div>

</header>

<g:layoutBody/>

<footer>
<hr class="bottom_line">
<p>
    <sec:ifLoggedIn>
        <g:link controller="logout" class="logout-link" action="index">
            <g:message code="logout.link" args="[session.repositoryName]"/>
        </g:link>
    </sec:ifLoggedIn>
    <sec:ifNotLoggedIn>
        <g:link controller="login" action="auth"><g:message code="login.link"/></g:link>
    </sec:ifNotLoggedIn>
</p>
</footer>
</body>
</html>