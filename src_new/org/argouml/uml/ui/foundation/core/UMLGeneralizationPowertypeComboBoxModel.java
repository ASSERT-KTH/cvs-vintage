// $Id: UMLGeneralizationPowertypeComboBoxModel.java,v 1.9 2003/09/21 14:11:12 bobtarling Exp $
// Copyright (c) 1996-2002 The Regents of the University of California. All
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

// $header$
package org.argouml.uml.ui.foundation.core;

import org.argouml.model.ModelFacade;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.uml.UmlModelEventPump;
import org.argouml.model.uml.modelmanagement.ModelManagementHelper;
import org.argouml.uml.ui.UMLComboBoxModel2;

/**
 * @since Nov 3, 2002
 * @author jaap.branderhorst@xs4all.nl
 */
public class UMLGeneralizationPowertypeComboBoxModel
    extends UMLComboBoxModel2 {

    /**
     * Constructor for UMLGeneralizationPowertypeComboBoxModel.
     * @param container
     */
    public UMLGeneralizationPowertypeComboBoxModel() {
        super("powertype", true);
        UmlModelEventPump.getPump().addClassModelEventListener(this, (Class)ModelFacade.NAMESPACE, "ownedElement");
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#getSelectedModelElement()
     */
    protected Object getSelectedModelElement() {
        if (getTarget() != null) {
            return ModelFacade.getPowertype(getTarget());
        }
        return null;
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#buildModelList()
     */
    protected void buildModelList() {
        Set elements = new HashSet();
        Project p = ProjectManager.getManager().getCurrentProject();
        Iterator it = p.getUserDefinedModels().iterator();
        while (it.hasNext()) {
	    Object model = /*(MModel)*/ it.next();
	    elements.addAll(ModelManagementHelper.getHelper().getAllModelElementsOfKind(model, (Class)ModelFacade.CLASSIFIER));
        }
        elements.addAll(ModelManagementHelper.getHelper().getAllModelElementsOfKind(p.getDefaultModel(), (Class)ModelFacade.CLASSIFIER));
        setElements(elements);
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#isValidElement(ru.novosoft.uml.MBase)
     */
    protected boolean isValidElement(Object element) {
        return org.argouml.model.ModelFacade.isAClassifier(element);
    }

}