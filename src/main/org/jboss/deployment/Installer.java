/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment;

import java.net.URL;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Enumeration;

import javax.management.ObjectName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.jboss.logging.Logger;
import org.jboss.metadata.XmlFileLoader;

import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 * A class intended to encapsulate the complex task of making an URL pointed
 * to an J2ee module an installed Deployment.
 * <br>
 * Extended and refactored on 5th October 2001 by CGJ to also cater for 
 * connector and java-client module support 
 * 
 * @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.19 $
 */

public class Installer
{
   // Constants -----------------------------------------------------
   public static final int EJB_MODULE = 0;
   public static final int WAR_MODULE = 1;
   public static final int EAR_MODULE = 2;
   public static final int RAR_MODULE = 3;

   // the order is the order of the type constants defined in this class
   private static final String[] files = {"META-INF/ejb-jar.xml", "WEB-INF/web.xml", "META-INF/application.xml", "META-INF/ra.xml"};
   
   // Attributes ----------------------------------------------------
   
   // the basedir of this Deployment within the InstallerFactories baseDir
   File baseDir;
   // the URL to install
   URL src;
   // the resulting Deployment
   Deployment d;
   
   // the log4j category for output
   Logger log = Logger.getLogger(Installer.class);

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
      this.src = src;
   }

   // protected --------------------------------------------------
   
   /** install pure ejb module */
   protected URL executeEJBModule(String name, Deployment d, InputStream in, URL libraryRoot)  throws IOException {
       // just install the package
       if (log.isDebugEnabled()) {
          log.debug("install EJB module "+name);
       }
       
       File f = install(in, "ejb");
       // Check for libs declared int the EJB jar manifest
       JarFile jar = new JarFile(f);
       Manifest mf = jar.getManifest();
       URL[] libs = resolveLibraries(mf, libraryRoot);
       URL localJar = f.toURL();
       d.addEjbModule(name, localJar, libs);
       return localJar;
   }

   /** install pure war module */
   protected URL executeWarModule(String name, Deployment d, InputStream in, URL libraryRoot, String webContext) throws IOException {
       // just inflate the package and determine the context name
       if (log.isDebugEnabled()) {
          log.debug("inflate and install WEB module "+name);
       }
      
       File f = installInflate(in, "web");
       // Check for libs declared int the WAR jar manifest
       URL[] libs = {};
       try {
           InputStream mfIn = new FileInputStream(new File(f, "META-INF/MANIFEST.MF"));
           Manifest mf = new Manifest(mfIn);
           libs = resolveLibraries(mf, libraryRoot);
       }
       catch (FileNotFoundException _fnfe) {
           // No manifest
       }
       URL localJar = f.toURL();
       d.addWebModule(name, webContext, localJar, libs); 
       return localJar;
   }
   
   /** install pure rar module */
   protected URL executeConnectorModule(String name, Deployment d, InputStream in, URL libraryRoot) throws IOException {
       if (log.isDebugEnabled()) {
          log.debug("install CONNECTOR module "+name);
       }
       
       File f = install(in, "rar");
       // Check for libs declared int the EJB jar manifest
       JarFile jar = new JarFile(f);
       Manifest mf = jar.getManifest();
       URL[] libs = resolveLibraries(mf, libraryRoot);
       URL localJar = f.toURL();
       d.addConnectorModule(name, localJar, libs);
       return localJar;
   }

   /** install pure java client module */
   protected URL executeJavaModule(String name, Deployment d, InputStream in, URL libraryRoot) throws IOException {
       if (log.isDebugEnabled()) {
          log.debug("install JAVA application module "+name);
       }
       File f = install(in, "java");
       // Check for libs declared int the EJB jar manifest
       JarFile jar = new JarFile(f);
       Manifest mf = jar.getManifest();
       URL[] libs = resolveLibraries(mf, libraryRoot);
       URL localJar = f.toURL();
       d.addJavaModule(name, localJar, libs);
       return localJar;
   }

   /** install EAR application */
   protected void executeEARModule(String name, Deployment d, File localCopy, URL libraryRoot)
      throws J2eeDeploymentException, IOException
   {
       // reading the deployment descriptor...
       JarFile jarFile = new JarFile(localCopy);
       J2eeApplicationMetaData app = null;

       try
       {
           InputStream in = jarFile.getInputStream(jarFile.getEntry(files[EAR_MODULE]));
           XmlFileLoader xfl = new XmlFileLoader();
           Element root = xfl.getDocument(in, files[EAR_MODULE]).getDocumentElement();
           app = new J2eeApplicationMetaData(root);
           in.close();
       }
       catch (IOException e)
       {
           throw new J2eeDeploymentException("Error in accessing application metadata", e);
       }
       catch (DeploymentException e)
       {
           throw new J2eeDeploymentException("Error deploying application", e);
       }
       catch (NullPointerException e)
       {
           throw new J2eeDeploymentException("unexpected error: application.xml was found once but not a second time?!", e);
       }

       // Read application deployment descriptor
       try {
           InputStreamReader lInput = new InputStreamReader(
               jarFile.getInputStream(
                   jarFile.getEntry( files[ EAR_MODULE ] )
               )
           );
           StringWriter lOutput = new StringWriter();
           char[] lBuffer = new char[ 1024 ];
           int lLength = 0;
           while( ( lLength = lInput.read( lBuffer ) ) > 0 ) {
               lOutput.write( lBuffer, 0, lLength );
           }
           d.applicationDeploymentDescriptor = lOutput.toString();
           lInput.close();
       }
       catch( Exception e ) {
           e.printStackTrace();
       }
       
       // iterating the modules and install them
       // the library url is used to root the embedded classpaths
       libraryRoot=new URL("jar:file:"+localCopy.getAbsolutePath()+"!/");
       File f = null;
       J2eeModuleMetaData mod;
       ArrayList ejbJars = new ArrayList();
       Iterator it = app.getModules();
       while (it.hasNext()) {
           // iterate the ear modules
           mod = (J2eeModuleMetaData) it.next();
           // get the name of the module
           String modName=mod.getFileName();
           
           if (mod.isEjb()) {
               try {
                   URL localJar=executeEJBModule(modName,d,jarFile.
                    getInputStream(jarFile.getEntry(modName)),libraryRoot);
                   ejbJars.add(localJar);
               }
               catch (IOException _ioe) {
                   throw _ioe;
               }
               catch (NullPointerException _npe) {
                   log.warn("module "+modName+" not found in "+d.name);
                   throw new J2eeDeploymentException("module "+modName+" not found in "+d.name);
               }
           }
           else if (mod.isWeb()) {
               try {
                   String webContext = mod.getWebContext();
                   if (webContext == null)
                       // this line here is not smart yet!!!
                       webContext = name.substring(Math.max(0, modName.lastIndexOf("/")));
                   
                   // make sure the context starts with a slash
                   if (!webContext.startsWith("/"))
                       webContext = "/"+webContext;
                   
                   executeWarModule(modName,d,jarFile.
                    getInputStream(jarFile.getEntry(modName)),libraryRoot,webContext);
               }
               catch (IOException _ioe) {
                   throw _ioe;
               }
               catch (NullPointerException _npe) {
                   log.warn("module "+modName+" not found in "+d.name);
                   throw new J2eeDeploymentException("module "+modName+" not found in "+d.name);
               }
           } 
           else if (mod.isConnector()) {
               try{
                   executeConnectorModule(modName,d,jarFile.
                    getInputStream(jarFile.getEntry(modName)),libraryRoot);
               } catch(NullPointerException e) {
                   log.warn("module "+modName+" not found in "+d.name);
                   throw new J2eeDeploymentException("module "+modName+" not found in "+d.name);
               }
           } else if(mod.isJava()) {
               try{
                   executeJavaModule(modName,d,
                    jarFile.getInputStream(jarFile.getEntry(modName)),libraryRoot);
                } catch(NullPointerException e) {
                   log.warn("module "+modName+" not found in "+d.name);
                   throw new J2eeDeploymentException("module "+modName+" not found in "+d.name);
               }
           } //
                     
           // other packages we dont care about (currently)
       }
       
       // put all ejb jars to the common classpath too
       if( ejbJars.size() > 0 ) {
          log.debug("add all ejb jar files to the common classpath");
          for(int e = 0; e < ejbJars.size(); e ++) {
             URL jar = (URL) ejbJars.get(e);
             d.commonUrls.add(jar);
          }
       }
   }

   // Public --------------------------------------------------------

   /** performes the complex task of installation
    *  @return the installed Deployment on success
    *  @throws J2eeDeploymentException
    *  @throws IOException
    */
   public Deployment execute() throws J2eeDeploymentException, IOException
   {
      if (done) {
         throw new IllegalStateException
            ("this object ("+src+")is already executed.");
      }
      
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

         if (log.isDebugEnabled()) {
            log.debug("Create application " + d.name);
         }
         
         switch (type)
         {
            case EJB_MODULE:
            {
               executeEJBModule(d.name,d,new FileInputStream(localCopy),src);
            }
            break;

            case WAR_MODULE:
            {
                executeWarModule(d.name,d,new FileInputStream(localCopy),src,getWebContext(src.toString()));
            }
            break;

             case RAR_MODULE:
            {
                executeConnectorModule(d.name,d,new FileInputStream(localCopy),src);
            }
            break;

            case EAR_MODULE:
            {
                executeEARModule(d.name,d,localCopy,src);
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
            log.warn("could not remove unused files in " +
                     baseDir.getAbsolutePath(), _e);
         }
         
         if (_ex instanceof J2eeDeploymentException)
            throw (J2eeDeploymentException) _ex;
         
         if (_ex instanceof IOException)
            throw (IOException) _ex;
         
         log.error("unexpected exception occured", _ex);
         throw new J2eeDeploymentException("unexpected exception occured", _ex);
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
            log.warn("could not remove temporary copy " + localCopy, _e);
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

      boolean debug = log.isDebugEnabled();
      
      URL[] libs = {};
      if (classPath != null)
      {
         ArrayList tmp = new ArrayList();
         StringTokenizer st = new StringTokenizer(classPath);
         if (debug) {
            log.debug("resolveLibraries: "+classPath);
         }
         
         while (st.hasMoreTokens())
         {
            String tk = st.nextToken();
            try
            {
               URL lib = new URL(baseURL, tk);
               URL baseDir = factory.baseDir.toURL();
               URL localURL = URLWizzard.downloadTemporary(lib, baseDir, "lib", ".jar");
               tmp.add(localURL);
               if (debug) {
                  log.debug("added "+lib+" to common classpath");
               }
            }
            catch (IOException _ioe)
            {
               log.warn("Failed to add "+tk+" to common classpath", _ioe);
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
      return install(_in,_prefix,".jar");
   }
   
   /** Creates a temporary .xxx-jar file from the InputStream 
    *  with the given _prefix and _suffix in its name.
    *  @param _in an InputStream of a jar/zip file
    *  @param _prefix name prefix for the temporary file (prefix&lt;number&gt;.jar)
    *  @param _suffix name suffix for the temporary file (bla&lt;number&gt;.suffix)
    *  @return a File representing the newly created jar file
    *  @throws IOException
    */
   private File install(InputStream _in, String _prefix, String _suffix) throws IOException
   {
      File result = createTmpFile(baseDir, _prefix, _suffix);
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
               File dir = new File(_destDir, name.substring(0, x));
               if (!dir.exists())
                  dir.mkdirs();
            }
            // and extract...
            File file = new File(_destDir, name);
            out = new FileOutputStream(file);
            copy(_in, out, false);
            // Preserve the last modified time if it exists
            long time = entry.getTime();
            if( time > 0 )
               file.setLastModified(time);
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
         throw new IOException("could not create directory: "+result.getCanonicalPath());
      
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
