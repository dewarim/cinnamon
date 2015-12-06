    -- changeTracking --
    alter table users add column change_tracking boolean not null default true;
    alter table objects add column content_changed boolean not null default false;
    alter table objects add column metadata_changed boolean not null default false;
    alter table folders add column metadata_changed boolean not null default false;

-- set change_tracking to false for admin user;
-- if you got other users with administrative permissions, add their names 
-- in a comma separated list, for example ('admin', 'renderer')
    update users set change_tracking=false where name in ('admin');

-- Remove deprecated OSD.metadata
 
    alter table objects drop column metadata;
    alter table folders drop column metadata;
  
    insert into change_trigger_types values( (select max(id)+1 from change_trigger_types),
    'MicroserviceChangeTriggerType', 0, 'cinnamon.trigger.impl.MicroserviceChangeTrigger');

    -- only needed for testing:
    insert into change_triggers values( (select max(id)+1 from change_triggers), true, 0, 100, (select id from 
    change_trigger_types where name='MicroserviceChangeTriggerType'), 'echo', true, false, 
    '<config><remoteServer>http://localhost:8080/cinnamon/test/microserviceChangeTriggerPreRequestTest</remoteServer></config>', 'test');    
    insert into change_triggers values( (select max(id)+1 from change_triggers), true, 0, 100, (select id from 
    change_trigger_types where name='MicroserviceChangeTriggerType'), 'echo', false, true, 
    '<config><remoteServer>http://localhost:8080/cinnamon/test/microserviceChangeTriggerPostRequestTest</remoteServer></config>', 'test');

--  new column 'summary' for folders and objects

    alter table objects add column summary text not null default '<summary />';
    alter table folders add column summary text not null default '<summary />';
    

