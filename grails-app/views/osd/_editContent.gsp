<g:form method="POST" enctype="multipart/form-data" onsubmit="return false;">
    <input type="hidden" name="osd" value="${osd.id}">
%{--<input type="hidden" name="formatId" value=""/>--}%
    <input id="fileupload_${osd_id}" type="file" name="file" data-url="${webRequest.baseUrl}/osd/saveContentJson">

    <p id="uploadResult_${osd.id}" style="display: none"></p>
</g:form>

<script type="application/javascript">
    $(function () {
        $('#fileupload_${osd_id}').fileupload({
            dataType: 'json',
            done: function (e, data) {
                console.log(data);
                $('#uploadResult_${osd.id}').text(data.result.result.msg);
                $('#uploadResult_${osd.id}').show();
            }
        });
    });
</script>