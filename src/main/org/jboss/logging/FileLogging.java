/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

import java.io.*;
import java.net.URL;
import java.text.*;
import java.util.Date;
import java.util.SortedSet;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.StringTokenizer;
import javax.management.*;

import org.jboss.util.ServiceMBeanSupport;

/**
 *
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @deprecated Use the log4j api instead
 *   @version $Revision: 1.14 $
 */
public class FileLogging
   implements FileLoggingMBean, NotificationListener, MBeanRegistration
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   PrintStream out, err;
   String format = "<{0,date,yyyy-MM-dd} {0,time,hh.mm}><{2}> {4}";
   MessageFormat msgFmt = new MessageFormat(format);

   boolean verbose = false;

   String filter = "Information,Debug,Warning,Error";
   String logName = "server";
   String sources;
   boolean append;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public FileLogging()
   {
   }

   public FileLogging(String filter, String format)
   {
      this(filter, format, Boolean.FALSE);
   }

   public FileLogging(String filter, String format, Boolean append)
   {
      this.filter = filter;
      setFormat(format);
      this.append = (append != null && append.booleanValue());
   }

   public FileLogging(String filter, String format, String sources, String fileName)
   {
      this(filter, format, sources, fileName, Boolean.FALSE);
   }

   public FileLogging(String filter, String format, String sources, String fileName, Boolean append)
   {
      this.filter = filter;
      setFormat(format);

      this.sources = sources;
      this.logName = fileName;
      this.append = (append != null && append.booleanValue());
   }

   // Public --------------------------------------------------------
   public void setFormat(String format)
   {
      this.format = format;
      msgFmt = new MessageFormat(format);
   }
   
   public String getFormat() { return format; }

   public void setLogName(String logName) throws FileNotFoundException
   {
      if(!logName.equals(this.logName)) 
      {
         this.logName = logName;

         if (out != null)
         {
            out = null;
            openLogFile();
         }
      }
   }
   public String getLogName() { return logName; }

   // NotificationListener implementation ---------------------------
   public void handleNotification(Notification n,
                                  java.lang.Object handback)
   {
      if (out == null) return; // Not started yet
      
      if (sources == null || sources.length() == 0 || sources.indexOf(n.getUserData().toString()) != -1)
      {
         if (filter.indexOf(n.getType()) != -1)
         {
            Object[] args = new Object[] { new Date(n.getTimeStamp()), new Long(n.getSequenceNumber()), n.getUserData(), n.getType(), n.getMessage() };
            out.println(msgFmt.format(args));
         }
      }
   }

   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      openLogFile();
      server.addNotificationListener(new ObjectName(server.getDefaultDomain(),"service","Log"),this,null,null);
      if (name != null) {
         return name; 
      } else {
         if (sources == null || sources.equals("")) {
            return new ObjectName(OBJECT_NAME);
         } else { 
	    return new ObjectName(OBJECT_NAME + ",sources=" + sources);
	 }
      } 
   }
   
   public void postRegister(java.lang.Boolean registrationDone) 
   {
   }
   
   public void preDeregister()
      throws java.lang.Exception 
   {
   }
   
   public void postDeregister() 
   {
   }
   
   // Private -------------------------------------------------------
   private void openLogFile() 
      throws FileNotFoundException 
   {
      URL properties = getClass().getResource("/log.properties");
      if(properties == null)
         throw new FileNotFoundException("Unable to identify logging directory!");
         
      File parent = new File(properties.getFile()).getParentFile();
      File logFile = new File(parent, logName+".log");
      try 
      {
         out = new PrintStream(new FileOutputStream(logFile.getCanonicalPath(), append));
      } catch (IOException e) 
      {
         throw new FileNotFoundException(e.getMessage());
      }
   }
}
