package cinnamon.filters

import cinnamon.trigger.ChangeTrigger
import cinnamon.PoBox

class TriggerFilters {

    def userService

    def filters = {
        changeTriggers(controller: '*', action: '*') {
            before = {
                log.debug("controllerName: ${controllerName} / action: ${actionName}")
                
                def triggers = ChangeTrigger.findAll("""from ChangeTrigger ct where 
                    ct.controller=:controller and
                    ct.action=:action and 
                    ct.preTrigger=true and
                    ct.active=true
                    order by ct.ranking
""".replaceAll('\n', ' '), [controller: controllerName, action: actionName])

                log.debug("found ${triggers.size()} changeTriggers")
               
                
                PoBox poBox = new PoBox(request, response, userService.user, session.repositoryName, params, null,
                        controllerName, actionName, grailsApplication) 

                triggers.each { changeTrigger ->                    
                    if (poBox.endProcessing) {
                        return
                    }
                    log.debug("executing trigger: " + changeTrigger.getTriggerType().getName());
                    def trigger = changeTrigger.triggerType.triggerClass.newInstance();
                    poBox = trigger.executePreCommand(poBox, changeTrigger)
                }
                if (poBox.endProcessing) {
                    return false
                    // TODO: redirect to error view
                }
                return true
            }

//            afterView = { (Exception exception ->
//            if(exception != null){
//                return true // do not apply any filters on exceptions.
//            }
            after = { Map model -> 

                def triggers = ChangeTrigger.findAll("""from ChangeTrigger ct where 
                    ct.controller=:controller and
                    ct.action=:action and
                    ct.postTrigger=true and
                    ct.active=true
                    order by ct.ranking
""".replaceAll('\n', ' '), [controller: controllerName, action: actionName])
                
                log.debug("model: "+model)
                
                PoBox poBox = new PoBox(request, response, userService.user, session.repositoryName, params, model, 
                        controllerName, actionName, grailsApplication);
                triggers.each { changeTrigger ->
                    if (poBox.endProcessing) {
                        return
                    }
                    log.debug("executing trigger: " + changeTrigger.getTriggerType().getName());
                    def trigger = changeTrigger.triggerType.triggerClass.newInstance();
                    poBox = trigger.executePostCommand(poBox, changeTrigger)
                }
                if (poBox.endProcessing) {
                    return false
                    // TODO: redirect to / render error view
                }
                return true
            }
            afterView = { Exception e ->

            }
        }
    }
}
