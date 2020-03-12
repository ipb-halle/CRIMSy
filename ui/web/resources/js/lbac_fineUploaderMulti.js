$(document).ready(function () {
    var url;
    if (document.location.protocol === 'https:') {
        urlUpload = 'https://' + window.location.host + '/ui/uploaddocs/';
        urlDelete = 'https://' + window.location.host + '/ui/deletedocs/';
    } else {
        urlUpload = 'http://' + window.location.host + '/ui/uploaddocs/';
        urlDelete = 'http://' + window.location.host + '/ui/deletedocs/';
    }
    $('#fine-uploader-manual-trigger').fineUploader({
        template: 'qq-template-manual-trigger',
        debug: true,
        autoUpload: false,
        request: {
            enabled: true,
            endpoint: urlUpload
        },
        deleteFile: {
            enabled: true,
            endpoint: urlDelete
        },
        success: {
            endpoint: urlUpload
        },
        resume: {
            enabled: true
        },
        callbacks: {
            onSubmitted: function (id, name) {
            },
            //*** set additional param collection before upload ***
            onUpload: function (id, name) {
                var sel = $('.upload-collection option:selected');
                this._options.request.params['collection'] = sel.val();
            }
        }
    });
    $('#trigger-upload').click(function () {
        $('#fine-uploader-manual-trigger').fineUploader('uploadStoredFiles');
    });
});

