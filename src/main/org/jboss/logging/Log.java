/*
 * JBoss, the OpenSource EJB server
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
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.6 $
 */
public abstract class Log
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   long count = 0;
   
   Date now = new Date();
   
   Object source = null;
   
   // Static --------------------------------------------------------
//   static ThreadLocal currentLog = new ThreadLocal();
   
   static ThreadLocal currentLog = new InheritableThreadLocal()
   {
   		// Child threads should get same stack, but only a copy of it
   		public Object childValue(Object obj)
   		{
			if (obj != null)
				return ((Stack)obj).clone();
			else	
				return null;
   		}
   };
	 
	 protected static Log defaultLog;
   
   public static void setLog(Log log)
   {
      Stack s;
      if ((s = (Stack)currentLog.get()) == null)
      {
         s = new Stack();
         s.push(log);
         currentLog.set(s);
      } else
      {
         s.push(log);
      }
   }
   
   public static void unsetLog()
   {
      if (currentLog.get() != null)
      {
         Stack s = (Stack)currentLog.get();
         s.pop();
         if (s.size() == 0)
            currentLog.set(null);
      }
   }
   
   public static Log getLog()
   {
      Stack s = (Stack)currentLog.get();
      return s == null ? defaultLog : (Log)s.peek();
   }
	 
	 public static Log createLog( Object pSource ) {
		 Log lReturn;
		try {
			Class lLog = Class.forName( "org.jboss.logging.DefaultLog" );
			lReturn = (Log) lLog.getConstructor( new Class[] { Object.class } ).newInstance(
			new Object[] { pSource }
			);
		}
		catch( Exception e ) {
			lReturn = new Log.NoLog( pSource );
		}
		return lReturn;
	 }
   
   // Constructors --------------------------------------------------
   public Log()
   {
      this("Default");
   }
   
   public Log(Object source)
   {
      this.source = source;
   }
   
   // Public --------------------------------------------------------
   public abstract void log(String type, String message);
   
   public synchronized void log(String message)
   {
      log("Information", message);
   }
   
   public synchronized void error(String message)
   {
      log("Error", message);
   }
   
   public synchronized void warning(String message)
   {
      log("Warning", message);
   }   
   
   public synchronized void debug(String message)
   {
      log("Debug", message);
   }   
   
   public synchronized void exception(Throwable exception)
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(baos);
      exception.printStackTrace(out);
      out.close();
      
      DataInputStream din = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
      
      String error;
      try
      {
         while((error = din.readLine()) != null)
            log("Error", error);
      } catch (Exception e) {}
   }
   
   public synchronized void debug(Throwable exception)
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(baos);
      exception.printStackTrace(out);
      out.close();
      
      DataInputStream din = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
      
      String error;
      try
      {
         while((error = din.readLine()) != null)
            log("Debug", error);
      } catch (Exception e) {}
   }
   
   // Private -------------------------------------------------------
   private long nextCount()
   {
      return count++;
   }
	 
	 public static class NoLog extends Log {
		 public NoLog() {
			 super();
		 }
		 public NoLog( Object pSource ) {
			 super( pSource );
		 }
		 public Log getDefault() {
			 return new NoLog();
		 }
		 public synchronized void log( String pType, String pMessage ) {
		 }
	 }
}

