// $Id: PropPanelParameter.java,v 1.41 2004/07/25 09:14:37 mkl Exp $
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
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,g
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.uml.ui.foundation.core;

import javax.swing.JList;
import javax.swing.JScrollPane;

import org.argouml.i18n.Translator;
import org.argouml.model.ModelFacade;
import org.argouml.model.uml.foundation.core.CoreFactory;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.ui.PropPanelButton;
import org.argouml.uml.ui.UMLComboBox2;
import org.argouml.uml.ui.UMLInitialValueComboBox;
import org.argouml.uml.ui.UMLList;
import org.argouml.uml.ui.UMLReflectionListModel;
import org.argouml.util.ConfigLoader;
/**
 * TODO: this property panel needs refactoring to remove dependency on
 *       old gui components.
 */
public class PropPanelParameter extends PropPanelModelElement {

    public PropPanelParameter() {
        super(
	      "Parameter",
	      _parameterIcon,
	      ConfigLoader.getTabPropsOrientation());
        Class mclass = (Class)ModelFacade.PARAMETER;

        Class[] namesToWatch = {
	    (Class)ModelFacade.STEREOTYPE,
	    (Class)ModelFacade.OPERATION,
	    (Class)ModelFacade.PARAMETER,
	    (Class)ModelFacade.CLASSIFIER 
	};
        setNameEventListening(namesToWatch);

        addField(Translator.localize("UMLMenu", "label.name"), getNameTextField());
        // addField(Translator.localize("UMLMenu", "label.stereotype"), new UMLComboBoxNavigator(this, Translator.localize("UMLMenu", "tooltip.nav-stereo"), getStereotypeBox()));
        addField(Translator.localize("UMLMenu", "label.stereotype"), getStereotypeBox());

        JList namespaceList = new UMLList(new UMLReflectionListModel(this, "behaviorialfeature", false, "getBehavioralFeature", null, null, null), true);
        namespaceList.setVisibleRowCount(1);
        addLinkField(Translator.localize("UMLMenu", "label.owner"), new JScrollPane(namespaceList, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        addSeperator();

        addField(Translator.localize("UMLMenu", "label.type"), new UMLComboBox2(new UMLParameterTypeComboBoxModel(), ActionSetParameterType.SINGLETON));

        addField("Initial Value:", new UMLInitialValueComboBox(this));
        
        //      TODO: i18n
        add(new UMLParameterDirectionKindRadioButtonPanel("ParameterKind:", true));

	new PropPanelButton(this, buttonPanel, _navUpIcon, Translator.localize("UMLMenu", "button.go-up"), "navigateUp", null);
	new PropPanelButton(this, buttonPanel, _parameterIcon, Translator.localize("UMLMenu", "button.new-parameter"), "addParameter", null);
	//	new PropPanelButton(this,buttonPanel,_dataTypeIcon, Translator.localize("UMLMenu", "button.new-datatype"),"addDataType",null);
	new PropPanelButton(this, buttonPanel, _deleteIcon, Translator.localize("UMLMenu", "button.delete-parameter"), "removeElement", null);

    }

    public Object getType() {
        Object target = getTarget();
        if (org.argouml.model.ModelFacade.isAParameter(target)) {
            return org.argouml.model.ModelFacade.getType(target);
        }
        return null;
    }

    public void setType(Object/*MClassifier*/ type) {
        Object target = getTarget();
        if (org.argouml.model.ModelFacade.isAParameter(target)) {
            ModelFacade.setType(target, type);
        }
    }

    public boolean isAcceptibleType(Object/*MModelElement*/ type) {
	return org.argouml.model.ModelFacade.isAClassifier(type);
    }

    public Object getBehavioralFeature() {
        Object feature = null;
        Object target = getTarget();
        if (ModelFacade.isAParameter(target)) {
            feature = ModelFacade.getBehavioralFeature(target);
        }
        return feature;
    }

    public void addDataType() {
        Object target = getTarget();
        if (ModelFacade.isANamespace(target)) {
            Object ns = /*(MNamespace)*/ target;
            Object ownedElem = CoreFactory.getFactory().createDataType();
            ModelFacade.addOwnedElement(ns, ownedElem);
            TargetManager.getInstance().setTarget(ownedElem);
        }
    }



    public void navigateUp() {
        Object feature = getBehavioralFeature();
        if (feature != null) {
            TargetManager.getInstance().setTarget(feature);
        }
    }

    public void addParameter() {
        Object feature = null;
        Object target = getTarget();
        if (ModelFacade.isAParameter(target)) {
            feature = ModelFacade.getBehavioralFeature(target);
            if (feature != null) {
                TargetManager.getInstance().setTarget(CoreFactory.getFactory().buildParameter(feature));
            }
        }
    }

    public void addDataType(Object/*MModelElement*/ element) {
        addDataType();
    }

} /* end class PropPanelParameter */