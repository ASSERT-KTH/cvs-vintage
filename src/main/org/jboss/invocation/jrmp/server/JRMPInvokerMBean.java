/******************************************************
 * File: JRMPInvokerMBean.java
 * created Nov 29, 2001 6:37:14 AM by marcf
 */
package org.jboss.invocation.jrmp.server;

import org.jboss.system.ServiceMBean;

public interface JRMPInvokerMBean
extends ServiceMBean
{
   
   public void setRMIObjectPort(int rmiPort); 
   public int getRMIObjectPort();
   
   public void setRMIClientSocketFactory(String name);
   public String getRMIClientSocketFactory();
   
   public void setRMIServerSocketFactory(String name);
   public String getRMIServerSocketFactory();
   
   public void setServerAddress(String address);
   public String getServerAddress();
   
   public String getName();
   
   // Add management methods at this level, 

}

   