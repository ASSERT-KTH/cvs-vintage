/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class DefaultLog extends Log
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
	 static {
		 Log.defaultLog = new DefaultLog();
	 }
   
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

