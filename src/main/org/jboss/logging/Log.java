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
 *   @version $Revision: 1.8 $
 */
public abstract class Log
{
   // Constants -----------------------------------------------------
   private static final String m_sInformation = "Information";
   private static final String m_sDebug = "Debug";
   private static final String m_sWarning = "Warning";
   private static final String m_sError = "Error";

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
			if( s == null ) {
				if( defaultLog == null ) {
					defaultLog = createLog( "Default" );
				}
				return defaultLog;
			}
			else {
				return (Log)s.peek();
			}
   }

	 public static Log createLog( Object pSource ) {
		 Log lReturn;
		try {
            final String logClass = System.getProperty("JBOSS_LOG_CLASS", "org.jboss.logging.DefaultLog");

			Class lLog = Thread.currentThread().getContextClassLoader().loadClass( logClass );
//AS			Class lLog = Class.forName( "org.jboss.logging.DefaultLog" );
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
      log(m_sInformation, message);
   }

   public synchronized void error(String message)
   {
      log(m_sError, message);
   }

   public synchronized void warning(String message)
   {
      log(m_sWarning, message);
   }

   public synchronized void debug(String message)
   {
      log(m_sDebug, message);
   }

   public synchronized void log(Throwable exception)
   {
      logException(m_sInformation, exception);
   }

   public synchronized void error(Throwable exception)
   {
      logException(m_sError, exception);
   }

   public synchronized void exception(Throwable exception)
   {
      error(exception);
   }

   public synchronized void warning(Throwable exception)
   {
      logException(m_sWarning, exception);
   }

   public synchronized void debug(Throwable exception)
   {
      logException(m_sDebug, exception);
   }

   protected synchronized void logException(final String categoryString, Throwable exception)
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
            log(categoryString, error);
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

