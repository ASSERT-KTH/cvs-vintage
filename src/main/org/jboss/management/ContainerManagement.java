/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management;

/*
import java.beans.Beans;
import java.beans.beancontext.BeanContextServicesSupport;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
*/

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.transaction.TransactionManager;

import org.jboss.ejb.Container;

import org.jboss.logging.Log;

import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.logging.Logger;
                                                        
/**
*   A ContainerMgt is used as the long arm of a deployed EJB's container.
*
*   @see Container
*   @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
*   @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
*
*   @version $Revision: 1.3 $
*/
public class ContainerManagement
  extends org.jboss.util.ServiceMBeanSupport
  implements ContainerManagementMBean
{
   // Attributes ----------------------------------------------------
   // Container this is the management proxy for
   Container mContainer = null;
   // The logger of this service
   Log mLog = Log.createLog( getName() );
   ObjectName mName = null;

   // Constructor ---------------------------------------------------

   public ContainerManagement( Container pContainer ) {
      setContainer( pContainer );
   }
   
   // Public --------------------------------------------------------

   private void setContainer( Container pContainer ) {
      mContainer = pContainer;
   }
   
   public Container getContainer() {
      return mContainer;
   }
   
   /**
   * Implements the abstract <code>getObjectName()</code> method in superclass
   * to return this service's name.
   *
   * @param   server
   * @param   name
   *
   * @exception MalformedObjectNameException
   * @return
   */
   public ObjectName getObjectName( MBeanServer server, ObjectName name )
      throws javax.management.MalformedObjectNameException
   {
      mName = name;
      return name;
   }
   
   /**
   * Implements the abstract <code>getName()</code> method in superclass to
   * return the name of this object.
   *
   * @return <tt>'Container factory'</code>
   */
   public String getName() {
      return "Container Management Proxy";
   }
   
   /**
   * Implements the template method in superclass. This method stops all the
   * applications in this server.
   */
   public void stopService() {
   }
   
   /**
   * Implements the template method in superclass. This method destroys all
   * the applications in this server and clears the deployments list.
   */
   public void destroyService() {
   }
   
}
