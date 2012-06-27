# Configuration of the Cinnamon server

This text describes the main configuration file of the Cinnamon server (version 2.x).

<style type="text/css">
 .codeBlock{ background-color:lightblue;padding:1ex;border:1px solid black;}
</style>

The cinnamon_config.xml file is the main configuration file of the Cinnamon server. It is used for global settings like the database driver and connection URL as well as the settings for specific repositories and their API settings. All currently available elements are listed below, along with an example file.

Please note that plugins may query the configuration file for information, too.
## Server wide configuration settings

### data_root

The path to where the Cinnamon server should store the content of documents. Note that currently uploaded data is saved in this place in the file system, not in the database. The database holds the metadata (both custom and systemic), and the content is stored on disc in a folder structure which consists of: $data_root + $repository_name + $UUID-folder/filename

### encryptPasswords

If set to true, all passwords of new user accounts will be stored in encrypted form (as a jBcrypt-hash). All login passwords will be hashed and checked against the database. If you still have legacy user accounts with clear text passwords or you are a server developer, you may opt to have this set to false. Otherwise, you should really enable encryption of passwords.

### logback_configuration_path

The path to your Logback configuration file. Cinnamon uses Logback for logging (if possible, some legacy APIs or Apache libs still use commons-logging).

### maxTokensPerDay

Every time a user requests a link to reset his or her password or verify an email address, a unique token is generated. The maxTokensPerDay setting defines how often a user may call one of those API methods. The default is 3 times per day. This limit exists so that a malicious person cannot request thousands of "reset your password"-emails.

### minimalPasswordLength

The minimal length which is required of a user's password. The default is 4 (for historic reasons). Change this to improve password safety.

### passwordRounds

The number of rounds used to generate the bcrypt salt. Valid values are from 4..31, the default is 12. Password hashing takes exponentially longer the higher this number is. The passwordRounds parameter is only useful if you encrypt passwords.

### server-url

The URL where this server can be contacted, for example: http://localhost:8080/cinnamon/cinnamon. This is currently used for the links inside mails for password reset or email verification.

### startIndexServer

If this element is set to true, an extra Thread is started for each repository. It contains the IndexServer, a class which is used for asynchronous indexing of the database. Usually, all changes to documents and folders are picked up by the LuceneBridge class and the index is refreshed synchronously, that is: as soon as you update an object, its index is updated too. But when you make changes to the way Cinnamon indexes objects or if your Lucene index becomes corrupted (for example, due to a disc failure), you may need to regenerate the index as a whole. This is the job of the IndexServer, which will re-index each object and folder which has its index_ok column set to NULL.

If the IndexServer encounters a permanent problem (for example, an object with a broken XML content file), it will set the index_ok field of this object or folder to 'false'. If the item was successfully indexed, the field is set to 'true'.

### startWorkflowServer

If this element is set to true, an extra Thread is started for each repository. It contains the WorkflowServer, which is running in the background (of the background server process...) and is responsible for the automatic transition of workflow tasks from one state to another. This feature only needs to be enabled if you are actually using the workflow engine of Cinnamon.

### system_root

The file system path to the place where you are want to store the cinnamon system files (like log files). On Windows systems, you need to escape "\", for example: "C:\\system-root".

### system-administrator

Email address of the system's administrator. Used as default for the admin user's email address by the Initializer class. May be used as contact information in error messages.

### use_session_logging

Set this to 'true' to request separate log files per session. See below for an example Logback configuration file which contains the necessary setup to create those log files.

## Global database settings

At the moment, a Cinnamon server will connect to only one database. (Unless you configure custom connections).

The data needed (along with the given examples) should be self explanatory.

*    sql_host = 127.0.0.1
*    sql_user = cinnamon
*    sql_pwd = cinnamon
*    db_type = postgresql (or: mssql mssql2000 mysql)
*    jdbc_driver = one of:
    * net.sourceforge.jtds.jdbc.Driver
    * com.mysql.jdbc.Driver
    * org.postgresql.Driver

Other database drivers will most likely work if they are supported by Hibernate, but they are currently untested.

## Email configuration

For Cinnamon to be able to send emails (for email verification or workflow events), you have to configure a smtp account.

<div class="codeBlock">

    <mail>
        <from>CinnamonServer@lolhost</from>
  	<smtp-host>example.invalid</smtp-host>  
  	<user>cinnamon</user>  
  	<password>_cinnamon's_password</password>  
    </mail>
    
</div>

* from = this is added as the sender for mails from the server.
* smtp-host = the domain name of your mail server (like cinnamon-cms.de)
* user = the user account on the mail server via which mails will be sent.
* password = the password for the mail account.

## Repositories

Each repository consists of a database, a folder in $data_root and a Lucene index. The repository element should have the following child nodes:

*    name = name of the database, for example cmn_test
*    auto-initialize = if set to "true", this will initialize an empty repository with the necessary data structures. The database for the repository must exist before you start the server.
*    initialize-tests = if set to "true", the auto-initialize-feature
     (if enabled) will setup some objects for the test scripts.
*    sessionExpirationTime = how long an inactive user session is valid (in milliseconds). Upon a user's action, the session timeout is reset to this value.
*    persistence_unit = should be set to 'cinnamon'. For testing purposes developers may use 'cmn_test'.
     It is best to leave this one alone, unless you are about to make changes in META-INF/persistence.xml.
*    [optional] sudoers = contains a list of 'name' elements of users who are allowed to use the sudo API method.
*    apiClasses = you can enable or disable specific parts of the Cinnamon API by including or excluding modules. The only required 'apiClass' element is the one with "server.CmdInterpreter" as it's responsible for login to the server. Most other currently available modules enable administrative interfaces used for testing (but which may also be used for custom development or extensions).
    Current modules:
    * server.extension.Initializer (used to create basic IndexItems, Admin user, Permissions etc. Turns a blank repository into a rudimentary useful one).
    * server.extension.Translation
    * server.extension.TransformationEngine
    * server.extension.WorkflowAPI
    * server.extension.QueryCustomTable: old API which allows logged in users to send direct SQL statements to the database on preconfigured database connections. This is a potential security hole and should not be enabled, unless you absolutely need it. 
    * server.extension.admin.UserManager: Allows you to create user accounts. For a nice GUI to do this, look at Dandelion, the AdminTool.
    * server.extension.admin.ObjectTypeManager
    * server.extension.admin.AclManager
    * server.extension.admin.PermissionManager
    * server.extension.admin.FormatManager
    * server.extension.admin.GroupManager
    * server.extension.admin.RelationTypeManager
    * server.extension.admin.FolderTypeManager
    * server.extension.LifeCycleApi
    
## Example cinnamon_config.xml
[Download cinnamon_config.xml](cinnamon_config.xml)
<div class="codeBlock">

    <?xml version="1.0" encoding="utf-8"?>
    
    <cinnamon_config>
      <startIndexServer>true</startIndexServer>
      <startWorkflowServer>false</startWorkflowServer>
      <encryptPasswords>true</encryptPasswords>

      <system_root>/home/ingo/cinnamon/system/</system_root>
      <data_root>/home/ingo/cinnamon/data/</data_root>

      <mail>
      	<smtp-host>example.invalid</smtp-host>
      	<user>cinnamon</user>
      	<password>_cinnamon's_password</password>
      </mail>

      <repositories>

        <repository>
            <name>cmn_dev</name>
            <persistence_unit>cinnamon</persistence_unit>
        </repository>

        <repository>
            <name>cmn_test</name>
            <auto-initialize>true</auto-initialize>
            <persistence_unit>cinnamon_test</persistence_unit>
            <sessionExpirationTime>360000</sessionExpirationTime>
            <apiClasses>
                <!-- The only obligatory apiClass: -->
	        <apiClass>server.CmdInterpreter</apiClass>
            </apiClasses>
            <sudoers>
                <name>admin</name>
            </sudoers>
        </repository>
    </repositories>

    <logback_configuration_path>/home/cinnamon/logback.xml</logback_configuration_path>

    <!-- Postgres: -->
    <db_type>postgresql</db_type>
    <jdbc_protocol>postgresql</jdbc_protocol>
    <jdbc_driver>org.postgresql.Driver</jdbc_driver>
    <sql_host>172.16.168.134</sql_host>
    <sql_user>cinnamon</sql_user>
    <sql_pwd>cinnamon</sql_pwd>

    <use_session_logging>false</use_session_logging>

    </cinnamon_config>

</div>

### Example logback.xml
[Download logback.xml](logback.xml)
<div class="codeBlock">

    <configuration>
      <property name="logFolder" value="/home/zimt/cinnamon-system/global/log"/>
 
	<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <layout class="ch.qos.logback.classic.PatternLayout">
	    <Pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</Pattern>
       </layout>
      </appender>
  
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender"> 
     <File>${logFolder}/cinnamon.log</File> 
      <Append>true</Append>  
      <layout class="ch.qos.logback.classic.PatternLayout"> 
       <Pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</Pattern> 
        </layout>
    
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <FileNamePattern>${logFolder}/${session}.%d{yyyy-MM-dd}.gz</FileNamePattern>
      <!-- keep 30 days' worth of history -->
      <MaxHistory>30</MaxHistory>
    </rollingPolicy>
       
    </appender> 

    <appender name="SIFT" class="ch.qos.logback.classic.sift.SiftingAppender">
      <discriminator>  
      	<Key>session</Key>
       	<DefaultValue>unknown_session</DefaultValue>
      </discriminator>
      <sift>  
      	<appender name="FILE-${session}" class="ch.qos.logback.core.rolling.RollingFileAppender">
      	  <File>${logFolder}/${session}.log</File>
      	  <Append>true</Append>
      	  <ImmediateFlush>true</ImmediateFlush>
              <!--  false == 1.4 to 3 times faster, some risk of message loss on crash -->
  	  
      	 <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
          <FileNamePattern>${logFolder}/${session}.%d{yyyy-MM-dd}.gz</FileNamePattern>
          <!-- keep 30 days' worth of history -->
          <MaxHistory>30</MaxHistory>
        </rollingPolicy>  	  
  	    
       <layout class="ch.qos.logback.classic.PatternLayout">
       	<!-- note: remove %L to increase speed at the expense of loosing the line numbers.-->  
           <Pattern>%-4relative [%thread] %-5level %mdc{user} %mdc{repo} %logger{35} %L - %msg%n</Pattern>
        </layout>
        </appender>
      </sift>
    </appender> 


      <root level="DEBUG">
        <appender-ref ref="FILE" />
      </root>
     <logger name="org.mortbay.log" level="INFO"/>
     <logger name="org.hibernate" level="INFO"/>
     <logger name="server.CmdInterpreter" level="DEBUG"/>

    </configuration>

</div>

This logger outputs all log messages to the location specified in the 'File' element. It also can generate one log file per session.

See http://logback.qos.ch/ for more information about how to configure Logback.

You can change the 'root' level from "DEBUG" to "INFO" or "ERROR" to get Cinnamon to be a little more quiet.

