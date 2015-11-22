# Migration steps for Cinnamon

Current starting point: server vestion 3.1.1

## Changes to 3.6:

### Database changes

#### Change tracking

    -- changeTracking --
    alter table users add column change_tracking boolean not null default true;
    alter table objects add column content_changed boolean not null default false;
    alter table objects add column metadata_changed boolean not null default false;
    alter table folders add column metadata_changed boolean not null default false;
    
You must also update all user accounts who require changeTracking, for example by running

    update users set change_tracking=true where name in ('alice', 'bob', 'eve') 

Alternatively, just set change_tracking to true for everyone except the admin accounts.
   

#### Remove deprecated OSD.metadata
 
    alter table objects drop column metadata;
    alter table folders drop column metadata;

#### ChangeTriggers
    
    insert into change_trigger_types values( (select max(id)+1 from change_trigger_types),
    'MicroserviceChangeTriggerType', 0, 'cinnamon.trigger.impl.MicroserviceChangeTrigger');

    -- only needed for testing:
    insert into change_triggers values( (select max(id)+1 from change_triggers), true, 0, 100, (select id from 
    change_trigger_types where name='MicroserviceChangeTriggerType'), 'echo', true, false, 
    '<config><remoteServer>http://localhost:8080/cinnamon/test/microserviceChangeTriggerPreRequestTest</remoteServer></config>', 'test');    
     insert into change_triggers values( (select max(id)+1 from change_triggers), true, 0, 100, (select id from 
     change_trigger_types where name='MicroserviceChangeTriggerType'), 'echo', false, true, 
     '<config><remoteServer>http://localhost:8080/cinnamon/test/microserviceChangeTriggerPostRequestTest</remoteServer></config>', 'test');


#### new column 'summary' for folders and objects

    alter table objects add column summary text not null default '<summary />';
    alter table folders add column summary text not null default '<summary />';
    
    