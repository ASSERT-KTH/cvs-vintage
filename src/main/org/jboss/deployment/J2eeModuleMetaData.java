/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

// $Id: J2eeModuleMetaData.java,v 1.10 2004/08/14 01:04:00 starksm Exp $

import org.jboss.metadata.MetaData;

import org.w3c.dom.Element;

/**
 * The metadata for an application/module element
 *
 * @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.10 $
 */
public class J2eeModuleMetaData
        extends MetaData
{
   // Constants -----------------------------------------------------
   public static final int EJB = 0;
   public static final int WEB = 1;
   public static final int CLIENT = 2;
   public static final int CONNECTOR = 3;
   public static final int SERVICE = 4;
   public static final int HAR = 5;
   private static final String[] tags = {"ejb", "web", "java", "connector", "service", "har"};

   // Attributes ----------------------------------------------------
   int type;

   String fileName;
   String alternativeDD;
   String webContext;

   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   public boolean isEjb()
   {
      return (type == EJB);
   }

   public boolean isWeb()
   {
      return (type == WEB);
   }

   public boolean isJava()
   {
      return (type == CLIENT);
   }

   public boolean isConnector()
   {
      return (type == CONNECTOR);
   }


   public String getFileName()
   {
      return fileName;
   }

   public String getAlternativeDD()
   {
      return alternativeDD;
   }

   public String getWebContext()
   {
      if (type == WEB)
      {
         return webContext;
      }
      else
      {
         return null;
      }
   }

   public void importXml(Element rootElement) throws DeploymentException
   {
      String rootTag = rootElement.getOwnerDocument().getDocumentElement().getTagName();
      if (rootTag.equals("application"))
         importXml(rootElement, false);
      else if (rootTag.equals("jboss-app"))
         importXml(rootElement, true);
      else
         throw new DeploymentException("Unrecognized root tag: " + rootTag);
   }

   protected void importXml(Element element, boolean jbossSpecific) throws DeploymentException
   {
      String name = element.getTagName();
      if (name.equals("module"))
      {
         boolean done = false; // only one of the tags can hit!
         for (int i = 0; done == false && i < tags.length; ++i)
         {
            Element child = getOptionalChild(element, tags[i]);
            if (child == null)
            {
               continue;
            }

            type = i;
            switch (type)
            {
               case SERVICE:
                  if (jbossSpecific == false)
                  {
                     throw new DeploymentException("Service archives must be in jboss-app.xml");
                  } // end of if ()
                  //fall through.
               case HAR:
                  if (jbossSpecific == false)
                  {
                     throw new DeploymentException("Hibernate archives must be in jboss-app.xml");
                  }
               case EJB:
               case CLIENT:
               case CONNECTOR:
                  fileName = getElementContent(child);
                  alternativeDD = getElementContent(getOptionalChild(element, "alt-dd"));
                  break;
               case WEB:
                  fileName = getElementContent(getUniqueChild(child, "web-uri"));
                  webContext = getElementContent(getOptionalChild(child, "context-root"));
                  alternativeDD = getElementContent(getOptionalChild(element, "alt-dd"));
                  break;
            }
            done = true;
         }

         // If the module content is not recognized throw an exception
         if (done == false)
         {
            StringBuffer msg = new StringBuffer("Invalid module content, must be one of: ");
            for (int i = 0; i < tags.length; i ++)
            {
               msg.append(tags[i]);
               msg.append(", ");
            }
            throw new DeploymentException(msg.toString());
         }
      }
      else
      {
         throw new DeploymentException("non-module tag in application dd: " + name);
      }
   }
}
