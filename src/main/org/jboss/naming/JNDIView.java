/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;
import java.util.Iterator;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.jboss.ejb.Application;
import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerFactoryMBean;
import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/** A simple utlity mbean that allows one to recursively list the default
 JBoss InitialContext.
 
 Deploy by adding:
 <mbean code="org.jboss.naming.JNDIView" name="DefaultDomain:service=JNDIView" />
 to the jboss.jcml file.
 
 @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 @author Vladimir Blagojevic <vladimir@xisnext.2y.net>
 @version $Revision: 1.6 $
 */
public class JNDIView extends ServiceMBeanSupport implements JNDIViewMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private MBeanServer server;
   private String listType = "text/html";
   private boolean isHTML = true;
   private boolean isXML = false;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public JNDIView()
   {
   }

   // Public --------------------------------------------------------

    /** Get the mime-type for the value returned by the list() method.
     */
    public String getListType()
    {
       return this.listType;
    }
    /** Set the mime-type for the value returned by the list() method.
     @param mimeType: text/plain, text/html, text/xml are the currently
      supported types.
     */
    public void setListType(String mimeType)
    {
       this.listType = mimeType;
    }

   /** List deployed application java:comp namespaces, the java:
    namespace as well as the global InitialContext JNDI namespace.
    @param verbose, if true, list the class of each object in addition to its name
    @param maxdepth, the maxdepth to which an given context should be listed.
    */
   public String list(boolean verbose, int maxdepth)
   {
      return list(verbose, maxdepth, "text/html");
   }

   /** List the JBoss JNDI namespace.
    @param verbose, flag indicating if the type of object should be shown
    @param maxdepth, the maxdepth to which an given context should be listed.
    @param mimeType: text/plain, text/html, text/xml are the currently
     supported types.
    */
   public String list(boolean verbose, int maxdepth, String mimeType)
   {
      isHTML = mimeType.indexOf("html") > 0;
      isXML = mimeType.indexOf("xml") > 0;
      StringBuffer buffer = new StringBuffer();
      Iterator applications = null;
      Context context = null;
      ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();

      startBuffer(buffer);
     /* Get all deployed applications so that we can list their
        java: namespaces which are ClassLoader local
      */
      try
      {
         applications = (Iterator) server.invoke(
         new ObjectName(ContainerFactoryMBean.OBJECT_NAME),
         "getDeployedApplications",
         new Object[] { }, new String[] { });
      }
      catch(Exception e)
      {
         log.exception(e);
         formatException("Failed to getDeployedApplications", buffer, e);
         endBuffer(buffer);
         return buffer.toString();
      }
      
      // List each application JNDI namespace
      while(applications.hasNext())
      {
         Application app = (Application) applications.next();
         Iterator iter = app.getContainers().iterator();
         startApplication(app, buffer);
         while(iter.hasNext())
         {
            Container con = (Container)iter.next();
            /* Set the thread class loader to that of the container as
             the class loader is used by the java: context object
             factory to partition the container namespaces.
            */
            Thread.currentThread().setContextClassLoader(con.getClassLoader());
            startContainer(con, buffer);
            
            try
            {
               context = new InitialContext();
               context = (Context) context.lookup("java:comp");
            }
            catch(NamingException e)
            {
               formatException("Failed on lookup, "+e.toString(true), buffer, e);
               endContainer(con, buffer);
               continue;
            }
            addContext(context, null, buffer, maxdepth);
            endContainer(con, buffer);
         }
         endApplication(app, buffer);
      }

      // List the java: namespace
      Thread.currentThread().setContextClassLoader(currentLoader);
      try
      {
         context = new InitialContext();
         context = (Context) context.lookup("java:");
         addContext(context, "java: Namespace", buffer, maxdepth);
      }
      catch(NamingException e)
      {
         log.exception(e);
         formatException("Failed to get InitialContext, "+e.toString(true), buffer, e);
      }
      
      // List the global JNDI namespace
      try
      {
         context = new InitialContext();
         addContext(context, "Global JNDI Namespace", buffer, maxdepth);
      }
      catch(NamingException e)
      {
         log.exception(e);
         formatException("Failed to get InitialContext, "+e.toString(true), buffer, e);
      }
      endBuffer(buffer);
      return buffer.toString();
   }

   /**
    * List deployed application java:comp namespaces, the java:
    * namespace as well as the global InitialContext JNDI namespace in a
    * XML Format.
    *
    **/
   public String listXML()
   {
      return list(true, Integer.MAX_VALUE, "text/xml");
   }

   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      this.server = server;
      return new ObjectName(OBJECT_NAME);
   }

   public String getName()
   {
      return "JNDIView";
   }
   
   private void list(Context ctx, String indent, StringBuffer buffer,
      boolean verbose, int depth, int maxdepth)
   {
      if( depth == maxdepth )
         return;

      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try
      {
         NamingEnumeration ne = ctx.list("");
         while( ne.hasMore() )
         {
            NameClassPair pair = (NameClassPair) ne.next();
            boolean recursive = false;
            boolean isLinkRef = false;
            try
            {
               Class c = loader.loadClass(pair.getClassName());
               if( Context.class.isAssignableFrom(c) )
                  recursive = true;
               if( LinkRef.class.isAssignableFrom(c) )
                  isLinkRef = true;
            }
            catch(ClassNotFoundException cnfe)
            {
            }

            String name = pair.getName();
            String className = null;
            if( verbose )
               className = pair.getClassName();
            if( isLinkRef )
            {
               // Get the
               try
               {
                  LinkRef link = (LinkRef) ctx.lookupLink(name);
                  addLinkRef(link, name, className, buffer);
               }
               catch(Throwable e)
               {
                  e.printStackTrace();
               }
            }
            if( recursive )
            {
               try
               {
                  Object value = ctx.lookup(name);
                  if( value instanceof Context )
                  {
                     Context subctx = (Context) value;
                     list(subctx, indent + " |  ", buffer, verbose, depth+1, maxdepth);
                  }
                  else
                  {
                     buffer.append(indent + " |   NonContext: "+value);
                     buffer.append('\n');
                  }
               }
               catch(Throwable t)
               {
                  buffer.append("Failed to lookup: "+name+", errmsg="+t.getMessage());
                  buffer.append('\n');
               }
            }
         }
         ne.close();
      }
      catch(NamingException ne)
      {
         String msg = "error while listing context "+ctx.toString() + ": " + ne.toString(true);
         formatException(msg, buffer, ne);
      }
   }
   
   private void listXML( Context ctx, StringBuffer buffer )
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try
      {
         NamingEnumeration ne = ctx.list("");
         while( ne.hasMore() )
         {
            NameClassPair pair = (NameClassPair) ne.next();
            boolean recursive = false;
            boolean isLinkRef = false;
            try
            {
               Class c = loader.loadClass(pair.getClassName());
               if( Context.class.isAssignableFrom(c) )
                  recursive = true;
               if( LinkRef.class.isAssignableFrom(c) )
                  isLinkRef = true;
            }
            catch(ClassNotFoundException cnfe)
            {
            }
            
            String name = pair.getName();
            if( isLinkRef )
            {
               // Get the
               try
               {
                  LinkRef link = (LinkRef) ctx.lookupLink(name);
                  buffer.append( "<link-ref>" );
                  buffer.append( '\n' );
                  buffer.append( "<name>" + pair.getName() + "</name>" );
                  buffer.append( '\n' );
                  buffer.append( "<link>" + link.getLinkName() + "</link>" );
                  buffer.append( '\n' );
                  buffer.append( "<attribute name=\"class\">" + pair.getClassName() + "</attribute>" );
                  buffer.append( '\n' );
                  buffer.append( "</link-ref>" );
                  buffer.append( '\n' );
               }
               catch(Throwable e)
               {
                  e.printStackTrace();
                  buffer.append( "<link-ref>" );
                  buffer.append( '\n' );
                  buffer.append( "<name>Invalid</name>" );
                  buffer.append( '\n' );
                  buffer.append( "</link-ref>" );
                  buffer.append( '\n' );
               }
            }
            else
            {
               if( recursive )
               {
                  try
                  {
                     Object value = ctx.lookup(name);
                     if( value instanceof Context )
                     {
                        Context subctx = (Context) value;
                        buffer.append( "<context>" );
                        buffer.append( '\n' );
                        buffer.append( "<name>" + pair.getName() + "</name>" );
                        buffer.append( '\n' );
                        buffer.append( "<attribute name=\"class\">" + pair.getClassName() + "</attribute>" );
                        buffer.append( '\n' );
                        listXML( subctx, buffer );
                        buffer.append( "</context>" );
                        buffer.append( '\n' );
                     }
                     else
                     {
                        buffer.append( "<non-context>" );
                        buffer.append( '\n' );
                        buffer.append( "<name>" + pair.getName() + "</name>" );
                        buffer.append( '\n' );
                        buffer.append( "<attribute name=\"value\">" + value + "</attribute>" );
                        buffer.append( '\n' );
                        buffer.append( "</non-context>" );
                        buffer.append( '\n' );
                     }
                  }
                  catch(Throwable t)
                  {
                     buffer.append( "<error>" );
                     buffer.append( '\n' );
                     buffer.append( "<message>" + "Failed to lookup: "+name+", errmsg="+t.getMessage() + "</message>" );
                     buffer.append( '\n' );
                     buffer.append( "</error>" );
                     buffer.append( '\n' );
                  }
               }
               else
               {
                  buffer.append( "<leaf>" );
                  buffer.append( '\n' );
                  buffer.append( "<name>" + pair.getName() + "</name>" );
                  buffer.append( '\n' );
                  buffer.append( "<attribute name=\"class\">" + pair.getClassName() + "</attribute>" );
                  buffer.append( '\n' );
                  buffer.append( "</leaf>" );
                  buffer.append( '\n' );
               }
            }
         }
         ne.close();
      }
      catch(NamingException ne)
      {
         buffer.append( "<error>" );
         buffer.append( '\n' );
         buffer.append( "<message>" + "error while listing context "+ctx.toString() + ": " + ne.toString(true) + "</message>" );
         buffer.append( '\n' );
         buffer.append( "</error>" );
         buffer.append( '\n' );
      }
   }

   private void startBuffer(StringBuffer buffer)
   {
      if( isHTML )
         buffer.insert(0, "<pre>");
      else if( isXML )
         buffer.insert(0, "<jndi>");
   }
   private void endBuffer(StringBuffer buffer)
   {
      if( isHTML )
         buffer.append("</pre>");
      else if( isXML )
         buffer.append("</jndi>");
   }
   private void startApplication(Application app, StringBuffer buffer)
   {
      if( isHTML )
      {
         buffer.append("<h1>Application: " + app.getName() + "</h1>\n");
      }
      else if( isXML )
      {
         buffer.append( "<application>" );
         buffer.append( '\n' );
         buffer.append( "<file>" + app.getName() + "</file>" );
         buffer.append( '\n' );
      }
      else
      {
         buffer.append("Application: " + app.getName() + "\n");
      }
   }
   private void endApplication(Application app, StringBuffer buffer)
   {
      if( isHTML )
      {
      }
      else if( isXML )
      {
         buffer.append( "</application>\n" );
      }
   }

   private void startContainer(Container con, StringBuffer buffer)
   {
      String bean = con.getBeanMetaData().getEjbName();
      if( isHTML )
      {
         buffer.append("<h2>java:comp namespace of the " + bean + " bean:</h2>\n");
      }
      else if( isXML )
      {
         buffer.append( "<context>" );
         buffer.append( '\n' );
         buffer.append( "<name>java:comp</name>" );
         buffer.append( '\n' );
         buffer.append( "<attribute name=\"bean\">" + bean + "</attribute>" );
         buffer.append( '\n' );
      }
      else
      {
         buffer.append("java:comp namespace of the " + bean + " bean:\n");
      }
   }
   private void endContainer(Container con, StringBuffer buffer)
   {
      if( isXML )
      {
         buffer.append( "</context>" );
         buffer.append( '\n' );
      }
   }

   private void addContext(Context context, String name, StringBuffer buffer, int maxdepth)
   {
      if( isHTML )
      {
         buffer.append("<pre>\n");
         if( name != null )
            buffer.append("<h1>"+name+"</h1>\n");
         list(context, " ", buffer, true, 0, maxdepth);
         buffer.append("</pre>\n");
      }
      else if( isXML )
      {
         buffer.append( "<context>\n" );
         if( name != null )
            buffer.append( "<name>Global</name>\n" );
         list(context, " ", buffer, true, 0, maxdepth);
         buffer.append( "</context>\n" );
      }
   }
   private void addLinkRef(LinkRef link, String name, String className, StringBuffer buffer)
   {
      String linkName = null;
      try
      {
         linkName = link.getLinkName();
      }
      catch(NamingException e)
      {
         linkName = "(invalid)";
      }
      if( isHTML )
      {
         String indent = "";
         buffer.append(indent +  " +- " + name);
         buffer.append("[link -> ");
         buffer.append(linkName);
         buffer.append(']');
         if( className != null )
            buffer.append(" (class: "+className+")");
         buffer.append('\n');
      }
   }

   private void formatException(String msg, StringBuffer buffer, Throwable t)
   {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      String trace = sw.toString();
      if( isHTML )
      {
         buffer.append(msg);
         buffer.append("<pre>\n");
         buffer.append(trace);
         buffer.append("</pre>\n");
      }
      else if( isXML )
      {
         buffer.append( "<error>" );
         buffer.append( '\n' );
         buffer.append( "<message>" + msg + "</message>" );
         buffer.append( "<trace>" + trace + "</trace>" );
         buffer.append( '\n' );
         buffer.append( "</error>" );
         buffer.append( '\n' );
      }
   }

}
