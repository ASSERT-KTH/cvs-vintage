/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import javax.management.ObjectName;
import java.util.List;
import java.util.LinkedList;

/**
 * ServiceContext holds information for the Service
 *
 * @see Service
 * @see ServiceMBeanSupport
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">marc fleury</a>
 * @version $Revision: 1.2 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011219 marc fleury:</b>
 * <ul>
 * <li> initial check in.
 * </ul>
 */

public class ServiceContext
{
   
   
   public static int INSTALLED = 0;
   public static int CONFIGURED = 1;
   public static int CREATED = 2;
   public static int RUNNING = 3;
   public static int FAILED = 4;
   public static int STOPPED = 5;
   public static int DESTROYED = 6;
   public static int NOTYETINSTALLED = 7;
   
   /** The name of the service **/
   public ObjectName objectName;
   
   /** State of the service **/
   public int state = NOTYETINSTALLED;
   
   /** dependent beans **/
   public List iDependOn = new LinkedList();
   
   /** beans that depend on me **/
   public List dependsOnMe = new LinkedList();
   
   /** the fancy proxy to my service calls **/
   public Service proxy;
   
}
