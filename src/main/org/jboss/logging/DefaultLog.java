/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import javax.management.*;

/**
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.5 $
 */
public class DefaultLog extends Log
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public DefaultLog()
   {
		 super();
   }
   
   public DefaultLog(Object source)
   {
		 super( source );
   }
   
   // Public --------------------------------------------------------
   public synchronized void log(String type, String message)
   {
      Logger.getLogger().fireNotification(type, source, message);
   }
   
   // Private -------------------------------------------------------
}

