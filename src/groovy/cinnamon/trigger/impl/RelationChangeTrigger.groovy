package cinnamon.trigger.impl

import cinnamon.trigger.ITrigger
import cinnamon.RelationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import cinnamon.PoBox
import cinnamon.ObjectSystemData

/**
 *
 */
public class RelationChangeTrigger implements ITrigger {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public PoBox executePreCommand(PoBox poBox, String config) {
        log.debug("preCommand of RelationChangeTrigger");
        if (poBox.controller.equals("osd") && poBox.action.equals("delete")) {
            /*
            * If an osd is deleted, we will be unable to use it later on to find the version tree
            * whose relations need updates. So, we store the id here - unless the delete command
            * refers to a lonely version one without descendants (in that case there will be no
            * relations left after deletion).
            */
            try {
                ObjectSystemData osd = ObjectSystemData.get((String) poBox.params.get("id"));
                if (osd.getRoot().equals(osd) && osd.getLatestBranch() && osd.getLatestHead()) {
                    // There is only this one object, so we got nothing to do here.
                }
                else {
                    poBox.params.put("_RelationChangeTrigger_delete_id_", String.valueOf(osd.getRoot().getId()));
                }
            }
            catch (Exception e) {
                log.debug("Failed to load osd.", e);
            }
        }
        return poBox;
    }

    @Override
    public PoBox executePostCommand(PoBox poBox, String config) {
        log.debug("postCommand RelationChangeTrigger");
        Map<String, Object> params = poBox.params
        String controller = poBox.controller
        String action = poBox.action

        ObjectSystemData osd = null;
        if(controller != 'osd'){
            // nothing to do.
            return poBox
        }
            
        if (commandIdMap.containsKey(action) && params.containsKey(commandIdMap.get(action))) {
            osd = ObjectSystemData.get((String) params.get(commandIdMap.get(action)));
        }
        else if (params.containsKey("_RelationChangeTrigger_delete_id_")) {
            // latestHead / latestBranch may have changed, among others
            osd = ObjectSystemData.get((String) params.get("_RelationChangeTrigger_delete_id_"));
        }
        else if (action.equals("create")) {
            // latestHead / latestBranch may have changed, among others
            osd = poBox.model.osd; // TODO: make sure /osd/create does use 'osd' as model name.
        }

        if (osd == null) {
            log.debug("target of " + action + " not found");
        }
        else {
            log.debug("update relations if necessary")
            RelationService relationService = poBox.grailsApplication.getMainContext().getBean('relationService')
            relationService.updateRelations(osd);
        }
        return poBox;
    }

    static Map<String, String> commandIdMap;
    static {
        commandMapBuilder();
    }

    static void commandMapBuilder() {
        commandIdMap = new HashMap<String, String>();
        commandIdMap.put("setmeta", "id");
        commandIdMap.put("setcontent", "id");
        commandIdMap.put("version", "preid");
        commandIdMap.put("setsysmeta", "id");        
        commandIdMap.put("newVersion", "osd");
    }
}

