
CKEDITOR.editorConfig = function(config) {
    config.resize_enabled = false;
    config.toolbar = 'Toolbar';
    config.toolbar_Toolbar = [
            [ 'Save','Undo', 'Redo', '-', 'Bold', 'Italic', 'Underline', 'Strike', '-', 'Cut', 'Copy', 'Paste'],
            [ 'JustifyLeft', 'JustifyCenter','JustifyRight', 'JustifyBlock', '-','NumberedList','BulletedList' ],
            [ 'Outdent','Indent','-','Blockquote'],
            //[ 'Table', 'PageBreak','Styles', 'Maximize' ] ];
            [ 'Table', 'PageBreak','Maximize' ] ];
    
    config.stylesSet = 'default:/js/cke_styles.js';
};

CKEDITOR.config.contentsCss = 'css/0/portfolio.css';
//CKEDITOR.replace('myfield');