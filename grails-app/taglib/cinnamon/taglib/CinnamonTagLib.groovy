package cinnamon.taglib

class CinnamonTagLib {

    /**
     * Display an icon for ok/activated or 'not ok'/deactivated fields.
     * This tag uses /images/ok.png and /images/no.png as resources.
     * @attr ok REQUIRED if true, display an 'ok' icon (green 'v'), if false, display a 'not ok' icon (red 'x')
     */
    def imageYesNo = {attrs, body->
        def res = g.resource(dir:'/images', file:"${attrs.ok.toString().equals('true') ? 'ok.png' : 'no.png'}")
        out << """<img src="${res}" alt="${message(code:attrs.ok.toString().equals('true') ? 'input.enabled':'input.disabled')}">"""
    }

}
