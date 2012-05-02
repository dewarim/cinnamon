<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>
    <g:layoutTitle default="Grails"/></title>
<g:meta name="app.version"/>
<g:layoutHead/>

<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"/>
<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon"/>

<r:require modules="jquery"/>

<r:script>
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

    function limitText(limitField, limitNum) {
        if (limitField.value.length > limitNum) {
            limitField.value = limitField.value.substring(0, limitNum);
        }
    }

    function showInfoMessage(info) {
        var infoElement = $('#infoMessage');
        infoElement.text(info);
        if (infoElement.text().length > 0) {
            infoElement.show();
        }
    }

</r:script>



<script type="text/javascript" src="<g:resource dir="/js/codemirror/lib" file="codemirror.js"/>"></script>
<link rel="stylesheet" href="<g:resource dir="/js/codemirror/lib" file="codemirror.css"/>">
<script type="text/javascript" src="<g:resource dir="/js/codemirror/mode/xml" file="xml.js"/>"></script>
<%-- <link rel="stylesheet" href="<g:resource dir="/js/CodeMirror-2.24/mode/xml" file="xml.css"/>"> --%>
<script type="text/javascript"
        src="<g:resource dir="/js/codemirror/mode/htmlmixed" file="htmlmixed.js"/>"></script>

<script src='<g:resource dir="js/codemirror-ui/js" file="codemirror-ui.js"/>' type="text/javascript"></script>
<link rel="stylesheet" href="<g:resource dir="js/codemirror-ui/css/" file="codemirror-ui.css"/>" type="text/css"
      media="screen"/>

<r:script disposition="head">
    $.ajaxSetup({
        type:'POST'
    });

    var codeMirrorEditor;
    function createEditor(id) {
        var uiOptions = { path:'<g:resource dir="js/codemirror-ui/" file="js" />/', searchMode:'popup' };
        var cmOptions = {
            mode:'application/xml',
            lineNumbers:true
        };
        codeMirrorEditor = new CodeMirrorUI(id, uiOptions, cmOptions);
        codeMirrorEditor.mirror.refresh();
    }

    function hideChildren(id) {
        var children = document.getElementById('children_of_' + id);
        if (children != null) {
            children.style.display = 'none';
        }
        children = document.getElementById('hideChildren_' + id);
        if (children != null) {
            children.style.display = 'none';
        }
        var fetchLink = document.getElementById('fetchLink_' + id);
        if (fetchLink != null) {
            fetchLink.style.display = 'inline';
        }
    }

    function showHideLink(id) {
        var children = document.getElementById('children_of_' + id);
        if (children != null) {
            children.style.display = 'block';
        }
        children = document.getElementById('hideChildren_' + id);
        if (children != null) {
            children.style.display = 'inline';
        }
        var fetchLink = document.getElementById('fetchLink_' + id);
        if (fetchLink != null) {
            fetchLink.style.display = 'none';
        }
    }

    function setLinkActive(id) {
        $("span.folder_name_content").each(function (index) {
            $(this).css('background-color', 'white')
        });
        $("span.folder_name_no_content").each(function (index) {
            $(this).css('background-color', 'white')
        });

        $("#" + id).css('background-color', "#CCFFCC");
    }

    function setFolderName(id, name) {
        $("#folderName_" + id).html(name);
    }

    function setOsdActive(id, oddEven) {
        $("tr.osd_row").each(function (index) {
            $(this).removeClass('row_highlight');
            if ($(this).hasClass('was_even')) {
                $(this).removeClass('was_even');
                $(this).addClass('even');
            }

        });
        var osd = $("#" + id);
        osd.addClass('row_highlight');
        if (osd.hasClass('even')) {
            osd.removeClass('even');
            osd.addClass('was_even');
        }
    }

    function showSpinner(id) {
        $("#" + id).prepend('<img src="<g:resource dir="/images" file="spinner.gif"/>" alt="<g:message code="message.loading"/>" id="' + id + '_spinner">');
    }

    function hideSpinner(id) {
        $("#" + id + "_spinner").detach();
    }

    function showClearButton() {
        var msg = $("#message");

        msg.append('<a class="close_button" href="#" onclick="hideClearButton();return false;">' +
'<r:img border="0" uri="/images/no.png" alt="${message(code:"message.clear").encodeAsJavaScript()}"/>' +
'</a> ');
        msg.addClass('error_message');

    }

    function hideClearButton() {
        var msg = $("#message");
        msg.html('&nbsp;');
        msg.removeClass("error_message");
    }

    function copyFolder(id, targetId) {
        jQuery.ajax({
            url:'<g:resource dir="folder" file="copyFolder"/>',
            success:function (data, textStatus, xmlrequest) {
                $("#" + targetId).html(data);
            },
            error:function (xmlrequest, status, errorThrown) {
                var sm = $("#message");
                sm.html(xmlrequest.responseText);
                showClearButton();
            },
            data:{targetFolder:targetId, folder:id},
            method:'POST',
            async:true
        })
    }

    function showRelationType(id) {
        var dialog = $('<div class="hidden"></div>').appendTo('body');
        var url = '<g:resource dir="/folder" file="fetchRelationTypeDialog"/>?relationType=' + id;
        dialog.load(
                url,
                {},
                function (responseText, textStatus, XMLHttpRequest) {
                    dialog.dialog({
                        closeOnEscape:true,
                        close:function (event, ui) {
                            dialog.dialog('destroy')
                        }
                    });
                });
        return false;
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


</r:script>
<r:layoutResources/>
</head>
<body>
<header>
    <div id="header">
        <a href="${resource(dir: 'folder', file: 'index')}">
            <r:img uri="/images/illicium_100.jpg" alt="${message(code: 'app.illicium')}" border="0"/>
        </a>

        <h1 id="TITLE">Illicium</h1>

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

<hr class="bottom_line">

<p>
    <sec:ifLoggedIn>
        <g:link controller="logout" action="index"><g:message code="logout.link"
                                                              args="[session.repositoryName]"/></g:link>
    </sec:ifLoggedIn>
    <sec:ifNotLoggedIn>
        <g:link controller="login" action="auth"><g:message code="login.link"/></g:link>
    </sec:ifNotLoggedIn>
</p>

<r:layoutResources/>
</body>
</html>