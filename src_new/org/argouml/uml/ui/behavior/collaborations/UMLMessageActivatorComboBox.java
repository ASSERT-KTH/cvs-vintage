// $Id: UMLMessageActivatorComboBox.java,v 1.6 2004/09/22 17:55:57 mvw Exp $
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

package org.argouml.uml.ui.behavior.collaborations;

import java.awt.event.ActionEvent;
import org.argouml.model.ModelFacade;

import org.argouml.model.uml.behavioralelements.collaborations.CollaborationsHelper;
import org.argouml.uml.ui.UMLComboBox2;
import org.argouml.uml.ui.UMLComboBoxModel2;
import org.argouml.uml.ui.UMLUserInterfaceContainer;
/**
 * The combobox for activators on the message proppanel. The only reason this 
 * combobox implements melementlistener is to conform to UMLChangeDispatch. The 
 * combobox serves as a proxy for the 
 * model (UMLMessageActivatorComboBoxModel). Kind of strange...
 */
public class UMLMessageActivatorComboBox extends UMLComboBox2 {

    /**
     * Constructor for UMLMessageActivatorComboBox.
     * @param container the UI container
     * @param arg0 the model
     */
    public UMLMessageActivatorComboBox(
        UMLUserInterfaceContainer container,
        UMLComboBoxModel2 arg0) {
        super(arg0);
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBox2#doIt(ActionEvent)
     */
    protected void doIt(ActionEvent event) {
        Object o = getModel().getElementAt(getSelectedIndex());
        Object activator = /*(MMessage)*/ o;
        Object mes = /*(MMessage)*/ getTarget();
        if (activator != ModelFacade.getActivator(mes)) {
            CollaborationsHelper.getHelper().setActivator(mes, activator);
        }
    }

}