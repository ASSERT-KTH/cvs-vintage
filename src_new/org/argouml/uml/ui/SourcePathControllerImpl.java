// $Id: SourcePathControllerImpl.java,v 1.2 2004/11/23 19:29:33 mvw Exp $
// Copyright (c) 2004 The Regents of the University of California. All
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

package org.argouml.uml.ui;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import org.argouml.model.ModelFacade;
import org.argouml.model.uml.ModelManagementHelper;

/**
 * Implements the source path controller. 
 * NOTE: If requested in the future this could be returned from the language 
 * modules.
 *
 * @author euluis
 * @since 0.17.1
 */
public class SourcePathControllerImpl implements SourcePathController {
    
    /** 
     * The string used to store source path string as tagged value.
     * [Shouldn't this be in ModelFacade?]
     */
    private static final String SRC_PATH_TAG = "src_path";
    
    /**
     * @see org.argouml.uml.ui.SourcePathController#getSourcePath(java.lang.Object)
     */
    public File getSourcePath(Object modelElement) {
        Object tv = ModelFacade.getTaggedValue(modelElement, SRC_PATH_TAG);
        if (tv != null) {
            String srcPath = ModelFacade.getValueOfTag(tv);
            if (srcPath != null) {
                return new File(srcPath);
            }
        }
        return null;
    }
    
    /**
     * @see org.argouml.uml.ui.SourcePathController#getSourcePathSettings()
     */
    public SourcePathTableModel getSourcePathSettings() {
        return new SourcePathTableModel(this);
    }
    
    /**
     * @see org.argouml.uml.ui.SourcePathController#setSourcePath(org.argouml.uml.ui.SourcePathTableModel)
     */
    public void setSourcePath(SourcePathTableModel srcPaths) {
        for (int i = 0; i < srcPaths.getRowCount(); i++) {
            setSourcePath(srcPaths.getModelElement(i), 
                new File(srcPaths.getMESourcePath(i)));
        }
    }
    
    /**
     * @see org.argouml.uml.ui.SourcePathController#setSourcePath(
     * java.lang.Object, java.io.File)
     */
    public void setSourcePath(Object modelElement, File sourcePath) {
        ModelFacade.setTaggedValue(modelElement, SRC_PATH_TAG, 
            sourcePath.toString());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "ArgoUML default source path controller.";
    }
    
    /**
     * @see org.argouml.uml.ui.SourcePathController#deleteSourcePath(java.lang.Object)
     */
    public void deleteSourcePath(Object modelElement) {
        ModelFacade.removeTaggedValue(modelElement, SRC_PATH_TAG);
    }
    
    /**
     * @see org.argouml.uml.ui.SourcePathController#getAllModelElementsWithSourcePath()
     */
    public Collection getAllModelElementsWithSourcePath() {
        Collection elems =
            ModelManagementHelper.getHelper().getAllModelElementsOfKind(
                (Class) ModelFacade.MODELELEMENT);
        
        ArrayList mElemsWithSrcPath = new ArrayList();
        
        Iterator iter = elems.iterator();
        while (iter.hasNext()) {
            Object me = iter.next();
            if (getSourcePath(me) != null) {
                mElemsWithSrcPath.add(me);
            }
        }
        return mElemsWithSrcPath;
    }
    
} /* end of SourcePathControllerImpl class definition */
