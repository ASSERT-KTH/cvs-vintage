# all clients need this
CP=../client/ejb.jar
CP=$CP:../client/jndi.jar
CP=$CP:../client/jta-spec1_0_1.jar
CP=$CP:../client/jboss-client.jar
CP=$CP:../client/jnp-client.jar
CP=$CP:..

# only the test client needs the following
CP=$CP:../client/TestBeanClient.jar
CP=$CP:.:..:../client/AccountClient.jar


java -cp $CP  org.jboss.zol.testbean.client.EjbossClient  %1 %2 %3 %4 %5 %6 %7 %8 %9

# echo Running Account EJB Tests...
# echo These will test EJB references, findByPrimaryKey operations, simple transaction
# echo rollbacks on failed entity creation and SQL INSERT, SELECT and UPDATE operations.
# echo.

#java -cp $CP  org.jboss.zol.accountmanager.client.AccountManagerClient  %1 %2 %3 %4 %5 %6 %7 %8 %9

