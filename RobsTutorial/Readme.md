# Robs Tutorial
Get started learning stuff bc im dumb

Build and Deploy RobsTutorial
-------------------------

1. Make sure you have started the JBoss EAP / WildFly server
2. Open a command prompt and navigate to the root directory of this microservice.
3. Type this command to build and deploy the archive:

        mvn clean package wildfly:deploy

4. This will deploy `target/robstutorial.war` to the running instance of the server.

Access the application
----------------------

The application will be running at the following URL: <http://localhost:8080/robstutorial>

Undeploy the Archive
--------------------

1. Make sure you have started the JBoss EAP server as described above.
2. Open a command prompt and navigate to the root directory of this quickstart.
3. When you are finished testing, type this command to undeploy the archive:

        mvn wildfly:undeploy
