// $Id: UMLReceptionSignalComboBoxModel.java,v 1.12 2003/09/21 14:11:13 bobtarling Exp $
// Copyright (c) 1996-99 The Regents of the University of California. All
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

package org.argouml.uml.ui.behavior.common_behavior;

import org.argouml.model.ModelFacade;
import org.argouml.model.uml.UmlModelEventPump;
import org.argouml.model.uml.modelmanagement.ModelManagementHelper;
import org.argouml.uml.ui.UMLComboBoxModel2;

/**
 * The model for the signal combobox on the reception proppanel.
 */
public class UMLReceptionSignalComboBoxModel extends UMLComboBoxModel2 {

    /**
     * Constructor for UMLReceptionSignalComboBoxModel.
     * @param container
     */
    public UMLReceptionSignalComboBoxModel() {
        super("signal", false);
        UmlModelEventPump.getPump().addClassModelEventListener(this, (Class)ModelFacade.NAMESPACE, "ownedElement");
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#buildModelList()
     */
    protected void buildModelList() {
        Object target = getTarget();
        if (org.argouml.model.ModelFacade.isAReception(target)) {
            Object rec = /*(MReception)*/ target;
            removeAllElements();
            setElements(ModelManagementHelper.getHelper().getAllModelElementsOfKind((Class)ModelFacade.SIGNAL));
            setSelectedItem(ModelFacade.getSignal(rec));      
        }
         
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#isValidElement(ru.novosoft.uml.MBase)
     */
    protected boolean isValidElement(Object m) {
        return ModelFacade.isASignal(m);
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#getSelectedModelElement()
     */
    protected Object getSelectedModelElement() {
        if (getTarget() != null) {
            return ModelFacade.getSignal(getTarget());
        }
        return null;
    }

}