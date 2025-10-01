// Resource declarations for Resources plugin

modules = {
    grailsflow {
        dependsOn 'jquery'
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/common.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/jquery/config.js']
    }
    grailsflowGraphics {
        dependsOn 'jquery'
        resource url: [plugin: 'grailsflow', file: 'css/grailsflow/graphic/ext-all.css']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/common.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/draw2d/draw2d.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/draw2d/wz_jsgraphics.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/draw2d/YahooUI_integration/adapter/yui/yui-utilities.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/draw2d/YahooUI_integration/adapter/yui/ext-yui-adapter.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/draw2d/YahooUI_integration/ext-all.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/draw2d/node/NodeFigure.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/draw2d/node/ContextmenuConnection.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/draw2d/node/NodeInputPort.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/draw2d/node/NodeOutputPort.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/draw2d/node/TransitionDecorator.js']
    }

    grailsflowDatepicker {
        dependsOn('jquery')
        resource url: [plugin: 'grailsflow', file: 'css/grailsflow/datepicker/bootstrap-datepicker.min.css'], nominify: true
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/datepicker/bootstrap-datepicker.min.js'], nominify: true
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/datepicker/bootstrap-datepicker.de.min.js'], nominify: true
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/datepicker/bootstrap-datepicker.es.min.js'], nominify: true
    }

    grailsflowJgplot {
        dependsOn 'jquery'
        resource url: [plugin: 'grailsflow', file: 'css/grailsflow/jqplot/jquery.jqplot.min.css'], nominify: true
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/jqplot/excanvas.min.js'], nominify: true, wrapper: {s -> "<!--[if IE]>$s<![endif]-->"}, disposition: 'head'
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/jqplot/jquery.jqplot.min.js'], nominify: true
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/jqplot/plugins/jqplot.categoryAxisRenderer.min.js'], nominify: true
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/jqplot/plugins/jqplot.enhancedLegendRenderer.min.js'], nominify: true

    }
    grailsflowCodeMirror {
        resource url: [plugin: 'grailsflow', file: 'css/grailsflow/codemirror/codemirror.css']
        resource url: [plugin: 'grailsflow', file: 'css/grailsflow/codemirror/default.css']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/codemirror/codemirror.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/codemirror/clike.js']
        resource url: [plugin: 'grailsflow', file: 'css/grailsflow/codemirror/grailsflow.css']
    }
    grailsflowHighlighter {
        resource url: [plugin: 'grailsflow', file: 'css/grailsflow/syntaxhighlighter/shCore.css']
        resource url: [plugin: 'grailsflow', file: 'css/grailsflow/syntaxhighlighter/shThemeDefault.css']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/syntaxhighlighter/shCore.js']
        resource url: [plugin: 'grailsflow', file: 'js/grailsflow/syntaxhighlighter/shBrushGroovy.js']
    }

}
