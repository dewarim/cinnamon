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