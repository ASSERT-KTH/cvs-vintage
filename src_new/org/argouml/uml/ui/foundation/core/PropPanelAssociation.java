// $Id: PropPanelAssociation.java,v 1.37 2004/07/31 08:31:58 mkl Exp $
// Copyright (c) 1996-2002 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
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

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.argouml.i18n.Translator;
import org.argouml.swingext.GridLayout2;
import org.argouml.swingext.Orientation;
import org.argouml.uml.ui.ActionNavigateContainerElement;
import org.argouml.uml.ui.ActionRemoveFromModel;
import org.argouml.uml.ui.PropPanelButton2;
import org.argouml.uml.ui.UMLLinkedList;
import org.argouml.util.ConfigLoader;

public class PropPanelAssociation extends PropPanelRelationship {

    /**
     * The scrollpane with the associationends.
     */
    protected JScrollPane _assocEndScroll;

    /**
     * The scrollpane with the associationroles this association plays a role
     * in.
     */
    protected JScrollPane _associationRoleScroll;

    /**
     * Ths scrollpane with the links that implement this association.
     */
    protected JScrollPane _linksScroll;

    /**
     * Panel for abstract/leaf/root
     */
    protected JPanel _modifiersPanel;

    public PropPanelAssociation() {
        this("Association", ConfigLoader.getTabPropsOrientation());
        addField(Translator.localize("UMLMenu", "label.name"),
                getNameTextField());
        // addField(Translator.localize("UMLMenu", "label.stereotype"), new
        // UMLComboBoxNavigator(this, Translator.localize("UMLMenu",
        // "tooltip.nav-stereo"), getStereotypeBox()));
        addField(Translator.localize("UMLMenu", "label.stereotype"),
                getStereotypeBox());
        addField(Translator.localize("UMLMenu", "label.namespace"),
                getNamespaceComboBox());
        add(_modifiersPanel);

        addSeperator();

        addField(Translator.localize("UMLMenu", "label.association-ends"),
                _assocEndScroll);

        addSeperator();

        addField(Translator.localize("UMLMenu", "label.association-roles"),
                _associationRoleScroll);
        addField(Translator.localize("UMLMenu", "label.association-links"),
                _linksScroll);

        buttonPanel.add(new PropPanelButton2(this,
                new ActionNavigateContainerElement()));
        buttonPanel
                .add(new PropPanelButton2(this, new ActionRemoveFromModel()));

    }

    protected PropPanelAssociation(String title, Orientation orientation) {
        super(title, orientation);
        initialize();
        JList assocEndList = new UMLLinkedList(
                new UMLAssociationConnectionListModel());
        _assocEndScroll = new JScrollPane(assocEndList);
        JList baseList = new UMLLinkedList(
                new UMLAssociationAssociationRoleListModel());
        _associationRoleScroll = new JScrollPane(baseList);
        JList linkList = new UMLLinkedList(new UMLAssociationLinkListModel());
        _linksScroll = new JScrollPane(linkList);

        // TODO: implement the multiple inheritance of an Association
        // (Generalizable element)

    }

    private void initialize() {

        _modifiersPanel = new JPanel(new GridLayout2());
        _modifiersPanel.setBorder(new TitledBorder(Translator.localize(
                "UMLMenu", "label.modifiers")));
        _modifiersPanel.add(new UMLGeneralizableElementAbstractCheckBox());
        _modifiersPanel.add(new UMLGeneralizableElementLeafCheckBox());
        _modifiersPanel.add(new UMLGeneralizableElementRootCheckBox());

    }

    /**
     * Adds an associationend to the association.
     */
    protected void addAssociationEnd() {
        // TODO implement this method as soon as issue 1703 is answered.
        throw new UnsupportedOperationException(
                "addAssociationEnd is not yet implemented");
    }

} /* end class PropPanelAssociation */
