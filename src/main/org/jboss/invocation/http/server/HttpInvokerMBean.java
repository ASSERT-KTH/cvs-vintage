package org.jboss.invocation.http.server;

import java.net.URL;

import org.jboss.invocation.Invocation;
import org.jboss.system.ServiceMBean;

/** The MBean interface for the HTTP invoker.
 @author Scott.Stark@jboss.org
 @version $Revision: 1.2 $
 */
public interface HttpInvokerMBean extends ServiceMBean
{
   public String getInvokerURL();
   public void setInvokerURL(String invokerURL);

   public Object invoke(Invocation invocation)
      throws Exception;
}
