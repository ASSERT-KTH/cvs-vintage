// $Id: PropPanelActor.java,v 1.35 2004/07/17 13:10:32 kataka Exp $
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

// File: PropPanelActor.java
// Classes: PropPanelActor
// Original Author: jrobbins@ics.uci.edu
// $Id: PropPanelActor.java,v 1.35 2004/07/17 13:10:32 kataka Exp $

// 21 Mar 2002: Jeremy Bennett (mail@jeremybennett.com). Changed to use the
// labels "Generalizes:" and "Specializes:" for inheritance.

// 4 Apr 2002: Jeremy Bennett (mail@jeremybennett.com). Labels corrected to
// "Generalizations:" and "Specializations". Problems with these two fields
// fixed. Display of dependencies dropped (why show for actors and not other
// classifiers). Tool tip changed to "Add actor"


package org.argouml.uml.ui.behavior.use_cases;

import org.argouml.i18n.Translator;
import org.argouml.model.uml.behavioralelements.usecases.UseCasesFactory;

import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.ui.PropPanelButton;
import org.argouml.uml.ui.UMLComboBoxNavigator;
import org.argouml.uml.ui.foundation.core.PropPanelClassifier;
import org.argouml.util.ConfigLoader;


/**
 * <p>Builds the property panel for a actor.</p>
 *
 * <p>This is a type of Classifier, and like other Classifiers can have
 *   attributes and operations (some processes use these to define
 *   requirements). <em>Note</em>. ArgoUML has no way to display these.</p>
 */

public class PropPanelActor extends PropPanelClassifier {


    ///////////////////////////////////////////////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * <p>Constructor. Builds up the various fields required.</p>
     */

    public PropPanelActor() {
    	super("Actor", ConfigLoader.getTabPropsOrientation());


    	addField(Translator.localize("UMLMenu", "label.name"), getNameTextField());
    	// addField(Translator.localize("UMLMenu", "label.stereotype"), new UMLComboBoxNavigator(this, Translator.localize("UMLMenu", "tooltip.nav-stereo"), getStereotypeBox()));
    	addField(Translator.localize("UMLMenu", "label.stereotype"), getStereotypeBox());

        addField(Translator.localize("UMLMenu", "label.modifiers"),
                 _modifiersPanel);

    	addField(Translator.localize("UMLMenu", "label.namespace"), getNamespaceComboBox());

    	addSeperator();

    	addField(Translator.localize("UMLMenu", "label.generalizations"), getGeneralizationScroll());
    	addField(Translator.localize("UMLMenu", "label.specializations"), getSpecializationScroll());

    	addSeperator();

    	addField(Translator.localize("UMLMenu", "label.association-ends"),
            getAssociationEndScroll());
        // The toolbar buttons that go at the top.

        new PropPanelButton(this, buttonPanel, _navUpIcon,
                Translator.localize("UMLMenu", "button.go-up"), "navigateUp",
                            null);
        new PropPanelButton(this, buttonPanel, _actorIcon,
                Translator.localize("UMLMenu", "button.new-actor"), "newActor",
                            null);
        new PropPanelButton(this, buttonPanel, _receptionIcon, Translator.localize("UMLMenu", "button.new-reception"), getActionNewReception());
        new PropPanelButton(this, buttonPanel, _deleteIcon,
                            localize("Delete"), "removeElement",
                            null);
    }


    /**
     * <p>Invoked by the "Add actor" toolbar button to create a new actor
     *   property panel in the same namespace as the current actor.</p>
     *
     * <p>This code uses getFactory and adds the actor explicitly to the
     *   namespace. Extended to actually navigate to the new actor.</p>
     */

    public void newActor() {
        TargetManager.getInstance().setTarget(UseCasesFactory.getFactory().buildActor(getTarget()));
    }



} /* end class PropActor */
