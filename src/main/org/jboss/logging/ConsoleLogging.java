/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

import java.io.*;
import java.text.*;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.SortedSet;
import java.util.Comparator;
import java.util.TreeSet;
import javax.management.*;

import org.jboss.util.ServiceMBeanSupport;

/**
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.8 $
 */
public class ConsoleLogging
   implements ConsoleLoggingMBean, NotificationListener, MBeanRegistration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   PrintStream out, err;
   String format = "<{0,date,yyyy-MM-dd} {0,time,hh.mm}><{2}> {4}";
   MessageFormat msgFmt = new MessageFormat(format);
   
   boolean verbose = false;
   
   Log log = new DefaultLog("Console logging");
   
   String filter = "Information,Debug,Warning,Error";
   
   ObjectName name;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public ConsoleLogging()
   {
   }
   
   public ConsoleLogging(String filter, String format)
   {
      this.filter = filter;
      setFormat(format);
   }
   
   // Public --------------------------------------------------------
   public void setFormat(String format) 
   { 
      this.format = format; 
      msgFmt = new MessageFormat(format);
   }
   public String getFormat() { return format; }
   
   // NotificationListener implementation ---------------------------
   public synchronized void handleNotification(Notification n,
                                  java.lang.Object handback)
   {
            Object[] args = new Object[] { new Date(n.getTimeStamp()), new Long(n.getSequenceNumber()), n.getUserData(), n.getType(), n.getMessage() };
      if (n.getType().equals("Error") || n.getType().equals("Warning"))
         err.println(msgFmt.format(args));
      else         
         out.println(msgFmt.format(args));
   }
   
   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      NotificationFilterSupport f = new NotificationFilterSupport();
      StringTokenizer types = new StringTokenizer(filter, ",");
      while (types.hasMoreTokens())
         f.enableType(types.nextToken());
   
      server.addNotificationListener(new ObjectName(server.getDefaultDomain(),"service","Log"),this,f,null);
   
      return name == null ? new ObjectName(OBJECT_NAME) : name;
   }
   
   public void postRegister(java.lang.Boolean registrationDone) 
   {
      out = System.out;
      err = System.err;
   
      LogStream outLog = new LogStream("Information");
      LogStream errLog = new LogStream("Error");
      System.setOut(outLog);
      System.setErr(errLog);
   }
   
   public void preDeregister()
      throws java.lang.Exception 
   {
   }
   
   public void postDeregister() 
   {
   }
}

