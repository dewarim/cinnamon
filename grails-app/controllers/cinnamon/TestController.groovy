package cinnamon

import cinnamon.index.SearchableDomain
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
class TestController {

    def luceneService
    
    def search(){
        def q = "<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>test</TermQuery></Clause></BooleanQuery>"
        def result = luceneService.search(q, "demo", SearchableDomain.OSD)
        log.debug(result)
        render(text:result.toString())
    }
    
    def echo(String msg) {
        log.info("TestController received msg: " + (msg?.encodeAsHTML()))
        log.info("Params: " + params)
        if (request.getHeader("microservice-test")?.equals("ok")) {
            log.info("Request was changed by microservice test.")
        }
        if (!msg) {
            msg = ""
        }
        render(text: msg.encodeAsHTML())
    }

    def echo2(String msg) {
        log.info("TestController received msg: " + (msg?.encodeAsHTML()))
        log.info("Params: " + params)
        if (request.getHeader("microservice-test")?.equals("ok")) {
            log.info("Request was changed by microservice test.")
        }
        if (!msg) {
            msg = ""
        }
        return [msg: msg, foo:'bar']
    }

    def microserviceChangeTriggerPreRequestTest(String msg) {
        log.info("microserviceChangeTriggerPreRequestTest received: " + (msg?.encodeAsHTML()))
        log.debug("Params: " + params)
        if (!msg) {
            throw new RuntimeException("parameter 'msg' is not set!")
        }
        response.setHeader("microservice-pre-test", "ok")
        render(text:"")
    }

    // test trigger that denies versioning of objects with uneven id
    /*
     needs:
     INSERT INTO public.change_triggers (id, active, obj_version, ranking, change_trigger_type_id, action,
      pre_trigger, post_trigger, config, controller) VALUES (9, true, 1, 200, 2, 'newVersionXml', true, false,
       '<config><remoteServer>http://localhost:8080/cinnamon/test/microserviceChangeTriggerFilterRequest</remoteServer></config>', 'osd');

     call like:
         curl -vv  --header "ticket:${TICKET}" -d "preid=59654&status=false" http://localhost:8080/cinnamon/cinnamon/legacy?command=version
     */
    def microserviceChangeTriggerFilterRequest(Boolean status) {
        log.info("microservice received status: " + status)

        if(status){
            response.setStatus(200)
            response.setHeader("microservice-pre-test", "OKAY")
            render(text:"<cinnamon><result>OKAY</result></cinnamon>")
        }
        else{
            response.setStatus(400)
            response.setHeader("microservice-pre-test", "NOPE")
            render(text:"<cinnamon><result>NOT OKAY</result></cinnamon>")
        }
    }

    def microserviceChangeTriggerPostRequestTest(String msg) {
        log.info("microserviceChangeTriggerPostRequestTest received: " + (msg?.encodeAsHTML()))
        log.debug("Params: " + params)
        response.setHeader("microservice-post-test", "ok")
        render(text: "<xml>Just a trigger result</xml>")
    }


}
