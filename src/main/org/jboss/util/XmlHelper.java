/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.io.*;
import java.net.*;

import org.w3c.dom.*;

import org.jboss.logging.Log;

/**
 *   A utility class to cover up the rough bits of xml parsing
 *      
 *   @author <a href="mailto:chris@kimptoc.net">Chris Kimpton</a>
 *   @version $Revision: 1.3 $
 */
public class XmlHelper
{
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   /** no public constructor - use the static methods */
   private XmlHelper() {}
 
   // Public --------------------------------------------------------
   public static void write(Writer out, Document dom)
      throws java.lang.Exception
   {
      DOMWriter writer = new DOMWriter(out,false);
      writer.print(dom, true);
   }
   
   // Protected -----------------------------------------------------
}


