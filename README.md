"# extc

THIS PROJECT IS UNDERGOING SIGNIFICANT CHANGE TO ADAPT TO SEVERAL
YEARS WORTH OF CHANGES TO ECLIPSE.  THIS FILE IS UNDER CONSTRUCTION.

http://stackoverflow.com/questions/6177034/how-do-i-install-eclipse-pde

Installing Eclipse for RCP and RAP Developers (Neon)
Import-> General -> Existing Projects into Workspace
Choose the GitHubSource directory to import. (This is the
ClassRefactoringPlugin project)

494 Errors
Choose quick-fix for one of the assert* errors
       Add org.junit to imported packages
       Properties -> Libraries -> Add Library -> JUnit -> JUnit4

264 Errors
net.sourceforge.metrics.* classes aren't found
        Put net.sourceforge.metrics*.jar in the dropins folder
        Put 3 org.appach.derby* jars in the dropins folder
        TEMP put classRefactoringPlugin* in the dropins folder
        Properties -> Java Build Path -> Add Library -> Plug-in Dependencies
6 errors from MANIFEST.MF
Derby database classes aren't found
      https://db.apache.org/derby/integrate/plugin_howto.html

Add support for older plugins as described in
http://stackoverflow.com/questions/18767831/while-installing-plugin-in-eclipse-luna-unable-to-acquire-pluginconverter-serv
via the command:
eclipse -nosplash -application org.eclipse.equinox.p2.director \
    -repository http://download.eclipse.org/eclipse/updates/4.4/ \
    -installIU org.eclipse.osgi.compatibility.plugins.feature.feature.group

There are dependencies on libraries:
<list>

Add plug-in dependencies: Properties -> Java Build Path -> Libraries
jdbm.jar
metrics.jar
derby.jar (c:\eclipse\dropins)
derbyclient.jar
derbytools.jar
derbynet.jar

In older Eclipse, set the project Builders to:
Java Builder
Plug-in Manifest Builder
Extension Point Schema Builder


Install derby database C:\Tools\Derby\db-derby-10.5.3.0-bin
Problems with starting the server due to socketPermission problem,
addressed by
http://stackoverflow.com/questions/21154400/unable-to-start-derby-database-from-netbeans-7-4. Changed
$HOME/.java.policy
grant {
    permission java.net.SocketPermission "localhost:1527", "listen";
};

How do we build the database?  Where is the schema?  Maybe we can
recreate it from the existing database.
