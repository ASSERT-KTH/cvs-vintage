// $Id: ArgoFilePersister.java,v 1.4 2004/09/06 16:37:55 mvw Exp $
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.argouml.application.ArgoVersion;
import org.argouml.util.FileConstants;
import org.tigris.gef.ocl.ExpansionException;
import org.tigris.gef.ocl.OCLExpander;
import org.tigris.gef.ocl.TemplateReader;

/**
 * To persist to and from argo (xml file) storage.
 * 
 * @author Bob Tarling
 */
public class ArgoFilePersister extends AbstractFilePersister {
    
    private static final Logger LOG = 
        Logger.getLogger(ArgoFilePersister.class);
    
    /**
     * The constructor.
     * Sets the extensionname and the description. 
     */
    public ArgoFilePersister() {
        extension = "argo";
        desc = "Argo project file";
    }
    
    /**
     * It is being considered to save out individual
     * xmi's from individuals diagrams to make
     * it easier to modularize the output of Argo.
     * 
     * @param file The file to write.
     * @param project the project to save
     * @throws SaveException when anything goes wrong
     *
     * @see org.argouml.kernel.ProjectFilePersister#save(
     * org.argouml.kernel.Project, java.io.File)
     */
    public void save(Project project, File file)
        throws SaveException {
        
        project.setFile(file);
        project.setVersion(ArgoVersion.getVersion());
        project.setPersistenceVersion(PERSISTENCE_VERSION);


        // frank: first backup the existing file to name+"#"
        File tempFile = new File( file.getAbsolutePath() + "#");
        File backupFile = new File( file.getAbsolutePath() + "~");
        if (tempFile.exists()) {
            tempFile.delete();
        }
        
        Writer writer = null;
        try {
            if (file.exists()) {
                copyFile(tempFile, file);
            }
            // frank end
    
            FileOutputStream stream =
                new FileOutputStream(file);
            writer =
                new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        stream, "UTF-8")));
    
            expand(writer, project);
            writer.flush();
            
            stream.close();

            String path = file.getParent();
            if (LOG.isInfoEnabled()) {
                LOG.info("Dir ==" + path);
            }
            
            // if save did not raise an exception 
            // and name+"#" exists move name+"#" to name+"~"
            // this is the correct backup file
            if (backupFile.exists()) {
                backupFile.delete();
            }
            if (tempFile.exists() && !backupFile.exists()) {
                tempFile.renameTo(backupFile);
            }
            if (tempFile.exists()) {
                tempFile.delete();
            }
        } catch (Exception e) {
            LOG.error("Exception occured during save attempt", e);
            try {
                writer.close();
            } catch (IOException ex) { }
            
            // frank: in case of exception 
            // delete name and mv name+"#" back to name if name+"#" exists
            // this is the "rollback" to old file
            file.delete();
            tempFile.renameTo( file);
            // we have to give a message to user and set the system to unsaved!
            throw new SaveException(e);
        }

        try {
            writer.close();
        } catch (IOException ex) {
            LOG.error("Failed to close save output writer", ex);
        }
    }
    
    private void expand(Writer writer, Object project) throws SaveException {
        if (expander == null) {
            Hashtable templates = TemplateReader.readFile(ARGO2_TEE);
            expander = new OCLExpander(templates);
        }
        
        try {
            expander.expand(writer, project, "", "");
        } catch (ExpansionException e) {
            throw new SaveException(e);
        }
    }
    
    
    /**
     * @see org.argouml.kernel.ProjectFilePersister#loadProject(java.net.URL)
     */
    public Project loadProject(URL url) throws OpenException {
        throw new OpenException("Open argo has not yet been implemented");
    }
}
