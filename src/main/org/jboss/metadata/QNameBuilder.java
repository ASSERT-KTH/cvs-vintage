/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: QNameBuilder.java,v 1.3 2004/05/07 14:58:49 tdiesler Exp $

import org.jboss.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

/**
 * A QName builder that discovers the namespaceURI for a given prefix by walking
 * up the document tree.
 *
 * The combined name is of the form [prefix:]localPart
 *
 * [todo] This class should live with the XML stuff in module common,
 * but then common would have to depend on j2ee
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.3 $
 */
public final class QNameBuilder
{
   private static Logger log = Logger.getLogger(QNameBuilder.class);

   /**
    * Build a QName from a combined name
    * @param element The current element
    * @param combinedName A name of form prefix:localPart
    * @return A QName, or null
    */
   public static QName buildQName(Element element, String combinedName)
   {
      if (combinedName == null)
         return null;

      int colonIndex = combinedName.indexOf(":");
      if (colonIndex < 0)
         return new QName(combinedName);

      String prefix = combinedName.substring(0, colonIndex);
      String localPart = combinedName.substring(colonIndex + 1);

      Node currNode = element;
      String namespaceURI = getNamespaceURI (currNode, prefix);
      while (namespaceURI == null && currNode != null)
      {
         Node parentNode = currNode.getParentNode();
         if (parentNode != null && parentNode != currNode)
            namespaceURI = getNamespaceURI (parentNode, prefix);

         if (parentNode == currNode)
            break;

         currNode = parentNode;
      }

      if (namespaceURI != null)
         return new QName(namespaceURI, localPart);

      log.warn ("Cannot find namespaceURI for name: " + combinedName);
      return new QName(localPart);
   }

   /**
    * Get the namespaceURI from a given prefix from the current node.
    */
   private static String getNamespaceURI(Node node, String prefix)
   {
      String namespaceURI = null;
      NamedNodeMap attrs = node.getAttributes();
      if (attrs != null)
      {
         for (int i=0; namespaceURI == null && i < attrs.getLength(); i++)
         {
            Node attr = attrs.item(i);
            if (prefix.equals(attr.getLocalName()))
               namespaceURI = attr.getNodeValue();
         }
      }
      return namespaceURI;
   }
}
