/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws.deployment;

import java.io.File;
import javax.swing.filechooser.FileFilter;

import com.dreambean.ejx.FileManager;
import com.dreambean.ejx.FileManagerFactory;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.3 $
 */
public class JawsFileManagerFactory
	extends FileFilter
	implements FileManagerFactory
{
   // Constants -----------------------------------------------------
    
   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   // FileFilter implementation -------------------------------------
	public boolean accept(File f)
	{
		return (f.getName().equals("ejb-jar.xml") || f.getName().endsWith(".jar"))
					|| f.isDirectory();
	}
	
	public String getDescription() { return toString(); }

   // FileManagerFactory implementation -----------------------------
   public FileManager createFileManager()
	{
		return new JawsFileManager(this);
	}
   
   public FileFilter getFileFilter()
	{
		return this;
	}
	
	public String toString()
	{
		return "JAWS XML";
	}
}
