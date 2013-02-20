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


/**
 * Load the content of a folder with a specific usage type and (if applicable) an osdId.
 * @param folderId
 * @param folderType the template type, for example 'relation'
 * @param osdId
 */
function loadFolderContent(folderId, folderType, osdId) {
    $.ajax({
        url: CINNAMON.links.loadFolderContent,
        data: {
            osd:osdId,
            folder: folderId,
            folderType: folderType
        },
        dataType: 'html',
        success: function (data) {
            $('#' + folderType + 'FolderContent').html(data);
        },
        statusCode: {
            500: function () {
                alert(CINNAMON.i18n.load_preview_fail);
            }
        }
    });
}


function showRelationType(id) {
    var rtDialog = $('#relationtype-dialog');
    var url = CINNAMON.links.showRelationType + id;
    rtDialog.load(
        url,
        {},
        function (responseText, textStatus, XMLHttpRequest) {
            $('#relationtype-dialog').dialog({
                autoOpen: false,
                height: 540,
                width: 540,
                modal: true,
                closeOnEscape:true,
                buttons: [
                    {
                        text: CINNAMON.i18n.dialog_close,
                        click: function () {
                            $(this).dialog("close");
                        }
                    }
                ],

                close:function (event, ui) {
                    rtDialog.dialog('destroy');
                }
            });
            rtDialog.dialog('open');
        });
    return false;
} 