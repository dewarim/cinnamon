# Migration steps for Cinnamon

Current starting point: server vestion 3.1.1

## Changes to 3.2:

### Database changes

#### Change tracking

    -- changeTracking --
    alter table users add column change_tracking boolean not null default false;
    alter table objects add column content_changed boolean not null default false;
    alter table objects add column metadata_changed boolean not null default false;
    alter table folders add column metadata_changed boolean not null default false;
    
You must also update all user accounts who require changeTracking, for example by running

    update users set change_tracking=true where name in ('alice', 'bob', 'eve') 

Alternatively, just set change_tracking to true for everyone except the admin accounts.
   

#### Remove deprecated OSD.metadata
 
    alter table objects drop column metadata;
    alter table folders drop column metadata;

    