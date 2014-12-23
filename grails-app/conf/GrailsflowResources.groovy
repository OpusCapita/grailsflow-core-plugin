// Resource declarations for Resources plugin
// This is a bit ugly, we'll find a way to make this better in future
def plugin = grails.util.Holders.getGrailsApplication().getMainContext().getBean("pluginManager")?.getGrailsPlugin("jquery-ui")
def jqver = plugin.instance.JQUERYUI_VERSION

modules = {
    overrides {
        'jquery-theme' {
            resource id:'theme',
            url:[ plugin: 'jqueryUi', dir: 'jquery-ui/themes/ui-lightness',
                  file:'jquery-ui-'+jqver+'.custom.css'],
            attrs:[media:'screen, projection']
        }


        'jquery-ui' {
            dependsOn 'jquery', 'jquery-theme'
            resource id:'js', url:[plugin: 'jqueryUi', dir:'jquery-ui/js', file:"jquery-ui-${jqver}.custom.min.js"],
            nominify: true, disposition: 'head'
        }
    }
    grailsflow {
        dependsOn 'jquery', 'jquery-ui'
        defaultBundle 'ui'
        resource url:'/js/grailsflow/common.js'
    }
    grailsflowGraphics {
        dependsOn 'jquery'
        resource url:'/css/grailsflow/graphic/ext-all.css'
        resource url:'/js/grailsflow/common.js'
        resource url:'/js/grailsflow/draw2d/draw2d.js'
        resource url:'/js/grailsflow/draw2d/wz_jsgraphics.js'
        resource url:'/js/grailsflow/draw2d/YahooUI_integration/adapter/yui/yui-utilities.js'
        resource url:'/js/grailsflow/draw2d/YahooUI_integration/adapter/yui/ext-yui-adapter.js'
        resource url:'/js/grailsflow/draw2d/YahooUI_integration/ext-all.js'
        resource url:'/js/grailsflow/draw2d/node/NodeFigure.js'
        resource url:'/js/grailsflow/draw2d/node/ContextmenuConnection.js'
        resource url:'/js/grailsflow/draw2d/node/NodeInputPort.js'
        resource url:'/js/grailsflow/draw2d/node/NodeOutputPort.js'
        resource url:'/js/grailsflow/draw2d/node/TransitionDecorator.js'
    }
    grailsflowCalendar {
        resource url:'/js/grailsflow/jquery/config.js', disposition: 'head'
    }
    grailsflowJgplot {
        dependsOn 'jquery'
        resource url:'/css/grailsflow/jqplot/jquery.jqplot.min.css'
        resource url:'/js/grailsflow/jqplot/excanvas.min.js', wrapper: {s -> "<!--[if IE]>$s<![endif]-->"}, disposition: 'head'
        resource url:'/js/grailsflow/jqplot/jquery.jqplot.min.js'
        resource url:'/js/grailsflow/jqplot/plugins/jqplot.categoryAxisRenderer.min.js'
        resource url:'/js/grailsflow/jqplot/plugins/jqplot.enhancedLegendRenderer.min.js'

    }
    grailsflowCodeMirror {
        resource url:'/css/grailsflow/codemirror/codemirror.css'
        resource url:'/css/grailsflow/codemirror/default.css'
        resource url:'/js/grailsflow/codemirror/codemirror.js'
        resource url:'/js/grailsflow/codemirror/clike.js'
        resource url:'/css/grailsflow/codemirror/grailsflow.css'
    }
    grailsflowHighlighter {
        resource url:'/css/grailsflow/syntaxhighlighter/shCore.css'
        resource url:'/css/grailsflow/syntaxhighlighter/shThemeDefault.css'
        resource url:'/js/grailsflow/syntaxhighlighter/shCore.js'
        resource url:'/js/grailsflow/syntaxhighlighter/shBrushGroovy.js'
    }

}