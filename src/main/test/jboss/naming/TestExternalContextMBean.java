package test.jboss.naming;

/** 

@author Scott_Stark@displayscape.com
@version $Revision: 1.1 $
*/
public interface TestExternalContextMBean extends org.jboss.util.ServiceMBean
{
    public void testExternalContexts() throws Exception;
}
