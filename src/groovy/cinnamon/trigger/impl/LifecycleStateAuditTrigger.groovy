package cinnamon.trigger.impl

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory
import cinnamon.trigger.ITrigger
import cinnamon.PoBox
import cinnamon.ObjectSystemData
import cinnamon.SqlCustomConn
import cinnamon.CustomTable
import cinnamon.lifecycle.LifeCycleState
import java.sql.PreparedStatement
import java.sql.Connection
import java.sql.Timestamp
import cinnamon.exceptions.CinnamonException
import cinnamon.utils.ParamParser;

/**
 * Log changes to an object's lifecycle state into a special table.
 *
 */

/*
   Installation hints:
  create table lifecycle_log(id serial  primary key, repository character varying(255) not null,
  hibernate_id bigint not null, 
  user_name varchar(255) not null, user_id bigint not null, date_created timestamp without time zone not null, 
  lifecycle_id bigint not null, lifecycle_name varchar(255) not null, old_state_id bigint not null, 
  old_state_name varchar(255) not null, new_state_id bigint not null, new_state_name varchar(255) not null, 
  folder_path varchar(8191) not null, name varchar(255) not null );
  
  Table "public.lifecycle_log"
     Column     |            Type             | Modifiers 
----------------+-----------------------------+-----------
 id             | bigint                      | not null default nextval('lifecycle_log_id_seq'::regclass)
 repository     | character varying(255)      | not null
 hibernate_id   | bigint                      | not null
 user_name      | character varying(255)      | not null
 user_id        | bigint                      | not null
 date_created   | timestamp without time zone | not null
 lifecycle_id   | bigint                      | not null
 lifecycle_name | character varying(255)      | not null
 old_state_id   | bigint                      | not null
 old_state_name | character varying(255)      | not null
 new_state_id   | bigint                      | not null
 new_state_name | character varying(255)      | not null
 folder_path    | character varying(8191)     | not null
 name           | character varying(255)      | not null
 
    Indexes:
        "lifecycle_log_pkey" PRIMARY KEY, btree (id)
 
 insert into customtables values(1,'jdbc:postgresql://127.0.0.1/demo?user=cinnamon&password=cinnamon',
 'org.postgresql.Driver','audit.connection', 0, 3)

 insert into change_trigger_types(id, description, name, trigger_class) values(
 2, 'Lifecyclestate Audit ChangeTrigger','lifecycle.state.trigger','server.trigger.impl.LifecycleStateAuditTrigger');

 insert into change_triggers values(6,true,0,100,2,'changestate', true,true,'<config />');
  grant SELECT,INSERT on lifecycle_log TO cinnamon ;
  grant USAGE on lifecycle_log_id_seq TO cinnamon ;
 * 
 */

public class LifecycleStateAuditTrigger implements ITrigger {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public PoBox executePreCommand(PoBox poBox, String config) {
        log.debug("preCommand of LifecycleStateAuditTrigger");

        if(poBox.action.equals("changestate")){
            try{
                ObjectSystemData osd = ObjectSystemData.get((String) poBox.params.get("id"));
                if(osd.getState() != null){
                    poBox.params.put("_old_lifecycle_state_id", osd.getState().getId());
                    poBox.params.put("_old_lifecycle_state_name", osd.getState().getName());
                }
            }
            catch (Exception e){
                log.debug("Failed to load osd.");
            }
        }
        return poBox;
    }

    @Override
    public PoBox executePostCommand(PoBox poBox, String config) {
        log.debug("postCommand of LifecycleStateAuditTrigger");
        Map<String, Object> params = poBox.params;
        String command = poBox.action;

        ObjectSystemData osd = null;
        if (commandIdMap.containsKey(command) && params.containsKey(commandIdMap.get(command))) {
            osd = ObjectSystemData.get((String) params.get(commandIdMap.get(command)));
        }

        CustomTable auditTable = CustomTable.findByName('audit.connection')
        SqlCustomConn auditConnection = new SqlCustomConn(auditTable.connstring, auditTable.jdbcDriver, auditTable.acl)
        if(auditConnection == null){
            log.debug("audit db connection does not exist - skip audit logging.");
            return poBox;
        }

        if (osd == null) {
            log.debug("target of " + command + " not found, nothing to do.");
            return poBox;
        }

        try{
            String oldName = "-";
            Long oldId = 0l;
            if(poBox.params.containsKey("_old_lifecycle_state_id")){
                oldId = (Long) poBox.params.get("_old_lifecycle_state_id");
            }
            if(poBox.params.containsKey("_old_lifecycle_state_name")){
                oldName = (String)  poBox.params.get("_old_lifecycle_state_name");
            }
            String newName = "-";
            Long newId = 0L;
            String lifecycleName = "-";
            Long lifecycleId = 0L;
            if(osd.getState() != null){
                LifeCycleState state = osd.getState();
                newName = state.getName();
                newId = state.getId();
                lifecycleName = state.getLifeCycle().getName();
                lifecycleId = state.getLifeCycle().getId();
            }

            if(filterLogMessage(config,lifecycleId, oldId, newId )){
                log.debug("filter log message.");
                return poBox;
            }

            Connection connection = auditConnection.getConnection();
            PreparedStatement stmt =
                connection.prepareStatement("insert into lifecycle_log (repository, hibernate_id, user_name, user_id, date_created, lifecycle_id, lifecycle_name, old_state_id, old_state_name, new_state_id, new_state_name, folder_path, name) values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
            stmt.setString(1, poBox.repository);
            stmt.setLong(2, osd.getId());
            stmt.setString(3, poBox.user.getName());
            stmt.setLong(4, poBox.user.getId());
            stmt.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
            stmt.setLong(6,lifecycleId);
            stmt.setString(7, lifecycleName);
            stmt.setLong(8, oldId);
            stmt.setString(9, oldName);
            stmt.setLong(10, newId);
            stmt.setString(11, newName);
            stmt.setString(12, osd.getParent().fetchPath());
            stmt.setString(13, osd.getName());
            int rows = stmt.executeUpdate();
            log.debug("executeUpdate changed: "+rows+" rows");
            stmt.close();
        }
        catch (Exception ex){
            log.debug("failed log lifecycle state change event", ex);
            throw new CinnamonException("error.log.lifecycle.fail",ex);
        }

        return poBox;
    }

    /**
     * Determine whether a log message should be filtered, depending on the ChangeTrigger's configuration.
     * A node "logEverything" with content "true" will approve all log messages.
     * @param config the configuration String
     * @param lifecycleId the current lifecycle of the OSD
     * @param oldStateId the old lifecycle state id , may be null or empty
     * @param newStateId the new lifecycle state id, may be null or empty
     * @return true if the log message should be filtered (not stored in the database), false otherwise.
     */
    Boolean filterLogMessage(String config, Long lifecycleId, Long oldStateId, Long newStateId ){
        // only log configured changes:
        Document filterConfig = ParamParser.parseXmlToDocument(config);
        Node logAll = filterConfig.selectSingleNode("//logEverything[text()='true']");
        if(logAll != null){
            log.debug("Found logEverything directive: approve log message");
            return false;
        }

        Node lifecycle = filterConfig.selectSingleNode("//lifecycles/lifecycle[@id='"+lifecycleId+"']");
        if(lifecycle == null){
            log.debug("Lifecycle is not configured for logging: deny logging.");
            return true;
        }
        Node oldState = lifecycle.selectSingleNode("stateId[text()='"+oldStateId+"']");
        Node newState = lifecycle.selectSingleNode("stateId[text()='"+newStateId+"']");
        if(oldState != null || newState != null){
            log.debug("Lifecycle state change will be logged.");
            return false;
        }
        log.debug("Could not find a reason to approve this log event.");
        return true;
    }

    static Map<String, String> commandIdMap;

    static {
        commandMapBuilder();
    }

    static void commandMapBuilder() {
        commandIdMap = new HashMap<String, String>();
        commandIdMap.put("changestate", "id");
    }



}