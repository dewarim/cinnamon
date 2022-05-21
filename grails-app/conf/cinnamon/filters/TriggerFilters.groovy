package cinnamon.filters

import cinnamon.trigger.ChangeTrigger
import cinnamon.PoBox

class TriggerFilters {

    def userService
    def infoService

    def filters = {
        changeTriggers(controller: '*', action: '*') {
            before = {
                infoService.setLastInsertId(null)
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
                    log.debug("executing pre trigger: " + changeTrigger.getTriggerType().getName());
                    def trigger = changeTrigger.triggerType.triggerClass.newInstance();
                    poBox = trigger.executePreCommand(poBox, changeTrigger)
                }
                if (poBox.endProcessing) {
                    return false
                    // TODO: redirect to error view
                }
                return true
            }

            afterView = { Exception exception ->

                    log.debug("afterView: controllerName: ${controllerName} / action: ${actionName})")
// \n params: " + "${params}")
                    if (flash["__post_triggers_are_done__"]) {
                        log.debug("afterView filters for this request were already run.")
                        flash["__post_triggers_are_done__"] = false
                        return true
                    }
                    if (exception != null) {
                        return true // do not apply any filters on exceptions.
                    }

                    def triggers = ChangeTrigger.findAll("""from ChangeTrigger ct where 
                    ct.controller=:controller and
                    ct.action=:action and
                    ct.postTrigger=true and
                    ct.active=true
                    order by ct.ranking
""".replaceAll('\n', ' '), [controller: controllerName, action: actionName])

                    PoBox poBox = new PoBox(request, response, userService.user, session.repositoryName, params, null,
                            controllerName, actionName, grailsApplication);
                    log.debug("LastInsertId: " + infoService.getLastInsertId())
                    triggers.each { changeTrigger ->
                        if (poBox.endProcessing) {
                            return false
                        }
                        poBox.lastInsertId = infoService.getLastInsertId();
                        log.debug("executing post trigger: " + changeTrigger.triggerType.name);
                        def trigger = changeTrigger.triggerType.triggerClass.newInstance();
                        poBox = trigger.executePostCommand(poBox, changeTrigger)
                        flash["__post_triggers_are_done__"] = true
                    }
                    if (poBox.endProcessing) {
                        return false
                        // TODO: redirect to / render error view
                    }
                    return true
                }
        }
    }
}
