/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

import java.io.File;
import javax.swing.filechooser.FileFilter;

import com.dreambean.ejx.FileManager;
import com.dreambean.ejx.FileManagerFactory;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class jBossFileManagerFactory
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
		return new jBossFileManager(this);
	}
   
   public FileFilter getFileFilter()
	{
		return this;
	}
	
	public String toString()
	{
		return "jBoss XML";
	}
}
