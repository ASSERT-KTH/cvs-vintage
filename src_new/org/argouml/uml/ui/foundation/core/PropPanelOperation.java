// $Id: PropPanelOperation.java,v 1.72 2004/12/20 23:15:10 mvw Exp $
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

package org.argouml.uml.ui.foundation.core;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.argouml.i18n.Translator;
import org.argouml.model.ModelFacade;
import org.argouml.model.uml.UmlFactory;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.diagram.ui.ActionAddOperation;
import org.argouml.uml.ui.AbstractActionNewModelElement;
import org.argouml.uml.ui.ActionNavigateOwner;
import org.argouml.uml.ui.ActionRemoveFromModel;
import org.argouml.uml.ui.PropPanelButton2;
import org.argouml.uml.ui.UMLLinkedList;
import org.argouml.uml.ui.foundation.extension_mechanisms.ActionNewStereotype;
import org.argouml.util.ConfigLoader;
import org.tigris.swidgets.GridLayout2;

/**
 * A property panel for operations. 
 */
public class PropPanelOperation extends PropPanelFeature {

    ////////////////////////////////////////////////////////////////
    // contructors
    /**
     * The constructor.
     */
    public PropPanelOperation() {
        super("Operation", lookupIcon("Operation"), ConfigLoader
                .getTabPropsOrientation());

        /* This will cause the components on this page to be notified
         * anytime a stereotype, namespace or classifier
         * has its name, ownedElement or baseClass changed 
         * anywhere in the model. */
//        Class[] namesToWatch = {(Class) ModelFacade.STEREOTYPE,
//            (Class) ModelFacade.NAMESPACE, (Class) ModelFacade.CLASSIFIER,
//            (Class) ModelFacade.PARAMETER};
//        setNameEventListening(namesToWatch);

        addField(Translator.localize("label.name"),
                getNameTextField());

        addField(Translator.localize("label.stereotype"),
                getStereotypeBox());

        addField(Translator.localize("label.owner"),
                getOwnerScroll());

        addSeperator();    

        add(getVisibilityPanel());

        JPanel modifiersPanel = new JPanel(new GridLayout2(0, 3,
                GridLayout2.ROWCOLPREFERRED));
        modifiersPanel.setBorder(new TitledBorder(Translator.localize(
                "label.modifiers")));
        modifiersPanel.add(new UMLGeneralizableElementAbstractCheckBox());
        modifiersPanel.add(new UMLGeneralizableElementLeafCheckBox());
        modifiersPanel.add(new UMLGeneralizableElementRootCheckBox());
        modifiersPanel.add(new UMLBehavioralFeatureQueryCheckBox());
        modifiersPanel.add(new UMLFeatureOwnerScopeCheckBox());
        add(modifiersPanel);

        add(new UMLOperationConcurrencyRadioButtonPanel(
                Translator.localize("label.concurrency"), true));

        addSeperator();

        addField(Translator.localize("label.parameters"),
                new JScrollPane(new UMLLinkedList(
                new UMLClassifierParameterListModel())));

        addField(Translator.localize("label.raisedsignals"),
               new JScrollPane(new UMLLinkedList(
               new UMLOperationRaisedSignalsListModel())));
        
        addButton(new PropPanelButton2(new ActionNavigateOwner()));
        addButton(new PropPanelButton2(new ActionAddOperation()));
        addButton(new PropPanelButton2(new ActionNewParameter()));
        addButton(new PropPanelButton2(new ActionNewRaisedSignal(), 
                lookupIcon("SignalSending")));
        addButton(new PropPanelButton2(new ActionAddDataType(), 
                lookupIcon("DataType")));
        addButton(new PropPanelButton2(new ActionNewStereotype(), 
                lookupIcon("Stereotype")));
        addButton(new PropPanelButton2(new ActionRemoveFromModel(), 
                lookupIcon("Delete")));
    }

    /**
     * @param index add a raised signal
     */
    public void addRaisedSignal(Integer index) {
        Object target = getTarget();
        if (ModelFacade.isAOperation(target)) {
            Object oper = /* (MOperation) */target;
            Object newSignal = UmlFactory.getFactory().getCommonBehavior()
                    .createSignal(); 
                    //((MOperation)oper).getFactory().createSignal();
            
            ModelFacade.addOwnedElement(ModelFacade.getNamespace(ModelFacade
                    .getOwner(oper)), newSignal);
            ModelFacade.addRaisedSignal(oper, newSignal);
            TargetManager.getInstance().setTarget(newSignal);
        }
    }

    /**
     * The button to add a raised signal is pressed.
     */
    public void buttonAddRaisedSignal() {
        Object target = getTarget();
        if (org.argouml.model.ModelFacade.isAOperation(target)) {
            addRaisedSignal(new Integer(1));
        }
    }

    private class ActionNewRaisedSignal extends AbstractActionNewModelElement {

        /**
         * The constructor.
         */
        public ActionNewRaisedSignal() {
            super("button.new-raised-signal");
            putValue(Action.NAME, 
                    Translator.localize("button.new-raised-signal"));
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            Object target = TargetManager.getInstance().getModelTarget();
            if (org.argouml.model.ModelFacade.isAOperation(target)) {
                addRaisedSignal(new Integer(1));
                super.actionPerformed(e);
            }
        }
    }

    
    /**
     * Appropriate namespace is the namespace of our class, not the class itself
     *
     * @see org.argouml.uml.ui.PropPanel#getDisplayNamespace()
     */
    protected Object getDisplayNamespace() {
        Object namespace = null;
        Object target = getTarget();
        if (ModelFacade.isAAttribute(target)) {
            if (ModelFacade.getOwner(target) != null) {
                namespace = ModelFacade.getNamespace(ModelFacade
                        .getOwner(target));
            }
        }
        return namespace;
    }

} /* end class PropPanelOperation */
