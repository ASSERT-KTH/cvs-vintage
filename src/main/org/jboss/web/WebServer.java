/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.web;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.jboss.logging.log4j.JBossCategory;

/**
 *   A mini webserver that should be embedded in another application. It can server any file that is available from
 *   classloaders that are registered with it, including class-files.
 *
 *   Its primary purpose is to simplify dynamic class-loading in RMI. Create an instance of it, register a classloader 
 *   with your classes, start it, and you'll be able to let RMI-clients dynamically download classes from it.
 *
 *   It is configured by calling any methods programmatically prior to startup.
 *
 *   @see WebClassLoader
 *
 *   @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 *   @author <a href="mailto:Scott.Stark@org.jboss">Scott Stark</a>.
 *   @version $Revision: 1.10 $
 *
 *   Revisions:
 *   
 *   20010619 scott.stark: Use log4j JBossCategory to enable trace level msgs
 *   20010618 scott.stark: Fixed extraction of mime-type from file extension in getMimeType
 *   20010627 scott.stark: Restore ability to download from the server classpath if no loader key is found
      amd downloadServerClasses is true
 */
public class WebServer
	implements Runnable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private static JBossCategory category = (JBossCategory)JBossCategory.getInstance(WebServer.class);
   /** The port the web server listens on */
   private int port = 8080;
   /** The map of class loaders registered with the web server */
   private HashMap loaderMap = new HashMap();
   /** The web server http listening socket */
   private ServerSocket server = null;
   /** A flag indicating if the server should attempt to download classes from
    thread context class loader when a request arrives that does not have a
    class loader key prefix.
    */
   private boolean downloadServerClasses = true;
   /** The class wide mapping of type suffixes(class, txt) to their mime
   type string used as the Content-Type header for the vended classes/resources */
   private static Properties mimeTypes = new Properties();
   /** The thread pool used to manage listening threads */
   private ThreadPool threadPool = new ThreadPool();

   // Public --------------------------------------------------------
    /** Set the http listening port
     */
    public void setPort(int p) 
    {
        port = p; 
    }
    /** Get the http listening port
     @return the http listening port
     */
    public int getPort() 
    {
        return port; 
    }

   public boolean getDownloadServerClasses()
   {
      return downloadServerClasses;
   }
   public void setDownloadServerClasses(boolean flag)
   {
      downloadServerClasses = flag;
   }

    /** Augment the type suffix to mime type mappings
     @param extension, the type extension without a
        period(class, txt)
     @param type, the mime type string
     */
    public void addMimeType(String extension, String type) 
    { 
        mimeTypes.put(extension,type);
    }

    /** Start the web server on port and begin listening for requests.
     */
    public void start() throws IOException
    {
        try
        {
            server = null;
            server = new ServerSocket(getPort());
            debug("Started on port " + getPort());
            listen();
        }
        catch (IOException e)
        {
            debug("Could not start on port " + getPort());
            throw e;
        }
    }

    /** Close the web server listening socket
     */
    public void stop()
    {
        try
        {
            ServerSocket srv = server;
            server = null;
            srv.close();
        }
        catch (Exception e)
        {
        }
    }

    /** Add a class loader to the web server map and return the URL that
     should be used as the annotated codebase for classes that are to be
     available via RMI dynamic classloading. The codebase URL is formed by
     taking the java.rmi.server.codebase system property and adding a subpath
     unique for the class loader instance.

    @see #getClassLoaderKey(ClassLoader)
    @param cl, the ClassLoader instance to begin serving download requests for
    @return the annotated codebase to use if java.rmi.server.codebase is set,
        null otherwise.
    */
    public URL addClassLoader(ClassLoader cl)
    {
        String key = getClassLoaderKey(cl);
        loaderMap.put(key, cl);
        URL loaderURL = null;
        String codebase = System.getProperty("java.rmi.server.codebase");
        if( codebase != null )
        {
            if( codebase.endsWith("/") == false )
                codebase += '/';
            codebase += key;
            try
            {
                loaderURL = new URL(codebase);
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
        }
        trace("Added ClassLoader: "+cl+" URL: "+loaderURL);
        return loaderURL;
    }

    /** Remove a class loader previously added via addClassLoader
    @param cl, the ClassLoader previously added via addClassLoader
     */
    public void removeClassLoader(ClassLoader cl)
    {
        String key = getClassLoaderKey(cl);
        loaderMap.remove(key);
    }

    // Runnable implementation ---------------------------------------
    /** Listen threads entry point. Here we accept a client connection
        and located requested classes/resources using the class loader
        specified in the http request.
     */
    public void run()
    {
        // Return if the server has been stopped
        if (server == null)
            return;

        // Accept a connection
        Socket socket = null;
        try 
        {
            socket = server.accept();
        }
        catch (IOException e) 
        {
            // If the server is not null meaning we were not stopped report the err
            if( server != null )
               debug("DynaServer error: " + e.getMessage());
            return;
        }

        // Create a new thread to accept the next connection
        listen();

        try 
        {
            // Get the request socket output stream
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            try 
            {
                // Get the requested item from the HTTP header
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String rawPath = getPath(in);
                // Parse the path into the class loader key and file path
                int separator = rawPath.indexOf('/');
                String filePath = rawPath.substring(separator+1);
                String loaderKey = rawPath.substring(0, separator+1);
                trace("loaderKey = "+loaderKey);
                trace("filePath = "+filePath);
                ClassLoader loader = (ClassLoader) loaderMap.get(loaderKey);
                /* If we did not find a class loader check to see if the raw path
                 begins with className + '@' + cl.hashCode() + '/' by looking for
                 an '@' char. If it does not and downloadServerClasses is true use
                 the thread context class loader and set filePath to the rawPath
                */
                if( loader == null && rawPath.indexOf('@') < 0 )
                {
                   filePath = rawPath;
                   trace("No loader, reset filePath = "+filePath);
                   loader = Thread.currentThread().getContextClassLoader();
                }
                trace("loader = "+loader);
                byte[] bytes;
                if( filePath.endsWith(".class") )
                {
                    // A request for a class file
                    String className = filePath.substring(0, filePath.length()-6).replace('/','.');
                    trace("loading className = "+className);
                    Class clazz = loader.loadClass(className);
                    URL clazzUrl = clazz.getProtectionDomain().getCodeSource().getLocation();
                    trace("clazzUrl = "+clazzUrl);
                    if (clazzUrl.getFile().endsWith(".jar"))
                       clazzUrl = new URL("jar:"+clazzUrl+"!/"+filePath);
                    else
                       clazzUrl = new URL(clazzUrl, filePath);
                    if (clazzUrl == null)
                     throw new Exception("Class not found:"+className);
              
                    // Retrieve bytecodes
                    bytes = getBytes(clazzUrl);
                }
                else // Resource
                {
                    // Try getting resource
                    trace("loading resource = "+filePath);
                    URL resourceUrl = loader.getResource(filePath);             
                    if (resourceUrl == null)
                        throw new FileNotFoundException("Resource not found:"+filePath);

                    // Retrieve bytes
                    bytes = getBytes(resourceUrl);
                }

                // Send bytecodes/resource data in response (assumes HTTP/1.0 or later)
                try 
                {
                    // The HTTP 1.0 header
                    out.writeBytes("HTTP/1.0 200 OK\r\n");
                    out.writeBytes("Content-Length: " + bytes.length + "\r\n");
                    out.writeBytes("Content-Type: "+getMimeType(filePath));
                    out.writeBytes("\r\n\r\n");
                    // The response body
                    out.write(bytes);
                    out.flush();
                }
                catch (IOException ie) 
                {
                    return;
                }
            }
            catch(Throwable e) 
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
        }
        catch (IOException ex) 
        {
            // eat exception (could log error to log file, but
            // write out to stdout for now).
            debug("error writing response: " + ex.getMessage());
            ex.printStackTrace();
        }
        finally 
        {
            // Close the client request socket
            try 
            {
                socket.close();
            } catch (IOException e) 
            {
            }
        }
    }

    // Protected -----------------------------------------------------
    /** Create the string key used as the key into the loaderMap.
     @return The class loader instance key.
     */
    protected String getClassLoaderKey(ClassLoader cl)
    {
        String className = cl.getClass().getName();
        int dot = className.lastIndexOf('.');
        if( dot >= 0 )
            className = className.substring(dot+1);
        String key =  className + '@' + cl.hashCode() + '/';
        return key;
    }

    protected void trace(String msg)
    {
        category.trace(msg);
    }
    protected void debug(String msg)
    {
        category.debug(msg);
    }

    protected void listen()
    {
        threadPool.run(this);
    }

    /**
    @return the path portion of the HTTP request header.
    */
    protected String getPath(BufferedReader in) throws IOException
    {
        String line = in.readLine();
        trace("raw request="+line);
        // Find the request path by parsing the 'REQUEST_TYPE filePath HTTP_VERSION' string
        int start = line.indexOf(' ')+1;
        int end = line.indexOf(' ', start+1);
        // The file minus the leading '/'
        String filePath = line.substring(start+1, end);
        return filePath;
    }

    /** Read the local class/resource contents into a byte array.
     */
    protected byte[] getBytes(URL url) throws IOException
    {
        InputStream in = new BufferedInputStream(url.openStream());
        debug("Retrieving "+url.toString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];
        int bytes;
        while ((bytes = in.read(tmp)) != -1)
        {
            out.write(tmp, 0, bytes);
        }
        return out.toByteArray();
    }

    /** Lookup the mime type for the suffix of the path argument.
     @return the mime-type string for path.
     */
    protected String getMimeType(String path)
    {
        int dot = path.lastIndexOf(".");
        String type = "text/html";
        if( dot >= 0 )
        {
            // The suffix is the type extension without the '.'
            String suffix = path.substring(dot+1);
            String mimeType = mimeTypes.getProperty(suffix);
            if( mimeType != null )
                type = mimeType;
        }
        return type;
    }

}
