// $Id: PropPanelLink.java,v 1.28 2004/07/22 10:58:49 mkl Exp $
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



// File: PropPanelLink.java
// Classes: PropPanelLink
// Original Author: jrobbins@ics.uci.edu
// $Id: PropPanelLink.java,v 1.28 2004/07/22 10:58:49 mkl Exp $

package org.argouml.uml.ui.behavior.common_behavior;


import javax.swing.JList;
import javax.swing.JScrollPane;

import org.argouml.i18n.Translator;
import org.argouml.model.ModelFacade;
import org.argouml.model.uml.UmlFactory;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.ui.PropPanelButton;
import org.argouml.uml.ui.UMLLinkedList;
import org.argouml.uml.ui.foundation.core.PropPanelModelElement;
import org.argouml.util.ConfigLoader;

public class PropPanelLink extends PropPanelModelElement {


    ////////////////////////////////////////////////////////////////
    // contructors
    public PropPanelLink() {
        super("Link", _linkIcon, ConfigLoader.getTabPropsOrientation());

        Class mclass = (Class)ModelFacade.LINK;
        addField(Translator.localize("UMLMenu", "label.name"), getNameTextField());
        addField(Translator.localize("UMLMenu", "label.stereotype"), getStereotypeBox());
        addLinkField(Translator.localize("UMLMenu", "label.namespace"), getNamespaceComboBox());
        addSeperator();
        
        JList connectionList = new UMLLinkedList(new UMLLinkConnectionListModel());
        JScrollPane connectionScroll = new JScrollPane(connectionList);
        addLinkField(Translator.localize("UMLMenu", "label.connections"), connectionScroll);

        new PropPanelButton(this, buttonPanel, _navUpIcon, Translator.localize("UMLMenu", "button.go-up"), "navigateNamespace", null);
        new PropPanelButton(this, buttonPanel, _deleteIcon, localize("Delete object"), "removeElement", null);
    }

    public void navigateNamespace() {
        Object target = getTarget();
        if (org.argouml.model.ModelFacade.isAModelElement(target)) {
            Object elem = /*(MModelElement)*/ target;
            Object ns = ModelFacade.getNamespace(elem);
            if (ns != null) {
                TargetManager.getInstance().setTarget(ns);
            }
        }
    }

    public void removeElement() {
	Object target = /*(MLink)*/ getTarget();
	Object newTarget = /*(MModelElement)*/ ModelFacade.getNamespace(target);

        UmlFactory.getFactory().delete(target);
	if (newTarget != null) TargetManager.getInstance().setTarget(newTarget);
    }
} /* end class PropPanelLink */
