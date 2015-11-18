package cinnamon

import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
class TestController {
    
    
    def echo(String msg){
        log.info("TestController received msg: "+(msg?.encodeAsHTML()))
        log.info("Params: "+params)
        if(request.getHeader("microservice-test")?.equals("ok")){
            log.info("Request was changed by microservice test.")
        }
        if(!msg){
            msg = ""
        }
        render(text: msg.encodeAsHTML())
    }
    
    def microserviceChangeTriggerPreRequestTest(String msg) {
        log.info("microserviceChangeTriggerPreRequestTest received: "+(msg.encodeAsHTML()))
        log.debug("Params: "+params)
        response.setHeader("microservice-test", "ok")
        response.flushBuffer()  
    }
    
}
