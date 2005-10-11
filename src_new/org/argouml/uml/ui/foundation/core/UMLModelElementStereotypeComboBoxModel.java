// $Id: UMLModelElementStereotypeComboBoxModel.java,v 1.32 2005/10/11 21:23:16 rastaman Exp $
// Copyright (c) 1996-2005 The Regents of the University of California. All
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

package org.argouml.uml.ui.foundation.core;

import java.util.Collection;

import org.argouml.kernel.ProjectManager;
import org.argouml.model.Model;
import org.argouml.uml.ui.UMLComboBoxModel2;
import org.argouml.util.CollectionUtil;

/**
 * @since Oct 10, 2002
 * @author jaap.branderhorst@xs4all.nl
 */
public class UMLModelElementStereotypeComboBoxModel extends UMLComboBoxModel2 {

    /**
     * Constructor for UMLModelElementStereotypeComboBoxModel.
     */
    public UMLModelElementStereotypeComboBoxModel() {
        super("stereotype", true);
        Model.getPump().addClassModelEventListener(
            this,
            Model.getMetaTypes().getNamespace(),
            "ownedElement");
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#isValidElement(Object)
     */
    protected boolean isValidElement(Object o) {
        return Model.getFacade().isAStereotype(o)
            && Model.getExtensionMechanismsHelper().isValidStereoType(
		   /*(MModelElement)*/ getTarget(), /*(MStereotype)*/ o);
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#buildModelList()
     */
    protected void buildModelList() {
        Object elem = /*(MModelElement)*/ getTarget();
        Collection models =
            ProjectManager.getManager().getCurrentProject().getModels();
        setElements(Model.getExtensionMechanismsHelper()
	        .getAllPossibleStereotypes(models, elem));
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#getSelectedModelElement()
     */
    protected Object getSelectedModelElement() {
        if (getTarget() != null) {
            Object stereo = null;
            if (Model.getFacade().isATagDefinition(getTarget())) {
                stereo = Model.getFacade().getOwner(getTarget());
            } else {
                stereo = CollectionUtil.getFirstItemOrNull(
                        Model.getFacade().getStereotypes(getTarget()));
            }
            return stereo;                
        }
        return null;
    }

}
