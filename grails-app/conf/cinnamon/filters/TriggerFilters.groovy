package cinnamon.filters

import cinnamon.trigger.ChangeTrigger
import cinnamon.PoBox

class TriggerFilters {

    def userService

    def filters = {
        changeTriggers(controller: '*', action: '*') {
            before = {
//                if (!session.repositoryName) {
//                    // do not bother filtering requests for non-logged in users at this stage
//                    return true
//                }
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
                    poBox = trigger.executePreCommand(poBox, changeTrigger.config)
                }
                if (poBox.endProcessing) {
                    return false
                    // TODO: redirect to error view
                }
                return true
            }

            after = { Map model ->
                if (!session.repositoryName) {
                    // do not bother filtering requests for non-logged in users at this stage
                    return
                }

                def triggers = ChangeTrigger.findAll("""from ChangeTrigger ct where 
                    ct.controller=:controller and
                    ct.action=:action and
                    ct.postTrigger=true and
                    ct.active=true
                    order by ct.ranking
""".replaceAll('\n', ' '), [controller: controllerName, action: actionName])

                PoBox poBox = new PoBox(request, response, userService.user, session.repositoryName, params, model, 
                        controllerName, actionName, grailsApplication);
                triggers.each { changeTrigger ->
                    if (poBox.endProcessing) {
                        return
                    }
                    log.debug("executing trigger: " + changeTrigger.getTriggerType().getName());
                    def trigger = changeTrigger.triggerType.triggerClass.newInstance();
                    poBox = trigger.executePostCommand(poBox, changeTrigger.config)
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
