# Installation from source: Cinnamon server 2

## Dependencies

You will need the following:

* Java 6 JDK (better: Java 7) 
* An RDBMS (recommended: PostgreSQL or MSSQL), although
  it should run with any Hibernate compatible RDBMS (DB2, Oracle, MySQL etc)
* A servlet container (Tomcat 7, alternatives: Jetty, Glassfish)
* A lot of open source libraries, so it's recommended you download the developer library bundle. The required files and their versions can be seen in the build.exmple.xml in the server's source package.

## Checkout the source code

The Cinnamon server is split into several modules:
   
+ CinnamonBase (essential classes used in all modules)
+ Dandelion (The administration GUI - a Grails application)
+ EntityLib (the database classes)
+ Humulus (Login and Database connection management for Dandelion and Illicium)
+ Illicium (rudimentary webclient - a Grails application)
+ Safran (Java client library, used for testing and direct API access)
+ Server (the View and Controller)    
+ Tools (optional code for migration and repository cleanup)
+ Utils (Configuration, constans, database session management)

To install from source, you will need to check out and compile at least the following projects: CinnamonBase, EntityLib, Server.

The Repositories for the projects are:

    https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/CinnamonBase/trunk
    https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Server/trunk
    https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/EntityLib/trunk
    https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Utils/trunk
    https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Dandelion/trunk
    https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Humulus/trunk
    https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Illicium/trunk    
    https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/CinnamonClient/Safran/   
    https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Utils/trunk
    https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Tools/trunk    

Currently, we use Subversion as our version control system (VCS), so you will need to either install its command line client or a GUI tool for it.

In your Workspace, on the command line, issue the following commands:

    svn co https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Server/trunk/. Server/.
    svn co https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/EntityLib/trunk/. EntityLib/.
    svn co https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Utils/trunk/. Utils/.
    svn co https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/CinnamonBase/trunk/. CinnamonBase/.
    svn co https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/CinnamonClient/Safran
    
If you want to install the Grails applications for administration and web access, you may also add:

    svn co https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Dandelion/trunk/. Dandelion/.
    svn co https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Humulus/trunk/. Humulus/.
    svn co https://cinnamon.svn.sourceforge.net/svnroot/cinnamon/Illicium/trunk/. Illicium/.		
    
## Configure the build environment

### edit ant build script
Please edit the ant build script in Server/build.example.xml to match your environment (and copy it to Server/build.xml).
The script is intended to be executed in the parent folder of Server/, so the paths should be relative to that.

### edit persistence.xml
Edit the hibernate persistence configuration in Server/META-INF/persistence. There are three variables that should be set
via the build.xml file, and if you are going to use the default configuration (postgres), you do not need to change them:

1. hibernate.dialect
2. hibernate.connection.driver_class
3. hibernate.jdbc.use_streams_for_binary

The variables are set in the "use$databaseName" targets:

    <target name="usePostgres" depends="copyPersistenceSource">
        <replace file="${server.home}/META-INF/persistence.xml" token="@hibernate.dialect@"
                 value="org.hibernate.dialect.PostgreSQLDialect"/>
        <replace file="${server.home}/META-INF/persistence.xml" token="@hibernate.connection.driver_class@"
                 value="org.postgresql.Driver"/>
        <replace file="${server.home}/META-INF/persistence.xml" token="@hibernate.jdbc.use_streams_for_binary@"
                 value="false"/>
        <property name="lib.database" value="${lib.postgres}"/>
        <copy todir="${build.home}/WEB-INF/lib" file="${lib.database}"/>
    </target>

And they are used in the persistence.xml:

     <property name="hibernate.dialect" value="@hibernate.dialect@"/>
     <property name="hibernate.connection.driver_class" value="@hibernate.connection.driver_class@"/>
     <property name="hibernate.jdbc.use_streams_for_binary" value="@hibernate.jdbc.use_streams_for_binary@"/>

### edit c3p0.properties
In EntityLib, copy c3p0.example.properties to c3p0.properties and edit it. You can find out more about the available
properties in the [c3p0 documentation](http://www.mchange.com/projects/c3p0/) (It's a library that manages groups of database connections).

## Run the build script
Start the ant build script in the directory that contains the checked out modules of the Cinnamon server with the command:

    ant -f build.example.xml dist_public

If everything works correctly, after some seconds you should see a "BUILD SUCCESSFUL" message:

![Screenshot: Build Successful](install_00100.png)

Congratulations, you have now a ready-to-deploy cinnamon.war :)
