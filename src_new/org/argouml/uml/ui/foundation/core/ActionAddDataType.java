// $Id: ActionAddDataType.java,v 1.1 2004/12/20 23:15:10 mvw Exp $
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

package org.argouml.uml.ui.foundation.core;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.argouml.i18n.Translator;
import org.argouml.model.ModelFacade;
import org.argouml.model.uml.CoreFactory;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.ui.AbstractActionNewModelElement;


/**
 * This action creates a new datatype.
 * 
 * @author mvw@tigris.org
 */
public class ActionAddDataType extends AbstractActionNewModelElement {
    
    /**
     * The constructor.
     */
    public ActionAddDataType() {
        super("button.new-datatype");
        putValue(Action.NAME, Translator.localize("button.new-datatype"));
    }
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        Object target = TargetManager.getInstance().getModelTarget();
        Object ns = null;
        if (ModelFacade.isANamespace(target)) 
            ns = target; 
        if (ModelFacade.isAParameter(target))
            if (ModelFacade.getModelElementContainer(target) != null) 
                target = ModelFacade.getModelElementContainer(target);
        if (ModelFacade.isAFeature(target)) 
            if (ModelFacade.getOwner(target) != null) 
                target = ModelFacade.getOwner(target); 
        if (ModelFacade.isAEvent(target)) 
            ns = ModelFacade.getNamespace(target);
        if (ModelFacade.isAClassifier(target)) 
            ns = ModelFacade.getNamespace(target);
        
        Object newDt = CoreFactory.getFactory().buildDataType("", ns);
        TargetManager.getInstance().setTarget(newDt);
        super.actionPerformed(e);
    }
}