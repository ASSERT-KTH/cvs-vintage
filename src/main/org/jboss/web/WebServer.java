/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.web;

import java.io.*;
import java.net.*;
import java.util.*;
import org.jboss.logging.Logger;

/**
 *   A mini webserver that should be embedded in another application. It can server any file that is available from
 *   classloaders that are registered with it, including class-files.
 *
 *   Its primary purpose is to simplify dynamic class-loading in RMI. Create an instance of it, register a classloader 
 *   with your classes, start it, and you'll be able to let RMI-clients dynamically download classes from it.
 *
 *   It is configured by editing either the dynaserver.default file in dynaserver.jar (not recommended), 
 *   or by adding a file dynaserver.properties in the same location as the dynaserver.jar file (recommended).
 *   It can also be configured by calling any methods programmatically prior to startup.
 *
 *   @author $Author: oberg $
 *   @version $Revision: 1.3 $
 */
public class WebServer
	implements Runnable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   private int port = 8080;
   private ArrayList classLoaders = new ArrayList();
   
   private ServerSocket server = null;
   
   private boolean debug = false; // The server shows some debugging info if this is true
   
   private static Properties mimeTypes = new Properties(); // The MIME type mapping
	
	private ThreadPool threadPool = new ThreadPool();
   
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void setPort(int p) 
	{ 
		port = p; 
	}
	
   public int getPort() 
	{ 
		return port; 
	}
   
   public void setDebug(boolean d) 
	{ 
		debug = d; 
	}
	
   public boolean isDebug() 
	{ 
		return debug; 
	}
   
   public void addMimeType(String extension, String type) 
	{ 
		mimeTypes.put(extension,type);
	}
	
   public void start()
      throws IOException
   {
      try
      {
         server = null;
         server = new ServerSocket(getPort());
         debug("Started on port " + getPort());
         listen();
         
      } catch (IOException e)
      {
         debug("Could not start on port " + getPort());
         throw e;
      }
   }
   
   public void stop()
   {
      try
      {
         ServerSocket srv = server;
         server = null;
         srv.close();
      } catch (Exception e) {}
   }
   
   public void addClassLoader(ClassLoader cl)
   {
      classLoaders.add(cl);
   }
   
   public void removeClassLoader(ClassLoader cl)
   {
      classLoaders.remove(cl);
   }
   
   // Runnable implementation ---------------------------------------
   public void run()
   {
      Socket socket = null;
      
      // Accept a connection
      try 
      {
         socket = server.accept();
      } catch (IOException e) 
      {
         if (server == null) return; // Stopped by normal means
         
         debug("DynaServer stopped: " + e.getMessage());
         Logger.exception(e);
         debug("Restarting DynaServer");
         try
         {
            start();
            return;
         } catch (IOException ex)
         {
            debug("Restart failed");
            return;
         }
      }
      
      // Create a new thread to accept the next connection
      listen();

      try 
      {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        try 
        {
           // Get path to class file from header
           BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           
           String path = getPath(in);
           byte[] bytes;
           if (path.endsWith(".class")) // Class
           {
              String className = path.substring(0, path.length()-6).replace('/','.');
              
              // Try getting class
              URL clazzUrl = null;
              for (int i = 0; i < classLoaders.size(); i++)
              {
                 try
                 {
                    Class clazz = ((ClassLoader)classLoaders.get(i)).loadClass(className);
                    clazzUrl = clazz.getProtectionDomain().getCodeSource().getLocation();
                    if (clazzUrl.getFile().endsWith(".jar"))
                       clazzUrl = new URL("jar:"+clazzUrl+"!/"+path);
                    else
                       clazzUrl = new URL(clazzUrl, path);
                       
                    break;
                 } catch (Exception e)
                 {
                    Logger.exception(e);
                 }
              }
              
              if (clazzUrl == null)
                 throw new Exception("Class not found:"+className);
              
              // Retrieve bytecodes
              bytes = getBytes(clazzUrl);
           } else // Resource
           {
              // Try getting resource
              URL resourceUrl = null;
              for (int i = 0; i < classLoaders.size(); i++)
              {
                 try
                 {
                    resourceUrl = ((ClassLoader)classLoaders.get(i)).getResource(path);
                    
                    if (resourceUrl != null)
                       break;
                 } catch (Exception e)
                 {
                    Logger.exception(e);
                 }
              }
              
              if (resourceUrl == null)
                 throw new Exception("File not found:"+path);
              
              // Retrieve bytes
              bytes = getBytes(resourceUrl);
           }
           
           // Send bytecodes in response (assumes HTTP/1.0 or later)
           try 
           {
                 out.writeBytes("HTTP/1.0 200 OK\r\n");
                 out.writeBytes("Content-Length: " + bytes.length +
                                "\r\n");
                 out.writeBytes("Content-Type: "+getMimeType(path)+"\r\n\r\n");
                 out.write(bytes);
                 out.flush();
            } catch (IOException ie) 
            {
               return;
            }
         } catch (Exception e) 
         {
            try
            {
               // Write out error response
               out.writeBytes("HTTP/1.0 400 " + e.getMessage() + "\r\n");
               out.writeBytes("Content-Type: text/html\r\n\r\n");
               out.flush();
            } catch (IOException ex)
            {
               // Ignore
            }
         }
      } catch (IOException ex) 
      {
         // eat exception (could log error to log file, but
         // write out to stdout for now).
         debug("error writing response: " + ex.getMessage());
         ex.printStackTrace();
      
      } finally 
      {
         try 
         {
            socket.close();
         } catch (IOException e) 
         {
         }
      }
   }
    
   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected void debug(String msg)
   {
      if (isDebug())
         Logger.log(msg);
   }
   
   protected void listen()
   {
      threadPool.run(this);
   }
   
   /**
    * Returns the path to the class file obtained from
    * parsing the HTML header.
    */
   protected String getPath(BufferedReader in)
      throws IOException
   {
      String line = in.readLine();
      
      int idx = line.indexOf(" ")+1;
      return line.substring(idx+1,line.indexOf(" ",idx)); // The file minus the leading /
   }
   
   protected byte[] getBytes(URL url)
      throws Exception
   {
      InputStream in = new BufferedInputStream(url.openStream());
      debug("Retrieving "+url.toString());
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      int data;
      while ((data = in.read()) != -1)
      {
         out.write(data);
      }
      return out.toByteArray();
   }
   
   protected String getMimeType(String path)
   {
      String type = mimeTypes.getProperty(path.substring(path.lastIndexOf(".")));
      if (type == null)
         return "text/html";
      else
         return type;
   }
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}