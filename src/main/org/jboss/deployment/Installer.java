/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.Enumeration;


import javax.management.MBeanServer;
import javax.management.MBeanException;
import javax.management.JMException;
import javax.management.ObjectName;

import org.jboss.logging.Log;
import org.jboss.util.MBeanProxy;
import org.jboss.system.ServiceMBeanSupport;

import org.jboss.metadata.XmlFileLoader;

import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.ContainerFactoryMBean;

import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** A class intended to encapsulate the complex task of making an URL pointed
 *  to an J2ee module an installed Deployment.
 *
 *	@see <related>
 *	@author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 *	@version $Revision: 1.12 $
 */
public class Installer
{
   // Constants -----------------------------------------------------
   public static final int EJB_MODULE = 0;
   public static final int WAR_MODULE = 1;
   public static final int EAR_MODULE = 2;
   public static final int RAR_MODULE = 3;

   // the order is the order of the type constants defined in this class
   private static final String[] files = {"META-INF/ejb-jar.xml", "WEB-INF/web.xml", "META-INF/application.xml"};
   
   // Attributes ----------------------------------------------------
   
   // the basedir of this Deployment within the InstallerFactories baseDir
   File baseDir;
   // the URL to install
   URL src;
   // the resulting Deployment
   Deployment d;
   
   // the log4j category for output
   Category log;

   // to get the Log and the temprary deployment dir
   InstallerFactory factory;
   
   // flag to not run execute twice
   boolean done;
   
   //copy buffer we'll re-use for copying from
   //an InputStream source to an OutputStream dest
   private byte[] copyBuffer;
   
   // Static --------------------------------------------------------
   
   static int counter = 1000;
   
   /** Number generator for temporary files.
    *  @return a new number on each call
    */
   private static int nextNumber()
   {
      return ++counter;
   }
   
   
   // Constructors --------------------------------------------------
   public Installer(InstallerFactory factory, URL src) throws IOException
   {
      this.factory = factory;
      log = factory.log;
      this.src = src;
   }

   // Public --------------------------------------------------------

   /** performes the complex task of installation
    *  @return the installed Deployment on success
    *  @throws J2eeDeploymentException
    *  @throws IOException
    */
   public Deployment execute() throws J2eeDeploymentException, IOException
   {
      if (done)
         throw new IllegalStateException("this object ("+src+")is already executed.");
      
      File localCopy = null;
      d = new Deployment();
      d.name = getName(src.toString());
      // <code author="cgjung"> is needed for redeployment purposes
      d.sourceUrl=src;
      // </code>
      
      baseDir = new File(factory.baseDir, d.name);
      d.localUrl = baseDir.toURL();
      
      try
      {
         localCopy = makeLocalCopy();
         
         // <code author="cgjung">
         // we include the manifest into the deployment
         try
         {
            d.manifest=new JarFile(localCopy).getManifest();
         } catch(IOException e)
         {
            // wrecked manifest is ignored
         }
         // </code>
         
         // determine the type...
         int type = determineType(new JarFile(localCopy));
         
         
         log.info("Create application " + d.name);
         switch (type)
         {
            case EJB_MODULE:
            {
               // just install the package
               log.info("install EJB module "+d.name);
               File f = install(new FileInputStream(localCopy), "ejb");               
               // Check for libs declared int the EJB jar manifest
               JarFile jar = new JarFile(f);
               Manifest mf = jar.getManifest();
               URL[] libs = resolveLibraries(mf, src);               
               URL localJar = f.toURL();
               d.addEjbModule(d.name, localJar, libs);
            }
            break;

            case WAR_MODULE:
            {
               // just inflate the package and determine the context name
               String webContext = getWebContext(src.toString());
               
               log.info("inflate and install WEB module "+d.name);
               File f = installInflate(new FileInputStream(localCopy), "web");
               // Check for libs declared int the WAR jar manifest
               URL[] libs = {};
               try
               {
                  InputStream mfIn = new FileInputStream(new File(f, "META-INF/MANIFEST.MF"));
                  Manifest mf = new Manifest(mfIn);
                  libs = resolveLibraries(mf, src);
               }
               catch (FileNotFoundException _fnfe)
               {
                  // No manifest
               }

               URL localJar = f.toURL();
               d.addWebModule(d.name, webContext, localJar, libs);
            }
            break;

            case EAR_MODULE:
            {
               // reading the deployment descriptor...
               JarFile jarFile = new JarFile(localCopy);
               J2eeApplicationMetaData app = null;
               try
               {
                  InputStream in = jarFile.getInputStream(jarFile.getEntry(files[type]));
                  XmlFileLoader xfl = new XmlFileLoader();
                  Element root = xfl.getDocument(in, files[type]).getDocumentElement();
                  app = new J2eeApplicationMetaData(root);
                  in.close();
               }
               catch (IOException _ioe)
               {
                  throw new J2eeDeploymentException("Error in accessing application metadata: "+_ioe.getMessage());
               }
               catch (DeploymentException _de)
               {
                  throw new J2eeDeploymentException("Error in parsing application.xml: "+_de.getMessage());
               }
               catch (NullPointerException _npe)
               {
                  throw new J2eeDeploymentException("unexpected error: application.xml was found once but not a second time?!");
               }
               
               // iterating the ejb and web modules and install them
               File f = null;
               J2eeModuleMetaData mod;
               ArrayList ejbJars = new ArrayList();
               Iterator it = app.getModules();
               while (it.hasNext())
               {
                  // iterate the ear modules
                  mod = (J2eeModuleMetaData) it.next();
                  
                  if (mod.isEjb())
                  {
                     String name = mod.getFileName();                     
                     log.info("install EJB module "+name);
                     try
                     {
                        String ejbJarName = mod.getFileName();
                        InputStream in = jarFile.getInputStream(jarFile.getEntry(ejbJarName));
                        f = install(in, "ejb");
                        // Check for libs declared int the EJB jar manifest
                        JarFile jar = new JarFile(f);
                        Manifest mf = jar.getManifest();
                        URL jarURL = new URL("jar:file:"+localCopy.getAbsolutePath()+"!/");
                        URL[] libs = resolveLibraries(mf, jarURL);
                        URL localJar = f.toURL();
                        d.addEjbModule(name, localJar, libs);
                        ejbJars.add(localJar);
                     }
                     catch (IOException _ioe)
                     {
                        throw _ioe;
                     }
                     catch (NullPointerException _npe)
                     {
                        log.info("module "+name+" not found in "+d.name);
                        throw new J2eeDeploymentException("module "+name+" not found in "+d.name);
                     }
                  }
                  else if (mod.isWeb())
                  {
                     String name = mod.getFileName();                     
                     String webContext = mod.getWebContext();
                     if (webContext == null)
                        // this line here is not smart yet!!!
                        webContext = name.substring(Math.max(0, name.lastIndexOf("/")));

                     // make sure the context starts with a slash
                     if (!webContext.startsWith("/"))
                        webContext = "/"+webContext;
                     
                     log.info("inflate and install WEB module "+name);
                     try
                     {
                        InputStream in = jarFile.getInputStream(jarFile.getEntry(name));
                        f = installInflate(in, "web");
                        
                        // Check for libs declared int the WAR jar manifest
                        URL[] libs = {};
                        try
                        {
                           InputStream mfIn = new FileInputStream(new File(f, "META-INF/MANIFEST.MF"));
                           Manifest mf = new Manifest(mfIn);
                           URL jarURL = new URL("jar:file:"+localCopy.getAbsolutePath()+"!/");
                           libs = resolveLibraries(mf, jarURL);
                        }
                        catch (FileNotFoundException _fnfe)
                        {}

                        URL localJar = f.toURL();
                        d.addWebModule(name, webContext, localJar, libs);
                     }
                     catch (IOException _ioe)
                     {
                        throw _ioe;
                     }
                     catch (NullPointerException _npe)
                     {
                        log.info("module "+name+" not found in "+d.name);
                        throw new J2eeDeploymentException("module "+name+" not found in "+d.name);
                     }
                  }
                  // other packages we dont care about (currently)
               }

               // put all ejb jars to the common classpath too
               if( ejbJars.size() > 0 )
                  log.info("add all ejb jar files to the common classpath");
               for(int e = 0; e < ejbJars.size(); e ++)
               {
                  URL jar = (URL) ejbJars.get(e);
                  d.commonUrls.add(jar);
               }
            }
            break;
         }
         saveConfig();
         
      }
      catch (Exception _ex)
      {
         // ooops something went wrong - clean up unfinished installation...
         try
         {
            deleteRecursive(baseDir);
         }
         catch (Exception _e)
         {
            log.debug("couldnt remove unused files in "+baseDir.getAbsolutePath()+": "+_e.getMessage());
         }
         
         if (_ex instanceof J2eeDeploymentException)
            throw (J2eeDeploymentException) _ex;
         
         if (_ex instanceof IOException)
            throw (IOException) _ex;
         
         log.error("unexpected exception occured", _ex);
         throw new J2eeDeploymentException("unexpected exception occured (see server trace)");
      }
      finally
      {
         // mark as done
         done = true;
         
         // finally try to remove the localCopy
         // must be changed to make really sure it is finally done!!!
         try
         {
            if (localCopy != null)
               localCopy.delete();
         }
         catch (Exception _e)
         {
            log.debug("couldnt remove temporary copy "+localCopy+": "+_e.getMessage());
         }
      }
      
      return d;
   }
   
   
   // Private -------------------------------------------------------
   
   /** Determines the type (ejb/war/ear) of the given JarFile by trying to access
    *  the deployment descriptor.
    *  @param _file the JarFile to test
    *  @return the number (0-2) of the matched deployment descriptor taken from the
    *          <strong>file</strong> class member.
    *  @throws J2eeDeploymentException in case not descriptor was found.
    */
   private int determineType(JarFile _file) throws J2eeDeploymentException
   {
      int result = -1;
      
      // trying to access the possible descriptor files
      ZipEntry dd = null;
      for (int i = 0; i < files.length && dd == null; ++i)
      {
         dd = _file.getEntry(files[i]);
         result = i;
      }
      
      // nothing found...
      if (dd == null)
      {
         // no descriptor found ...
         // lets seek if the app assembler is a little ???
         Enumeration e = _file.entries();
         while (e.hasMoreElements())
         {
            dd = (ZipEntry)e.nextElement();
            String name = dd.getName();
            for (int i = 0; i < files.length; ++i)
               if (name.equalsIgnoreCase(files[i]))
                  throw new J2eeDeploymentException("no deployment descriptor found but file that could be ment as: "+name+" <-> "+files[i]);
         }
         
         // really nothing found
         throw new J2eeDeploymentException("no deployment descriptor ("+files[0]+", "+files[1]+", "+files[2]+") found");
      }
      
      return result;
   }
   
   
   /** Downloads the jar file or directory the src URL points to.
    *  In case of directory it becomes packed to a jar file.
    *  @return a File object representing the downloaded module
    *  @throws IOException
    */
   private File makeLocalCopy() throws IOException
   {
      URL dest = null;
      if (src.getProtocol().equals("file") &&
      new File(src.getFile()).isDirectory())
      {
         dest = URLWizzard.downloadAndPackTemporary(src, factory.baseDir.toURL(), "copy", ".zip");
      }
      else
      {
         dest = URLWizzard.downloadTemporary(src, factory.baseDir.toURL(), "copy", ".zip");
      }
      
      return new File(dest.getFile());
   }
   
   /** Resolves all <strong>Class-Path:</strong> entries from the Manifest
    *  @param mf the Manifest to process
    *  @param baseURL the URL to which the Class-Path entries will be relative
    @return URL[] the array of URLs for the valid Class-Path entries
    */
   private URL[] resolveLibraries(Manifest mf, URL baseURL)
   {
      String classPath = null;
      if( mf != null )
      {
         Attributes mainAttributes = mf.getMainAttributes();
         classPath = mainAttributes.getValue(Attributes.Name.CLASS_PATH);
      }

      URL[] libs = {};
      if (classPath != null)
      {
         ArrayList tmp = new ArrayList();
         StringTokenizer st = new StringTokenizer(classPath);
         log.debug("resolveLibraries: "+classPath);
         while (st.hasMoreTokens())
         {
            String tk = st.nextToken();
            try
            {
               URL lib = new URL(baseURL, tk);
               URL baseDir = factory.baseDir.toURL();
               URL localURL = URLWizzard.downloadTemporary(lib, baseDir, "lib", ".jar");
               tmp.add(localURL);
               log.debug("added "+lib+" to common classpath");
            }
            catch (IOException _ioe)
            {
               log.warn("Failed to add "+tk+" to common classpath: "+_ioe.getMessage());
            }
         }
         libs = new URL[tmp.size()];
         tmp.toArray(libs);
      }
      return libs;
   }

   /** Creates a temporary jar file from the InputStream with the given _prefix in its name.
    *  @param _in an InputStream of a jar/zip file
    *  @param _prefix name prefix for the temporary file (prefix&lt;number&gt;.jar)
    *  @return a File representing the newly created jar file
    *  @throws IOException
    */
   private File install(InputStream _in, String _prefix) throws IOException
   {
      File result = createTmpFile(baseDir, _prefix, ".jar");
      copy(_in, new FileOutputStream(result), true);
      
      return result;
   }
   
   /** Same as <code>install()</code> but inflates the jar file.
    *  @param _in the Inputstream of an jar/zip file
    *  @param _prefix the name prefix for the temporary directory that will become created
    *  @return a File representing the newly created directory
    *  throws IOException
    */
   private File installInflate(InputStream _in, String _prefix) throws IOException
   {
      File result = createTmpDir(baseDir, _prefix);
      inflate(new ZipInputStream(_in), result);
      
      return result;
   }
   
   
   /** Serializes the Deployment object.
    *  @throws IOException
    */
   private void saveConfig() throws IOException
   {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(baseDir, J2eeDeployer.CONFIG)));
      
      out.writeObject(d);
      out.flush();
      out.close();
   }
   
   /** Truncates the the name (the last cluster of letters after the last slash.
    *  @param _url an URL or something like that
    */
   private String getName(String _url)
   {
      String result = _url;
      
      if (result.endsWith("/"))
         result = result.substring(0, result.length() - 1);
      
      result = result.substring(result.lastIndexOf("/") + 1);
      
      return result;
   }
   
   
   /** Generates a webcontex for the given url
    *  @param _url an URL or something like that
    */
   public String getWebContext(String _url)
   {
      String s = getName(_url);
      
      // truncate the file extension
      int p = s.lastIndexOf(".");
      if (p != -1)
         s = s.substring(0, p);
      
      return "/" + s.replace('.', '/');
   }
   
   
   /**
    * Get the copy buffer (create new if null)
    */
   private byte[] getCopyBuffer()
   {
      if (copyBuffer == null)
      {
         copyBuffer = new byte[1024*1024];
      }
      return copyBuffer;
   }
   
   
   /** Writes all from one stream into the other.
    *  @param _in the source
    *  @param _out the destination
    *  @param _closeInput indicates if the source stream shall be closed when finished
    *         (the dest stream gets closed anyway)
    *  @throws IOException
    */
   private void copy(InputStream _in, OutputStream _out, boolean _closeInput) throws IOException
   {
      byte[] buffer = getCopyBuffer();
      int read;
      while (true)
      {
         read = _in.read(buffer);
         if (read == -1)
            break;
         
         _out.write(buffer, 0, read);
      }
      
      _out.flush();
      _out.close();
      if (_closeInput)
         _in.close();
   }
   
   /** Infaltes a jar/zip file represented by the given stream into the given directory.
    *  @param _in the jar/zip file to extract
    *  @param _destDir the root dir for the extracted jar/zip
    *  @throws IOException
    */
   private void inflate(ZipInputStream _in, File _destDir) throws IOException
   {
      if (_destDir.exists())
         deleteRecursive(_destDir);
      
      _destDir.mkdirs();
      
      OutputStream out;
      ZipEntry entry;
      while ((entry = _in.getNextEntry()) != null)
      {
         String name = entry.getName();
         if (!entry.isDirectory()) // there are not all directories listed (?!)- so this way...
         {
            // create directory structure if necessary
            // System.out.println ("entry: "+name);
            int x = name.lastIndexOf("/");
            if (x != -1)
            {
               File dir = new File(_destDir.getCanonicalPath() + File.separator + name.substring(0, x));
               if (!dir.exists())
                  dir.mkdirs();
            }
            // and extract...
            out = new FileOutputStream(_destDir.getCanonicalPath() + File.separator + name);
            copy(_in, out, false);
         }
      }
      _in.close();
   }
   
   /** Creates  a temporary (unique) file.
    *  @param _parent the directory in which to create the file
    *  @param _prefix the file name prefix
    *  @param _suffix the file names suffix
    *  @throws IOException
    */
   private File createTmpFile(File _parent, String _prefix, String _suffix) throws IOException
   {
      if (_parent.exists())
      {
         if (!_parent.isDirectory())
            throw new IOException("parent file "+_parent.getCanonicalPath()+" is not a directory");
      }
      else
      {
         if (!_parent.mkdirs())
            throw new IOException("couldnt create parent directory: "+_parent.getCanonicalPath());
      }
      
      File result = null;
      do
      {
         result = new File(_parent, _prefix+nextNumber()+_suffix);
      }
      while (result.exists());
      
      return result;
   }
   
   /** Creates  a temporary (unique) directory.
    *  @param _parent the directory in which to create the file
    *  @param _prefix the file name prefix
    *  @throws IOException
    */
   private File createTmpDir(File _parent, String _prefix) throws IOException
   {
      File result = null;
      do
      {
         result = new File(_parent, _prefix+nextNumber());
      }
      while (result.exists());
      
      if (!result.mkdirs())
         throw new IOException("couldnt create directory: "+result.getCanonicalPath());
      
      return result;
   }
   
   /** deletes a File recursive
    *  @throws IOException
    */
   private void deleteRecursive(File _file) throws IOException
   {
      if (_file.exists())
      {
         if (_file.isDirectory())
         {
            File[] files = _file.listFiles();
            for (int i = 0, l = files.length; i < l; ++i)
               deleteRecursive(files[i]);
         }
         _file.delete();
      }
   }
   
}
