/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.management.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 *
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.6 $
 */
public class ViewerLogging
   implements ViewerLoggingMBean, MBeanRegistration, NotificationListener
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   PrintStream out;
   DateFormat fmt = new SimpleDateFormat();

   Log log = new DefaultLog("Viewer logging");

   DefaultTableModel tableModel;

   String source;
   String sourceList;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public ViewerLogging()
   {
   }

   public ViewerLogging(String source)
   {
      this.source = source;

      sourceList = ","+source+",";
   }

   // Public --------------------------------------------------------
   public void initGui()
   {
      JFrame frame = new JFrame("Logging");
      JTable events = new JTable(new DefaultTableModel(new Object[0][0], new String[] { "Time", "Source", "Message" }));
      tableModel = (DefaultTableModel)events.getModel();
      TableColumnModel columnModel = events.getColumnModel();
      columnModel.getColumn(0).setPreferredWidth(120);
      columnModel.getColumn(1).setPreferredWidth(100);
      columnModel.getColumn(0).setMaxWidth(150);
      columnModel.getColumn(1).setMaxWidth(100);
      JScrollPane sp = new JScrollPane(events);

      frame.getContentPane().add(sp, BorderLayout.CENTER);

      if (source != null)
         frame.getContentPane().add(new JLabel("Source filter:"+source), BorderLayout.NORTH);

      frame.resize(500,500);
      frame.show();
   }


   // NotificationListener implementation ---------------------------
   public void handleNotification(Notification n,
                                  java.lang.Object handback)
   {
      try
      {
         if (sourceList != null)
            if (sourceList.indexOf(n.getUserData().toString()) == -1)
               return;

         tableModel.addRow(new Object[] { fmt.format(new Date(n.getTimeStamp())), n.getUserData(), n.getMessage() });
      } catch (Throwable e)
      {
         Logger.exception(e);
      }
   }

   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      initGui();

      log.log("Logging started");

      server.addNotificationListener(new ObjectName(server.getDefaultDomain(),"service","Log"),this,null,null);

      return new ObjectName("DefaultDomain:service=Logging,type=Viewer");
   }

   public void postRegister(java.lang.Boolean registrationDone)
   {
   }

   public void preDeregister()
      throws java.lang.Exception
   {}

   public void postDeregister() {}
}


