<%@ page  contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Scorecard Viewer</title>
    <link rel="stylesheet" href="css/jquery-ui.min.css">
    <style type="text/css">

        .ui-tabs-vertical {  border: none !important;}
        .ui-tabs-vertical .ui-tabs-nav { padding: .2em .2em .2em .2em; float: left; width: 20em; }
        .ui-tabs-vertical .ui-tabs-nav li { clear: left; width: 100%; border-bottom-width: 1px !important; border-right-width: 0 !important; margin: 0 -1px .2em 0; }
        .ui-tabs-vertical .ui-tabs-nav li a { display:block; }
        .ui-tabs-vertical .ui-tabs-nav li.ui-tabs-active { padding-bottom: 0; padding-right: .1em; border-right-width: 1px; }
        .ui-tabs-vertical .ui-tabs-panel {   padding: 0;}
    </style>

    <script src="js/jquery.min.js" ></script>
    <script src="js/jquery-ui.min.js" ></script>
    <script  type="text/javascript">
        var boxID = 0;
        var data_url ;

        function changePlotURL(src){
            var name = src.split(/_(\d)*\.png/)[0];
            var src_new= data_url + name +'/' + src;
            var pos = {my: "center bottom",
                at: "center bottom",
                of: window};

            boxID = boxID + 1;
            var d = $('<div>').attr("id", boxID + "dialog").dialog({
                height: '690',
                width: '850',
                autoOpen: false,
                resizable: true,
                closeOnEscape: true,
                focus: true,
                dialogClass: "dialog-box-" + boxID,
                title: src,
                position: pos,

                close: function () {
                    $(this).empty();
                    $(this).dialog('destroy');

                },
                open: function () {
                    $(this).html('<img src="' + src_new + '?' + new Date().getTime()+ '" alt="' + src_new + '"/>');
                }

            });
            d.dialog("open");

        }

        $(document).ready(function() {
            $.ajax({
                url : 'ScorecardServlet',
                data : {

                },
                success : function(responseText) {
                    var json = $.parseJSON(responseText);
                    var scorecards = json.scorecards;
                    data_url = json.data_url;
                    for(var i=0; i< scorecards.length; i++){

                        const li = $("<li>");
                        const a = $("<a>").attr("href", data_url+ '/' +scorecards[i]+".html"+'?' + new Date().getTime()).text(scorecards[i]);
                        li.append(a);
                        $('#tabs1_ul').append(li);
                    }
                    $( "#tabs1" ).tabs({
                        beforeLoad: function( event, ui ) {
                            ui.jqXHR.fail(function() {
                                ui.panel.html(
                                    "Couldn't load this tab. We'll try to fix this as soon as possible. " +
                                    "If this wouldn't be a demo." );
                            });
                        }
                    }).addClass( "ui-tabs-vertical ui-helper-clearfix" );
                    $( "#tabs1 li" ).removeClass( "ui-corner-top" ).addClass( "ui-corner-left" );

                }
            });


        });
    </script>
</head>
<body>
<div style="text-align: left;padding-left: 1em;font-family: Arial,Helvetica,sans-serif;font-size: 1.2em;">Scorecards:</div></body>
<div id="tabs1">
    <ul id="tabs1_ul">
    </ul>
</div>




</html>