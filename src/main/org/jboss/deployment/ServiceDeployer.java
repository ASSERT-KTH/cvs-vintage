/*
* JBoss, the OpenSource J2EE server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.deployment;


import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collections;
import java.util.StringTokenizer;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanException;
import javax.management.RuntimeMBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.loading.MLet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.jboss.system.Service;
import org.jboss.system.URLClassLoader;
import org.jboss.system.MBeanClassLoader;
import org.jboss.system.ServiceMBeanSupport;


/** 
* This is the main Service Deployer API.
*   
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @see org.jboss.system.Service
*
* @version $Revision: 1.1 $
*
*   <p><b>20010830 marc fleury:</b>
*   <ul>
*      initial import
*   <li> 
*   </ul>
*/
public class ServiceDeployer
extends ServiceMBeanSupport
implements ServiceDeployerMBean 
{
	
	// Attributes --------------------------------------------------------
	// each url can spawn a series of MLet classloaders that are specific to it and cycled
	private Map urlToClassLoadersSetMap;
	
	// each url can describe many Services, we keep the ObjectNames in here
	private Map urlToServicesSetMap;
	
	// JMX
	private MBeanServer server;
	
	// Public --------------------------------------------------------
	
	public String getName() {return "ServiceDeployer";}
	
	public FilenameFilter getDeployableFilter()
	{
		return new FilenameFilter()
		{
			public boolean accept(File dir, String filename)
			{
				filename=filename.toLowerCase();
				return (filename.endsWith(".jsr") ||filename.endsWith("service.xml"));
			}
		};
	}
	
	public void deploy (String url)
	throws MalformedURLException, IOException, DeploymentException
	{
		
		//	System.out.println("THE DOCUMENT LOAD "+url);
		
		if (isDeployed(url)) undeploy(url);
			
		// The Document describing the service
		Document document = null;
		
		/**
		* First register the classloaders for this deployment
		* If it is a jsr, the jsr points to itself
		* If it is a something-service.xml then it looks for 
		*     <classpath>
		*         <codebase>http://bla.com (or file://bla.com)</codebase> default is system library dir
		*         <archives> bla.jar, bla2.jar, bla3.jar </archives> where bla is relative to codebase
		*	   </classpath>
		*
		*/
		
		// Support for the new packaged format
		try {
			if (url.endsWith(".jsr")) {
				
				URLClassLoader cl = new URLClassLoader(new URL[] {new URL(url)});
				
				document = getDocument("META-INF/jboss-service.xml", cl);
			
			}
			
			// We can deploy bare xml files as well
			else if (url.endsWith("service.xml")) 
			{	
				document = getDocument(url, null);
			
			}				
		
		} catch (Exception ignored) { throw new DeploymentException("No valid service.xml file found"+ignored.getMessage());  }
		
		// The service.xml file can define jar the classes it contains depend on
		// We should only have one codebase (or none at all)
		Element classpath = (Element) document.getElementsByTagName("classpath").item(0);
		
		String codebase = "";
		String archives = ""; 
		
		//Does it specify a codebase?
		if (classpath != null)
		{
			// Load the codebase
			codebase = classpath.getAttribute("codebase").trim();
			
			// Let's make sure the formatting of the codebase ends with the /
			if (codebase.startsWith("file:") && !codebase.endsWith(File.separator)) { codebase += File.separator;}
				else if (codebase.startsWith("http:") && !codebase.endsWith("/")) { codebase += "/";}
				
			//Load the archives
			archives = classpath.getAttribute("archives").trim();			
		}
		
		if (codebase.startsWith("file:") && archives.equals("")) 
		{
			try {
				File dir = new File(codebase.substring(5));
				// The patchDir can only be a File one, local
				File[] jars = dir.listFiles(new java.io.FileFilter()
					{
						public boolean accept(File pathname)
						{
							String name2 = pathname.getName();
							return name2.endsWith(".jar") || name2.endsWith(".zip");
						}
					}
				);
				for(int j = 0; jars != null && j < jars.length; j ++)
				{
					File jar = jars[j];
					URL u = jar.getCanonicalFile().toURL();
					
					URLClassLoader cl0 = new URLClassLoader(new URL[] {u});
				
				}
			}catch (Exception e) {e.printStackTrace();throw new DeploymentException(e.getMessage());}
		}		
		
		// Still no codebase? get the system default
		else if (codebase.equals("")) codebase = System.getProperty("jboss.system.libraryDirectory");
			
		// We have an archive whatever the codebase go ahead and load the libraries
		if (!archives.equals("")) 
		{
			
			StringTokenizer st = new StringTokenizer(archives, ",");
			while (st.hasMoreTokens()) 
			{
				String jar = st.nextToken().trim();
				
				URL archive = new URL(codebase+jar);
				
				URLClassLoader cl1 = new URLClassLoader(new URL[] {archive});
			
			}
		}  
		
		else if (codebase.startsWith("http:")) throw new DeploymentException ("Loading from a http:// codebase with no jars specified. Please fix jboss-service.xml in your configuration");
			
		
		// The libraries are loaded we can now load the mbeans 
		
		NodeList nl = document.getElementsByTagName("mbean");
		for (int i = 0 ; i < nl.getLength() ; i++) 	
		{
			
			Element mbean = (Element) nl.item(i);
			
			try {
				
				ObjectName service = (ObjectName) server.invoke(
					new ObjectName("JBOSS-SYSTEM:spine=ServiceController"),
					"deploy",
					new Object[] {mbean},
					new String[] {"org.w3c.dom.Element"});
				
				// marcf: I don't think we should keep track and undeploy... 
				Set services = (Set) urlToServicesSetMap.get(System.getProperty("jboss.system.configurationDirectory") + "jboss-service.xml");
				
				if (services == null) 
				{
					services = Collections.synchronizedSet(new HashSet());
					urlToServicesSetMap.put(System.getProperty("jboss.system.configurationDirectory") + "jboss-service.xml", services);
				}
				
				services.add(service);
			}
			catch (MBeanException mbe) {mbe.getTargetException().printStackTrace();}
			catch (RuntimeMBeanException rbe) {rbe.getTargetException().printStackTrace();}
			catch (MalformedObjectNameException mone) {} 
			catch (ReflectionException re) {} 
			catch (InstanceNotFoundException re) {} 
			catch (Exception e) {e.printStackTrace();}
		}
	}
	
	
	public void undeploy (String url)
	throws MalformedURLException, IOException, DeploymentException {
		
		Set set = (Set) urlToServicesSetMap.remove(url);
		
		if (set != null) 
		{
			
			Iterator iterator = set.iterator();
			while (iterator.hasNext()) 
			{
				ObjectName name = (ObjectName) iterator.next();
				
				try 
				{
					server.invoke(
						new ObjectName("JBOSS-SYSTEM:spine=ServiceController"),
						"undeploy",
						new Object[] {name},
						new String[] {"javax.management.ObjectName"});
				}
				catch (Exception e) { e.printStackTrace();}
			}
		}	
	};
	
	public boolean isDeployed (String url)
	throws MalformedURLException, DeploymentException {
		
		return urlToClassLoadersSetMap.containsKey(url);
	
	};
	
	/*
	* For legacy reasons we include the support for the old jboss.jcml now jboss-service.xml
	* 
	* eventually all the services should move to a big jsr
	*
	*/
	public ObjectName preRegister(MBeanServer server, ObjectName name)
	throws java.lang.Exception
	{
		
		System.out.println("About to load the CLassPath");
		this.server = server;
		
		return name==null ? new ObjectName(OBJECT_NAME) : name;	
	}
	
	
	public void postRegister(java.lang.Boolean registrationDone)
	{
		try {
			
			//Encapsulate with a ServiceClassLoader
			urlToClassLoadersSetMap = Collections.synchronizedMap(new HashMap());
			urlToServicesSetMap = Collections.synchronizedMap(new HashMap());
			
			//Initialize the libraries for the server by default we add the libraries in lib/services
			// and client
			
			Document document = null;
			
			try 
			{			
				document = getDocument(System.getProperty("jboss.system.configurationDirectory") + "jboss-service.xml", null);
			} 
			catch (Exception e) 
			{
				// for legacy reasons try jboss.jcml
				document = getDocument(System.getProperty("jboss.system.configurationDirectory") + "jboss.jcml", null);
			}
			
			/**
			* Initialize the system classpath by adding all jars found 
			*/
			
			// We should only have one codebase (or none at all)
			Element classpath = (Element) document.getElementsByTagName("classpath").item(0);
			
			String codebase = "";
			String archives = ""; 
			
			//Does it specify a codebase?
			if (classpath != null)
			{
				System.out.println("I do see a classpath");
				// Load the codebase
				codebase = classpath.getAttribute("codebase").trim();
				
				// Let's make sure the formatting of the codebase ends with the /
				if (codebase.startsWith("file:") && !codebase.endsWith(File.separator)) { codebase += File.separator;}
					else if (codebase.startsWith("http:") && !codebase.endsWith("/")) { codebase += "/";}
					
				//Load the archives
				archives = classpath.getAttribute("archives").trim();			
			}
			
			else System.out.println("I DONT see a classpath");
				
			// Still no codebase? get the system default
			if (codebase.equals("")) codebase = System.getProperty("jboss.system.libraryDirectory");
				
			// If there are no archives but the codebase is a file, just add listed jars 
			// This essentially enables one to specify just the codebase and in fact none at all
			// in jboss-service.xml (only applies for boot file)
			if (codebase.startsWith("file:") && archives.equals("")) 
			{
				try {
					File dir = new File(codebase.substring(5));
					// The patchDir can only be a File one, local
					File[] jars = dir.listFiles(new java.io.FileFilter()
						{
							public boolean accept(File pathname)
							{
								String name2 = pathname.getName();
								return name2.endsWith(".jar") || name2.endsWith(".zip");
							}
						}
					);
					for(int j = 0; jars != null && j < jars.length; j ++)
					{
						File jar = jars[j];
						URL u = jar.getCanonicalFile().toURL();
						
						URLClassLoader cl0 = new URLClassLoader(new URL[] {u});
					
					}
				}catch (Exception e) {e.printStackTrace();throw e;}
			}
			
			// We have an archive whatever the codebase go ahead and load the libraries
			//	else if (!archives.equals("")) 
			
			else if (!archives.equals("")) 
			{
				
				StringTokenizer st = new StringTokenizer(archives, ",");
				while (st.hasMoreTokens()) 
				{
					String jar = st.nextToken().trim();
					
					URL archive = new URL(codebase+jar);
					
					URLClassLoader cl1 = new URLClassLoader(new URL[] {archive});
				}
			}  
			
			else throw new Exception ("Loading from a http:// codebase with no jars specified. Please fix jboss-service.xml in your configuration");
				
			// The libraries are loaded we can now load the mbeans 
			
			NodeList nl = document.getElementsByTagName("mbean");
			for (int i = 0 ; i < nl.getLength() ; i++) 	
			{
				
				Element mbean = (Element) nl.item(i);
				
				try {
					
					ObjectName service = (ObjectName) server.invoke(
						new ObjectName("JBOSS-SYSTEM:spine=ServiceController"),
						"deploy",
						new Object[] {mbean},
						new String[] {"org.w3c.dom.Element"});
					
					Set services = (Set) urlToServicesSetMap.get(System.getProperty("jboss.system.configurationDirectory") + "jboss-service.xml");
					
					if (services == null) 
					{
						services = Collections.synchronizedSet(new HashSet());
						urlToServicesSetMap.put(System.getProperty("jboss.system.configurationDirectory") + "jboss-service.xml", services);
					}
					
					services.add(service);
				}
				catch (MBeanException mbe) {mbe.getTargetException().printStackTrace();}
				catch (RuntimeMBeanException rbe) {rbe.getTargetException().printStackTrace();}
				catch (MalformedObjectNameException mone) {} 
				catch (ReflectionException re) {} 
				catch (InstanceNotFoundException re) {} 
				catch (Exception e) {e.printStackTrace();}
			
			}		
		}
		catch (Exception e) {e.printStackTrace();}
	
	}
	// Private --------------------------------------------------------
	
	private void registerClassLoader(String url, ClassLoader cl) 
	{
		
		try
		{
			//Keep the local maps 
			Set set = (Set) urlToClassLoadersSetMap.get(url);
			
			if (set == null) 
			{
				set = new HashSet();
				urlToClassLoadersSetMap.put(url, cl);
			}
			
			set.add(cl);
			
			//marcf FIXME: move to real log
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private Document getDocument(String url, ClassLoader cl)
	throws DeploymentException, MalformedURLException, IOException
	{
		// Define the input stream to the configuration file
		InputStream input = null;
		
		// Try the contextClassLoader 
		if (cl == null) 
		{
			input = Thread.currentThread().getContextClassLoader().getResourceAsStream(url);
		}
		else 
		{
			input = cl.getResourceAsStream(url);
		}
		
		//If still no document, try to interpret the url as an absolute URL
		if (input == null) {
			
			// Try to understand the URL litterally
			input = (new URL(url)).openStream();
		}
		
		//Load the configuration with a buffered reader
		StringBuffer sbufData = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		
		String sTmp;
		
		try {
			while((sTmp = br.readLine())!=null){
				sbufData.append(sTmp);
			}
		} finally {
			input.close();
		}
		
		try {
			// Parse XML
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			return parser.parse(new InputSource(new StringReader(sbufData.toString())));
		} 
		catch (SAXException e) {throw new DeploymentException(e.getMessage());}
		catch (ParserConfigurationException pce) { throw new DeploymentException(pce.getMessage());}
	
	}
}
