/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;

import org.w3c.dom.*;

import com.dreambean.awt.*;
import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;
import com.dreambean.ejx.FileManager;
import com.dreambean.ejx.FileManagerFactory;
import org.jboss.logging.Logger;

/**
 *   <description>
 *
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *   @version $Revision: 1.13 $
 */
public class jBossFileManager
   extends BeanContextServicesSupport
   implements FileManager
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   XMLManager xm;
   File file;
   Component comp;

   jBossEjbJar ejbJar;

   jBossFileManagerFactory fact;

   ClassLoader cl;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   jBossFileManager(jBossFileManagerFactory f)
   {
      fact = f;
   }

   // Public --------------------------------------------------------
   public jBossEjbJar getEjbJar()
   {
      return ejbJar;
   }

	/*
	* load(URL file)
	*
	* This method creates the jBossEjbJar that encapsulates the MetaData for the container
	* if no proper jboss.xml and jaws.xml are found the system ones are used
	*
	*/
   public jBossEjbJar load(URL file)
      throws Exception
   {

      // Create classloader
      {
         URL fileUrl = file;
         if (fileUrl.toString().endsWith("ejb-jar.xml")) {

			// fileURL points to the top of the directory of the beans
            fileUrl = new File(fileUrl.getFile()).getParentFile().getParentFile().toURL();
	  	 }

		 // The classLoader has visibility on all the classes in this directory
         cl = new URLClassLoader(new URL[] { fileUrl }, Thread.currentThread().getContextClassLoader());
      }

      Document doc;

      ejbJar = new jBossEjbJar();
      add(ejbJar);

      // XML file
      if (file.getFile().endsWith("ejb-jar.xml"))
      {
         // Load EJB-JAR XML
         Reader in = new InputStreamReader(file.openStream());
         doc = xm.load(in);
         in.close();
         ejbJar.importXml(doc.getDocumentElement());

         // Load JBoss XML
         try
         {
            in = new BufferedReader(new InputStreamReader(new URL(file, "jboss.xml").openStream()));

            doc = xm.load(in);
            in.close();
            ejbJar.importXml(doc.getDocumentElement());
         } catch (IOException e)
         {
            // Couldn't find jboss.xml.. that's ok!
			// Load default JBoss XML
            InputStream jbossXml = getClass().getResourceAsStream("defaultjboss.xml");
            if (jbossXml == null)
            {
               // No default found
               return ejbJar;
            }
			in = new BufferedReader(new InputStreamReader(jbossXml));
            doc = xm.load(in);
            in.close();

            ejbJar.importXml(doc.getDocumentElement());

            return ejbJar;
         }

      } else if (file.getFile().endsWith(".jar")) // JAR file
      {
         // Load EJB-JAR XML
         InputStream ejbXml = getClassLoader().getResourceAsStream("META-INF/ejb-jar.xml");
         if (ejbXml == null)
         {
            // We want to use this file, but it doesn't contain the XML file yet (i.e. it's a JAR without the ejb-jar.xml file)
            return ejbJar;
         }
         Reader in = new BufferedReader(new InputStreamReader(ejbXml));
         doc = xm.load(in);
         in.close();

         ejbJar.importXml(doc.getDocumentElement());



         // Load JBoss XML
         InputStream jbossXml = getClassLoader().getResourceAsStream("META-INF/jboss.xml");
         if (jbossXml == null)
         {
            // We want to use this file, but it doesn't contain the XML file yet (i.e. it's a JAR without the jboss.xml file)
            // Load default JBoss XML
            jbossXml = getClass().getResourceAsStream("defaultjboss.xml");
            if (jbossXml == null)
            {
               // No default found
               return ejbJar;
            }
            in = new BufferedReader(new InputStreamReader(jbossXml));
            doc = xm.load(in);
            in.close();

            ejbJar.importXml(doc.getDocumentElement());
            return ejbJar;
         }
         in = new BufferedReader(new InputStreamReader(jbossXml));
         doc = xm.load(in);
         in.close();

         ejbJar.importXml(doc.getDocumentElement());
      } else
      {
         // Load from directory
         InputStream ejbXml = getClassLoader().getResourceAsStream("META-INF/ejb-jar.xml");
         Reader in = new BufferedReader(new InputStreamReader(ejbXml));
         doc = xm.load(in);
         ejbXml.close();
         ejbJar.importXml(doc.getDocumentElement());

         // Load JBoss XML
         InputStream jbossXml = getClassLoader().getResourceAsStream("META-INF/jboss.xml");
         if (jbossXml == null)
         {
            // We want to use this file, but it doesn't contain the XML file yet (i.e. it's a JAR without the jboss.xml file)
            // Load default JBoss XML
            jbossXml = getClass().getResourceAsStream("defaultjboss.xml");
            if (jbossXml == null)
            {
               // No default found
               return ejbJar;
            }

            in = new BufferedReader(new InputStreamReader(jbossXml));
            doc = xm.load(in);
            in.close();

            ejbJar.importXml(doc.getDocumentElement());
            return ejbJar;
         }
         in = new BufferedReader(new InputStreamReader(jbossXml));
         doc = xm.load(in);
         in.close();

         ejbJar.importXml(doc.getDocumentElement());
      }

      return ejbJar;
   }

   // FileManager implementation ------------------------------------
   public boolean isChanged()
   {
      return true;
   }

   public void createNew()
   {
      ejbJar = new jBossEjbJar();

      // Load default JBoss XML
      InputStream jbossXml = getClass().getResourceAsStream("defaultjboss.xml");
      if (jbossXml == null)
      {
         // No default found
         return;
      }

		try
		{
			Reader in = new BufferedReader(new InputStreamReader(jbossXml));
			Document doc = xm.load(in);
			in.close();

			ejbJar.importXml(doc.getDocumentElement());
		} catch (Exception e)
		{
			Logger.exception(e);;
		}

      add(ejbJar);
   }

   public void load(File file)
      throws Exception
   {
      setFile(file);

      // Copy to prevent locking by load if we want to save later on
      if (file.toString().endsWith(".jar"))
      {
         File tmp = File.createTempFile("ejbjar",".jar");
         tmp.deleteOnExit();
         FileInputStream fin = new FileInputStream(file);
         byte[] bytes = new byte[(int)file.length()];
         fin.read(bytes);
         FileOutputStream fout = new FileOutputStream(tmp);
         fout.write(bytes);
         fin.close();
         fout.close();
         file = tmp;
      }

      load(file.toURL());
   }

   public void save(File f)
      throws Exception
   {
      // Prepare XML document
      Document doc = xm.create();
      doc.appendChild(ejbJar.exportXml(doc));

      // Save to file
      if (f.toString().endsWith(".jar"))
      {
         // Save to existing file
         if (f.exists())
         {
            // Store XML
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Writer w = new OutputStreamWriter(out);
            xm.save(doc,w);
            w.close();
            byte[] arr = out.toByteArray();

            Util.insertFileIntoZip(f, "META-INF/jboss.xml", arr);
         } else
         {
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(f));

            zipOut.putNextEntry(new ZipEntry("META-INF/jboss.xml"));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Writer w = new OutputStreamWriter(out);
            xm.save(doc,w);
            w.close();
            byte[] arr = out.toByteArray();
            zipOut.write(arr);
            zipOut.closeEntry();
            zipOut.close();
         }

         setFile(f);
      }
      else if (f.toString().endsWith(".xml"))
      {
         FileWriter out = new FileWriter(new File(f.getParentFile(),"jboss.xml"));
         xm.save(doc,out);
         out.close();

         setFile(f);
      } else
      {
         // Check extension and add if possible
         String name = f.getName();
         if (name.indexOf(".") == -1)
         {
            name += ".xml";
            save(new File(f.getParent(),name));
         } else
         {
            JOptionPane.showMessageDialog(null, "Unknown filetype. File has not been saved!", "Save", JOptionPane.ERROR_MESSAGE);
         }
      }

   }

   public File getFile()
   {
      return file;
   }

   public void setFile(File f)
   {
      File old = file;
      file = f;
      firePropertyChange("File",old,file);
   }

   public FileManagerFactory getFactory() { return fact; }

   public Component getComponent()
   {
      if (comp == null)
      {
				comp = ejbJar.getComponent();
      }
      return comp;
   }

   public ClassLoader getClassLoader()
   {
      return cl;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
   protected void initializeBeanContextResources()
   {
      try
      {
         xm = (XMLManager)((BeanContextServices)getBeanContext()).getService(this,this,XMLManager.class,null,this);
      } catch (Exception e)
      {
         Logger.exception(e);;
      }
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
