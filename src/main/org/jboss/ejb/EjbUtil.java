/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Iterator;
import java.util.StringTokenizer;

import javax.management.MBeanServer;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.logging.Logger;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.MessageDestinationMetaData;
import org.jboss.metadata.WebMetaData;
import org.jboss.util.Strings;

/** Utility methods for resolving ejb-ref and ejb-local-ref within the
 * scope of a deployment.
 *
 * @author <a href="mailto:criege@riege.com">Christian Riege</a>
 * @author Scott.Stark@jboss.org
 *
 * @version $Revision: 1.6 $
 */
public final class EjbUtil
{
   private static final Logger log = Logger.getLogger(EjbUtil.class);

   /**
    * Resolves an &lt;ejb-link&gt; target for an &lt;ejb-ref&gt; entry and
    * returns the name of the target in the JNDI tree.
    *
    * @param di DeploymentInfo
    * @param link Content of the &lt;ejb-link&gt; entry.
    *
    * @return The JNDI Entry of the target bean; <code>null</code> if
    *         no appropriate target could be found.
    */
   public static String findEjbLink(MBeanServer server, DeploymentInfo di, String link)
   {
      return resolveLink(server, di, link, false);
   }

   /**
    * Resolves an &lt;ejb-link&gt; target for an &lt;ejb-local-ref&gt; entry
    * and returns the name of the target in the JNDI tree.
    *
    * @param di DeploymentInfo
    * @param link Content of the &lt;ejb-link&gt; entry.
    *
    * @return The JNDI Entry of the target bean; <code>null</code> if
    *         no appropriate target could be found.
    */
   public static String findLocalEjbLink(MBeanServer server, DeploymentInfo di, String link)
   {
      return resolveLink(server, di, link, true);
   }

   /**
    * Resolves a &lt;message-destination&gt; target for a &lt;message-destination-link&gt; 
    * entry and returns the name of the target in the JNDI tree.
    *
    * @param di DeploymentInfo
    * @param link Content of the &lt;message-driven-link&gt; entry.
    *
    * @return The JNDI Entry of the target; <code>null</code> if
    *         no appropriate target could be found.
    */
   public static MessageDestinationMetaData findMessageDestination(MBeanServer server, DeploymentInfo di, String link)
   {
      return resolveMessageDestination(server, di, link);
   }

   private static String resolveLink(MBeanServer server, DeploymentInfo di, String link, boolean isLocal)
   {
      if (link == null)
      {
         return null;
      }

      if (log.isTraceEnabled())
      {
         log.trace("resolveLink( {" + di + "}, {" + link + "}, {" + isLocal + "}");
      }

      if (di == null)
      {
         // We should throw an IllegalArgumentException here probably?
         return null;
      }

      if (link.indexOf('#') != -1)
      {
         // <ejb-link> is specified in the form path/file.jar#Bean
         return resolveRelativeLink(server, di, link, isLocal);
      }
      else
      {
         // <ejb-link> contains a Bean Name, scan the DeploymentInfo tree
         DeploymentInfo top = di;
         while (top.parent != null)
         {
            top = top.parent;
         }

         return resolveAbsoluteLink(top, link, isLocal);
      }
   }

   private static String resolveRelativeLink(MBeanServer server, DeploymentInfo di, String link, boolean isLocal)
   {

      String path = link.substring(0, link.indexOf('#'));
      String ejbName = link.substring(link.indexOf('#') + 1);
      String us = di.url.toString();

      // Remove the trailing slash for unpacked deployments
      if (us.charAt(us.length() - 1) == '/')
         us = us.substring(0, us.length() - 1);

      String ourPath = us.substring(0, us.lastIndexOf('/'));

      if (log.isTraceEnabled())
      {
         log.trace("Resolving relative link: " + link);
         log.trace("Looking for: '" + link + "', we're located at: '" + ourPath + "'");
      }

      for (StringTokenizer st = new StringTokenizer(path, "/"); st.hasMoreTokens();)
      {
         String s = st.nextToken();
         if (s.equals(".."))
         {
            ourPath = ourPath.substring(0, ourPath.lastIndexOf('/'));
         }
         else
         {
            ourPath += "/" + s;
         }
      }

      URL target = null;

      try
      {
         target = Strings.toURL(ourPath);
      }
      catch (MalformedURLException mue)
      {
         log.warn("Can't construct URL for: " + ourPath);
         return null;
      }

      DeploymentInfo targetInfo = null;
      try
      {
         targetInfo = (DeploymentInfo) server.invoke(MainDeployerMBean.OBJECT_NAME, "getDeployment", new Object[]
         {target}, new String[]
         {URL.class.getName()});
      }
      catch (Exception e)
      {
         log.warn("Got Exception when looking for DeploymentInfo: " + e);
         return null;
      }

      if (targetInfo == null)
      {
         log.warn("Can't locate deploymentInfo for target: " + target);
         return null;
      }

      if (log.isTraceEnabled())
      {
         log.trace("Found appropriate DeploymentInfo: " + targetInfo);
      }

      String linkTarget = null;
      if (targetInfo.metaData instanceof ApplicationMetaData)
      {
         ApplicationMetaData appMD = (ApplicationMetaData) targetInfo.metaData;
         BeanMetaData beanMD = appMD.getBeanByEjbName(ejbName);

         if (beanMD != null)
         {
            linkTarget = getJndiName(beanMD, isLocal);
         }
         else
         {
            log.warn("No Bean named '" + ejbName + "' found in '" + path + "'!");
         }
      }
      else
      {
         log.warn("DeploymentInfo " + targetInfo + " is not an EJB .jar " + "file!");
      }

      return linkTarget;
   }

   private static String resolveAbsoluteLink(DeploymentInfo di, String link, boolean isLocal)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Resolving absolute link, di: " + di);
      }

      String ejbName = null;

      // Search current DeploymentInfo
      if (di.metaData instanceof ApplicationMetaData)
      {
         ApplicationMetaData appMD = (ApplicationMetaData) di.metaData;
         BeanMetaData beanMD = appMD.getBeanByEjbName(link);
         if (beanMD != null)
         {
            ejbName = getJndiName(beanMD, isLocal);
            if (log.isTraceEnabled())
            {
               log.trace("Found Bean: " + beanMD + ", resolves to: " + ejbName);
            }

            return ejbName;
         }
         else if (log.isTraceEnabled())
         {
            log.trace("No match for ejb-link: " + link);
            Iterator iter = appMD.getEnterpriseBeans();
            while (iter.hasNext())
            {
               beanMD = (BeanMetaData) iter.next();
               ejbName = getJndiName(beanMD, isLocal);
               log.trace("... Has ejbName: " + ejbName);
            }
         }
      }

      // Search each subcontext
      Iterator it = di.subDeployments.iterator();
      while (it.hasNext() && ejbName == null)
      {
         DeploymentInfo child = (DeploymentInfo) it.next();
         ejbName = resolveAbsoluteLink(child, link, isLocal);
      }

      return ejbName;
   }

   private static String getJndiName(BeanMetaData beanMD, boolean isLocal)
   {
      String jndiName = null;
      if (isLocal)
      {
         // Validate that there is a local home associated with this bean
         String localHome = beanMD.getLocalHome();
         if (localHome != null)
            jndiName = beanMD.getLocalJndiName();
         else
         {
            log
                  .warn("LocalHome jndi name requested for: '" + beanMD.getEjbName()
                        + "' but there is no LocalHome class");
         }
      }
      else
      {
         jndiName = beanMD.getJndiName();
      }
      return jndiName;
   }

   private static MessageDestinationMetaData resolveMessageDestination(MBeanServer server, DeploymentInfo di, String link)
   {
      if (link == null)
         return null;

      if (log.isTraceEnabled())
         log.trace("resolveLink( {" + di + "}, {" + link + "})");

      if (di == null)
         // We should throw an IllegalArgumentException here probably?
         return null;

      if (link.indexOf('#') != -1)
         // link is specified in the form path/file.jar#Bean
         return resolveRelativeMessageDestination(server, di, link);
      else
      {
         // link contains a Bean Name, scan the DeploymentInfo tree
         DeploymentInfo top = di;
         while (top.parent != null)
            top = top.parent;

         return resolveAbsoluteMessageDestination(top, link);
      }
   }

   private static MessageDestinationMetaData resolveRelativeMessageDestination(MBeanServer server, DeploymentInfo di, String link)
   {
      String path = link.substring(0, link.indexOf('#'));
      String destinationName = link.substring(link.indexOf('#') + 1);
      String us = di.url.toString();

      // Remove the trailing slash for unpacked deployments
      if (us.charAt(us.length() - 1) == '/')
         us = us.substring(0, us.length() - 1);

      String ourPath = us.substring(0, us.lastIndexOf('/'));

      if (log.isTraceEnabled())
      {
         log.trace("Resolving relative message-destination-link: " + link);
         log.trace("Looking for: '" + link + "', we're located at: '" + ourPath + "'");
      }

      for (StringTokenizer st = new StringTokenizer(path, "/"); st.hasMoreTokens();)
      {
         String s = st.nextToken();
         if (s.equals(".."))
            ourPath = ourPath.substring(0, ourPath.lastIndexOf('/'));
         else
            ourPath += "/" + s;
      }

      URL target = null;
      try
      {
         target = Strings.toURL(ourPath);
      }
      catch (MalformedURLException mue)
      {
         log.warn("Can't construct URL for: " + ourPath);
         return null;
      }

      DeploymentInfo targetInfo = null;
      try
      {
         targetInfo = (DeploymentInfo) server.invoke
         (
            MainDeployerMBean.OBJECT_NAME, 
            "getDeployment", 
            new Object[] {target}, 
            new String[] {URL.class.getName()}
         );
      }
      catch (Exception e)
      {
         log.warn("Got Exception when looking for DeploymentInfo: " + e);
         return null;
      }

      if (targetInfo == null)
      {
         log.warn("Can't locate deploymentInfo for target: " + target);
         return null;
      }

      if (log.isTraceEnabled())
         log.trace("Found appropriate DeploymentInfo: " + targetInfo);

      if (targetInfo.metaData instanceof ApplicationMetaData)
      {
         ApplicationMetaData appMD = (ApplicationMetaData) targetInfo.metaData;
         return appMD.getMessageDestination(destinationName);
      }
      else if (targetInfo.metaData instanceof WebMetaData)
      {
         WebMetaData webMD = (WebMetaData) targetInfo.metaData;
         return webMD.getMessageDestination(destinationName);
      }
      else
      {
         log.warn("DeploymentInfo " + targetInfo + " is not an EJB .jar " + "file!");
         return null;
      }
   }

   private static MessageDestinationMetaData resolveAbsoluteMessageDestination(DeploymentInfo di, String link)
   {
      if (log.isTraceEnabled())
         log.trace("Resolving absolute link, di: " + di);

      // Search current DeploymentInfo
      if (di.metaData instanceof ApplicationMetaData)
      {
         ApplicationMetaData appMD = (ApplicationMetaData) di.metaData;
         MessageDestinationMetaData mdMD = appMD.getMessageDestination(link);
         if (mdMD != null)
            return mdMD;
      }
      else if (di.metaData instanceof WebMetaData)
      {
         WebMetaData webMD = (WebMetaData) di.metaData;
         return webMD.getMessageDestination(link);
      }

      // Search each subcontext
      Iterator it = di.subDeployments.iterator();
      while (it.hasNext())
      {
         DeploymentInfo child = (DeploymentInfo) it.next();
         MessageDestinationMetaData mdMD = resolveAbsoluteMessageDestination(child, link);
         if (mdMD != null)
            return mdMD;
      }

      // Not found
      return null;
   }
}