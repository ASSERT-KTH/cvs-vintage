/*
 * jBoss, the OpenSource EJB server
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
import org.jboss.util.ServiceMBeanSupport;

import org.jboss.metadata.XmlFileLoader;

import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.ContainerFactoryMBean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;



/** A class intended to encapsulate the complex task of making an URL pointed
 *  to an J2ee module an installed Deployment.
 *
 *	@see <related>
 *	@author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 *	@version $Revision: 1.6 $
 */
public class Installer
{
   // Constants -----------------------------------------------------


	// the order is the order of the type constants defined in this class
	private static final String[] files = {"META-INF/ejb-jar.xml", "WEB-INF/web.xml", "META-INF/application.xml"};

	// Attributes ----------------------------------------------------

	// the basedir of this Deployment within the InstallerFactories baseDir
	File baseDir;
	// the URL to install
	URL src;
	// the resulting Deployment
	Deployment d;

	// the Log for output
	Log log;

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
	public Installer (InstallerFactory _factory, URL _src) throws IOException
	{
		factory = _factory;
		log = _factory.log;
		src = _src;
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
			throw new IllegalStateException ("this object ("+src+")is already executed.");


		File localCopy = null;
		d = new Deployment();
		Deployment.Module m = null;
		d.name = getName (src.toString());

                // <code author="cgjung"> is needed for redeployment purposes
                d.sourceUrl=src;
                // </code>

	    baseDir = new File (factory.baseDir, d.name);

		d.localUrl = baseDir.toURL ();

		try
		{
			localCopy = makeLocalCopy();

			// determine the type...
			int type = determineType (new JarFile(localCopy));

			log.log ("Create application " + d.name);
			switch (type)
			{
			case 0: // ejb package...

				// just install the package
				m = d.newModule();
				m.name = d.name; // module name = app name

				log.log("install module "+m.name);
				File f = install(new FileInputStream(localCopy), "ejb");

				// check for referenced libraries
				Manifest mf = new JarFile(f).getManifest ();
				if (mf != null)
					addLibraries (mf, src);

				m.localUrls.add (f.toURL());

				d.ejbModules.add(m);
				break;

			case 1: // web package

				// just inflate the package and determine the context name
				m = d.newModule();
				m.name = d.name; // module name = app name
				m.webContext = getWebContext (src.toString());

				log.log("inflate and install module "+m.name);
				f = installInflate(new FileInputStream(localCopy), "web");

				try
				{
					InputStream mfIn = new FileInputStream (new File (f, "META-INF/MANIFEST.MF"));
					addLibraries (new Manifest(mfIn), src);
				}
				catch (FileNotFoundException _fnfe) {}

				m.localUrls.add (f.toURL());

				d.webModules.add(m);
				break;

			case 2: // application package

				// reading the deployment descriptor...
				JarFile jarFile = new JarFile (localCopy);
				J2eeApplicationMetaData app = null;
				try
				{
					InputStream in = jarFile.getInputStream(jarFile.getEntry(files[type]));
					Element root = XmlFileLoader.getDocument (in).getDocumentElement ();
					app = new J2eeApplicationMetaData (root);
					in.close();
				}
				catch (IOException _ioe)
				{
					throw new J2eeDeploymentException ("Error in accessing application metadata: "+_ioe.getMessage ());
				}
				catch (DeploymentException _de)
				{
					throw new J2eeDeploymentException ("Error in parsing application.xml: "+_de.getMessage ());
				}
				catch (NullPointerException _npe)
				{
					throw new J2eeDeploymentException ("unexpected error: application.xml was found once but not a second time?!");
				}

				// iterating the ejb and web modules and install them
				J2eeModuleMetaData mod;
				Iterator it = app.getModules ();
				while (it.hasNext ())
				{
					// iterate the ear modules
					mod = (J2eeModuleMetaData) it.next ();

					if (mod.isEjb ())
					{
						m = d.newModule();
						m.name = mod.getFileName();

						log.log("install module "+m.name);
						try
						{
							InputStream in = jarFile.getInputStream(jarFile.getEntry(mod.getFileName()));
							f = install(in, "ejb");

							// check for referenced libraries
							mf = new JarFile(f).getManifest ();
							if (mf != null)
								addLibraries (mf, new URL("jar:file:"+localCopy.getAbsolutePath()+"!/"));

							m.localUrls.add(f.toURL());
						}
						catch (IOException _ioe)
						{
							throw _ioe;
						}
						catch (NullPointerException _npe)
						{
							log.log("module "+m.name+" not found in "+d.name);
							throw new J2eeDeploymentException ("module "+m.name+" not found in "+d.name);
						}

						d.ejbModules.add(m);

					}
					else if (mod.isWeb ())
					{
						m = d.newModule();
						m.name = mod.getFileName();

						m.webContext = mod.getWebContext();
						if (m.webContext == null)
							// this line here is not smart yet!!!
							m.webContext = mod.getFileName ().substring (Math.max (0, mod.getFileName ().lastIndexOf ("/")));

						// make sure the context starts with a slash
						if (!m.webContext.startsWith ("/"))
							m.webContext = "/"+m.webContext;

						log.log("inflate and install module "+m.name);
						try
						{
							InputStream in = jarFile.getInputStream(jarFile.getEntry(mod.getFileName()));
							f = installInflate(in, "web");

							// check for referenced libraries
							try
							{
								InputStream mfIn = new FileInputStream (new File (f, "META-INF/MANIFEST.MF"));
								addLibraries (new Manifest(mfIn), new URL("jar:file:"+localCopy.getAbsolutePath()+"!/"));
							}
							catch (FileNotFoundException _fnfe) {}
							m.localUrls.add(f.toURL());
						}
						catch (IOException _ioe)
						{
							throw _ioe;
						}
						catch (NullPointerException _npe)
						{
							log.log("module "+m.name+" not found in "+d.name);
							throw new J2eeDeploymentException ("module "+m.name+" not found in "+d.name);
						}

						d.webModules.add(m);
					}
					// other packages we dont care about (currently)
				}

/*
				// walk throgh the .ear file and download all jar files that are included
				// (and not yet downloaded) and put them into the common classpath
				Enumeration enum = jarFile.entries();
				while (enum.hasMoreElements())
				{
					ZipEntry entry = (ZipEntry)enum.nextElement();
					if (entry.getName().endsWith(".jar") && !installedJars.contains(entry.getName()))
					{
						log.log("add "+entry.getName()+" to common classpath");
						d.commonUrls.add (install(jarFile.getInputStream(entry), "lib").toURL());
					}
				}

*/
				// put all ejb jars to the common classpath too
				it = d.ejbModules.iterator();
				if (it.hasNext())
					log.log("add all ejb jar files to the common classpath");
				while (it.hasNext())
					d.commonUrls.add (((Deployment.Module)it.next()).localUrls.firstElement());
				break;
			}
			saveConfig ();

		}
		catch (Exception _ex)
		{
			// ooops something went wrong - clean up unfinished installation...
			try
			{
				deleteRecursive (baseDir);
			}
			catch (Exception _e)
			{
				log.debug("couldnt remove unused files in "+baseDir.getAbsolutePath()+": "+_e.getMessage());
			}

			if (_ex instanceof J2eeDeploymentException)
				throw (J2eeDeploymentException) _ex;

			if (_ex instanceof IOException)
				throw (IOException) _ex;

			log.exception (_ex);
			throw new J2eeDeploymentException ("unexpected exception occured (see server trace)");
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
	private int determineType (JarFile _file) throws J2eeDeploymentException
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
			throw new J2eeDeploymentException ("no deployment descriptor ("+files[0]+", "+files[1]+", "+files[2]+") found");
		}

		return result;
	}


	/** Downloads the jar file or directory the src URL points to.
	 *  In case of directory it becomes packed to a jar file.
	 *  @return a File object representing the downloaded module
	 *  @throws IOException
	 */
	private File makeLocalCopy () throws IOException
	{
		URL dest = null;
		if (src.getProtocol().equals ("file") &&
				new File (src.getFile ()).isDirectory ())
		{
			dest = URLWizzard.downloadAndPackTemporary (src, factory.baseDir.toURL (), "copy", ".zip");
		}
		else
		{
			dest = URLWizzard.downloadTemporary (src, factory.baseDir.toURL (), "copy", ".zip");
		}

		return new File (dest.getFile());
	}

	/** Adds all <strong>Class-Path:</strong> entries from the Manifest to the
	 *  Deployments commonUrls member.
	 *  @param _mf the Manifest to process
	 *  @param _anchestor the URL to which the Class-Path entries will be relative
	 *  @throws IOException
	 */
	private void addLibraries (Manifest _mf, URL _anchestor) throws IOException
	{
		String attr = _mf.getMainAttributes().getValue (Attributes.Name.CLASS_PATH);
		if (attr != null)
		{
			StringTokenizer st = new StringTokenizer (attr);
			while (st.hasMoreTokens())
			{
				String tk = st.nextToken ();
				try
				{
					URL lib = new URL (_anchestor, tk);
					d.commonUrls.add (URLWizzard.downloadTemporary (lib, factory.baseDir.toURL (), "lib", ".jar"));
					log.log("added "+lib+" to common classpath");
				}
				catch (IOException _ioe)
				{
					log.log("couldnt add "+tk+" to common classpath: "+_ioe.getMessage());
				}
			}
		}
	}


	/** Creates a temporary jar file from the InputStream with the given _prefix in its name.
	 *  @param _in an InputStream of a jar/zip file
	 *  @param _prefix name prefix for the temporary file (prefix&lt;number&gt;.jar)
	 *  @return a File representing the newly created jar file
	 *  @throws IOException
	 */
	private File install (InputStream _in, String _prefix) throws IOException
	{
		File result = createTmpFile(baseDir, _prefix, ".jar");
		copy (_in, new FileOutputStream(result), true);

		return result;
	}

	/** Same as <code>install()</code> but inflates the jar file.
	 *  @param _in the Inputstream of an jar/zip file
	 *  @param _prefix the name prefix for the temporary directory that will become created
	 *  @return a File representing the newly created directory
	 *  throws IOException
	 */
	private File installInflate (InputStream _in, String _prefix) throws IOException
	{
		File result = createTmpDir(baseDir, _prefix);
		inflate (new ZipInputStream(_in), result);

		return result;
	}


	/** Serializes the Deployment object.
	 *  @throws IOException
	 */
	private void saveConfig () throws IOException
	{
		ObjectOutputStream out = new ObjectOutputStream (new FileOutputStream (new File (baseDir, J2eeDeployer.CONFIG)));

		out.writeObject (d);
		out.flush ();
		out.close ();
	}

	/** Truncates the the name (the last cluster of letters after the last slash.
	 *  @param _url an URL or something like that
	 */
	private String getName (String _url)
	{
		String result = _url;

		if (result.endsWith ("/"))
			result = result.substring (0, result.length() - 1);

		result = result.substring (result.lastIndexOf ("/") + 1);

		return result;
    }


	/** Generates a webcontex for the given url
	 *  @param _url an URL or something like that
	 */
	public String getWebContext (String _url)
	{
		String s = getName (_url);

		// truncate the file extension
		int p = s.lastIndexOf (".");
		if (p != -1)
			s = s.substring (0, p);

		return "/" + s.replace ('.', '/');
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
	private void copy (InputStream _in, OutputStream _out, boolean _closeInput) throws IOException
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
	private void inflate (ZipInputStream _in, File _destDir) throws IOException
	{
		if (_destDir.exists ())
			deleteRecursive (_destDir);

		_destDir.mkdirs ();

		OutputStream out;
		ZipEntry entry;
		while ((entry = _in.getNextEntry ()) != null)
        {
         String name = entry.getName ();
         if (!entry.isDirectory ()) // there are not all directories listed (?!)- so this way...
         {
            // create directory structure if necessary
            // System.out.println ("entry: "+name);
            int x = name.lastIndexOf ("/");
            if (x != -1)
            {
               File dir = new File (_destDir.getCanonicalPath () + File.separator + name.substring (0, x));
               if (!dir.exists ())
                  dir.mkdirs ();
            }
            // and extract...
            out = new FileOutputStream (_destDir.getCanonicalPath () + File.separator + name);
            copy (_in, out, false);
         }
      }
      _in.close ();
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
				throw new IOException ("parent file "+_parent.getCanonicalPath()+" is not a directory");
		}
		else
		{
			if (!_parent.mkdirs())
				throw new IOException ("couldnt create parent directory: "+_parent.getCanonicalPath());
		}

		File result = null;
		do
		{
			result = new File (_parent, _prefix+nextNumber()+_suffix);
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
			result = new File (_parent, _prefix+nextNumber());
		}
		while (result.exists());

		if (!result.mkdirs())
			throw new IOException ("couldnt create directory: "+result.getCanonicalPath());

		return result;
	}

	/** deletes a File recursive
	 *  @throws IOException
	 */
	private void deleteRecursive (File _file) throws IOException
	{
		if (_file.exists())
        {
			if (_file.isDirectory ())
            {
				File[] files = _file.listFiles ();
				for (int i = 0, l = files.length; i < l; ++i)
					deleteRecursive(files[i]);
			}
			_file.delete ();
		}
	}

}
