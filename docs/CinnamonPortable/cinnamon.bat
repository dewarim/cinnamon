@ECHO ON

REM create checkout folder if necesary:
IF exist "%HOMEDRIVE%\%HOMEPATH%\cinnamon\nul" ( echo cinnamon folder exists ) ELSE ( mkdir "%HOMEDRIVE%\%HOMEPATH%\cinnamon" && echo cinnamon folder created)
IF exist "%HOMEDRIVE%\%HOMEPATH%\cinnamon\checkout\nul" ( echo checkout folder exists ) ELSE ( mkdir "%HOMEDRIVE%\%HOMEPATH%\cinnamon\checkout" && echo checkout folder created)

REM set JAVA environment:
@SET JAVA_HOME="%~dp0\jre1.8.0_111"
@SET JRE_HOME="%~dp0\jre1.8.0_111"
@SET CATALINA_HOME="%~dp0\tomcat7"
@SET DITA_DIR="%~dp0\DITA-OT1.5.4"
@SET PATH=%JRE_HOME%\bin;"%~dp0\DITA-OT1.5.4\tools\ant\bin";"%~dp0\postgres9\bin";%PATH%;
@SET JAVA_OPTS=" -Xms800M -Xmx800M -XX:MaxPermSize=256M"
@SET ANT_OPTS=-Xmx800m -Djavax.xml.transform.TransformerFactory=net.sf.saxon.TransformerFactoryImpl
@SET CLASSPATH=.;"%~dp0\DITA-OT1.5.4\lib\saxon\saxon9-xom.jar";"%~dp0\DITA-OT1.5.4\lib\saxon\saxon9-xpath.jar";"%~dp0\DITA-OT1.5.4\lib\saxon\saxon9-xqj.jar";"%~dp0\DITA-OT1.5.4\lib\saxon\saxon9-s9api.jar";"%~dp0\DITA-OT1.5.4\lib\saxon\saxon9-sql.jar";"%~dp0\DITA-OT1.5.4\lib\saxon\saxon9-dom4j.jar";"%~dp0\DITA-OT1.5.4\lib\saxon\saxon9-jdom.jar";"%~dp0\DITA-OT1.5.4\lib\saxon\saxon9.jar";"%~dp0\DITA-OT1.5.4\lib\saxon\saxon9-dom.jar";"%~dp0\DITA-OT1.5.4\lib";"%~dp0\DITA-OT1.5.4\lib\dost.jar";"%~dp0\DITA-OT1.5.4\lib\resolver.jar";"%~dp0\DITA-OT1.5.4\lib\icu4j.jar";
@SET ANT_HOME="%~dp0\DITA-OT1.5.4\tools\ant"

REM set cinnamon vars:
@SET CINNAMON_HOME_DIR=%~dp0\cinnamon
@SET DANDELION_HOME_DIR=%~dp0\cinnamon

REM The script sets environment variables helpful for PostgreSQL´
@SET PGDATA=%~dp0\postgres9\data
@SET PGDATABASE=postgres
@SET PGUSER=postgres
@SET PGPORT=5432
@SET PGLOCALEDIR=%~dp0\postgres9\share\locale
REM "%~dp0\postgres9\bin\initdb" -U postgres -A trust
"%~dp0\postgres9\bin\pg_ctl" -D "%~dp0/postgres9/data" -l logfile start

cd "%~dp0\jetty8"
start java -Xms2G -Xmx2G -jar start.jar
cd "%~dp0"

REM following command commented out since server may take a minute to start, and the client 
REM would fail to find the server in the meantime.
REM "%~dp0\client\CinnamonDesktopClient.exe"

ECHO "Press enter to stop"
pause
"%~dp0\postgres9\bin\pg_ctl" -D "%~dp0/postgres9/data" stop
