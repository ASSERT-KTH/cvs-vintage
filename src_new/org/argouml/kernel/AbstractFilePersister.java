// $Id: AbstractFilePersister.java,v 1.8 2004/09/06 20:20:31 mvw Exp $
// Copyright (c) 1996-2004 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.tigris.gef.ocl.OCLExpander;
import org.tigris.gef.ocl.TemplateReader;

/**
 * To persist to and from zargo (zipped file) storage.
 * 
 * @author Bob Tarling
 */
public abstract class AbstractFilePersister extends FileFilter 
    implements ProjectFilePersister {
    
    private static final Logger LOG = 
        Logger.getLogger(AbstractFilePersister.class);
    
    private static final String ARGO_TEE = "/org/argouml/xml/dtd/argo.tee";
    private static final String ARGO2_TEE = "/org/argouml/xml/dtd/argo2.tee";

    /**
     * The PERSISTENCE_VERSION is increased every time a new version of 
     * ArgoUML is released, which uses an forwards incompatible file format.
     * This allows conversion of old persistence version files 
     * to be converted to the current one.
     */
    protected static final int PERSISTENCE_VERSION = 2;
    
    /**
     * This is used in the save process for PGML.
     */
    private static OCLExpander expander;
    
    /**
     * Copies one file src to another, raising file exceptions
     * if there are some problems.
     * 
     * @param dest The destination file.
     * @param src The source file.
     * @return The destination file after successful copying.
     * @throws IOException if there is some problems with the files.
     * @throws FileNotFoundException if any of the files cannot be found.
     */
    protected File copyFile(File dest, File src)
        throws FileNotFoundException, IOException {
        
        // first delete dest file
        if (dest.exists()) {
            dest.delete();
        }

        FileInputStream fis  = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(dest);
        byte[] buf = new byte[1024];
        int i = 0;
        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
        
        dest.setLastModified(src.lastModified());
        
        return dest;
    }
    
    
    
    ////////////////////////////////////////////////////////////////
    // FileFilter API

    /**
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f) {
        if (f == null) return false;
        if (f.isDirectory()) return true;
        String s = getExtension(f); 
        if (s != null) // this check for files without extension... 
            if (s.equalsIgnoreCase(getExtension())) return true;
        return false;
    }
    
    /**
     * The extension valid for this type of file.
     * (Just the chars, not the dot: e.g. "zargo".)
     *
     * @return the extension valid for this type of file
     */
    public abstract String getExtension();

    /**
     * (Just the description, not the extension between "()".)
     * 
     * @return the description valid for this type of file
     */
    protected abstract String getDesc();
    
    private static String getExtension(File f) {
        if (f == null) return null;
        return getExtension(f.getName());
    }

    private static String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1) {
            return filename.substring(i + 1).toLowerCase();
        }
        return null;
    }

    /**
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription() {
        return getDesc() + " (*." + getExtension() + ")";
    }
    
    /**
     * @param t the hashtable made with the Templatereader
     *         for a ArgoUML TEE file
     * @return the expander
     */
    public OCLExpander getExpander(Hashtable t) {
        if (expander == null) {
            expander = new OCLExpander(t);
        }
        return expander;
    }
    
    /**
     * @return the hashtable made with the Templatereader
     *         for the ArgoUML TEE file
     */
    protected Hashtable getArgoTeeTemplate() {
        return TemplateReader.readFile(ARGO_TEE);
    }

    /**
     * @return the hashtable made with the Templatereader
     *         for the ArgoUML TEE_2 file
     */
    protected Hashtable getArgoTee2Template() {
        return TemplateReader.readFile(ARGO2_TEE);
    }
}
