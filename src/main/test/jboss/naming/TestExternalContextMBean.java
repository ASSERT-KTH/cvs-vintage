package test.jboss.naming;

/** 

@author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
@version $Revision: 1.2 $
*/
public interface TestExternalContextMBean extends org.jboss.util.ServiceMBean
{
    public void testExternalContexts() throws Exception;
}
