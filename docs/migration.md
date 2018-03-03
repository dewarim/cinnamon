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
    
You must also update all user accounts who must not have changeTracking, for example by running

    update users set change_tracking=true where name in ('admin'); 

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

### Update to Tomcat8 (Ubuntu instructions)
 
If you are using the Ubuntu 14.04 edition, you will need to install Tomcat 8. 
Use the instructions from 
https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-on-ubuntu-14-04
but change the following steps:

* first, use 'sudo service tomcat7 stop'
* do not install Java anew, as we already got a working version when migrating.
* download directly from the Apache web page to get the newest version:
    http://tomcat.apache.org/download-80.cgi
* in the upstart script, use the following env settings:
  # Modify these options as needed                                                                                                                                                                                                                                               
  env CINNAMON_HOME_DIR=/opt/cinnamon/cinnamon-system
  env JAVA_OPTS="-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom"
  env CATALINA_OPTS="-Xms512M -Xmx2048M -server -XX:+UseParallelGC"
* in the upstart script, make sure that the JVM used is Java 8.
  for example: env JAVA_HOME=/usr/lib/jvm/java1.8.0 (if that is your Java 8 installation)
* afterwards, do: sudo chown -Rv tomcat:tomcat /opt/tomcat/webapps
* use sysv-rc-conf to disable tomcat7.
* reboot

## changes to 3.7

### Lucene: improve indexing of folders and objects

* Stop the server.
* Delete your Lucene index after applying the following changes.
* Store value of owner and acl fields in index:
 
    update index_items set store_field=true where fieldname in ('owner','acl');

* Fix a problem where ownerId is indexed as text:

    update index_items set index_type_id=(select it.id from index_types it where it.name='xpath.integer_indexer') where fieldname = 'owner';
    
* Apply the database changes shown below
* Restart the Cinnamon server.
* Re-Index everything: 

    insert into index_jobs select id,false,'cinnamon.ObjectSystemData',id from objects;
    insert into index_jobs select id,false,'cinnamon.Folder',id from folders;
    
### Authentication: LDAP logins

Note: Work in progress.
Note: Code for LDAP authentication requires licensing for commercial use beyond testing.

* Change database to allow LDAP logins
    
        ALTER TABLE public.users ADD login_type VARCHAR(64) DEFAULT 'CINNAMON' NOT NULL;
        
* Edit ldap-config.xml (see: [ldap-confing.example.xml](../ldap-config.exampl.xml)) in CINNAMON_HOME_DIR
  (that is, into the same directory as cinnamon-config.groovy). If you do not need or want LDAP connectivity,
  you do not need to do anything beyond the previous database change.
  
  