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
 *   @version $Revision: 1.6 $
 */
public class LogStream
   extends PrintStream
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String type;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public LogStream(String type)
   {
      super(System.out);
      
      this.type = type;
   }
   
   // Public --------------------------------------------------------
   public void println(String msg)
   {
      Logger.log(type, msg);
   }
   
   public void println(Object msg)
   {
      println(msg == null ? "null" : msg.toString());
   }

   public void println(char[] msg) {
      println(new String(msg));
   }
}

