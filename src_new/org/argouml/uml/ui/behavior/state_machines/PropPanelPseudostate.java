// $Id: PropPanelPseudostate.java,v 1.22 2004/07/02 16:15:19 mvw Exp $
// Copyright (c) 1996-99 The Regents of the University of California. All
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

// File: PropPanelPseudostate.java
// Classes: PropPanelPseudostate
// Original Author: your email address here
// $Id: PropPanelPseudostate.java,v 1.22 2004/07/02 16:15:19 mvw Exp $

package org.argouml.uml.ui.behavior.state_machines;

import org.argouml.i18n.Translator;
import org.argouml.model.ModelFacade;
import org.argouml.uml.ui.UMLComboBoxNavigator;
import org.argouml.util.ConfigLoader;
import org.tigris.gef.presentation.Fig;

/**
 * Property Panbel for the collection of pseudostates (branch, fork, ...). It
 * dynamically sets its name to the pseudostate used.
 */
public class PropPanelPseudostate extends PropPanelStateVertex {

    public PropPanelPseudostate() {
        super("Pseudostate", null, ConfigLoader.getTabPropsOrientation());

        Class mclass = (Class) ModelFacade.PSEUDOSTATE;

        addField(Translator.localize("UMLMenu", "label.name"),
                getNameTextField());
        addField(Translator.localize("UMLMenu", "label.stereotype"),
                new UMLComboBoxNavigator(this, Translator.localize("UMLMenu",
                        "tooltip.nav-stereo"), getStereotypeBox()));
        addField(Translator.localize("UMLMenu", "label.container"),
                containerScroll);

        addSeperator();

        addField(Translator.localize("UMLMenu", "label.incoming"),
                incomingScroll);
        addField(Translator.localize("UMLMenu", "label.outgoing"),
                outgoingScroll);

    }

    /**
     * This method is responsible for setting the title of the proppanel
     * according to the type of the pseudo state displayed in the property
     * panel. This is required as pseudostates share a common class and are
     * distinguished only by an attribute (pseudostatekind).
     * 
     * @param target
     *            the current target
     */
    public void setTarget(Object target) {
        super.setTarget(target);

        Object o = ((target instanceof Fig) ? ((Fig) target).getOwner()
                : target);
        if (ModelFacade.isAPseudostate(o)) {
            Object kind = ModelFacade.getPseudostateKind(o);
            if (ModelFacade.equalsPseudostateKind(kind,
                    ModelFacade.FORK_PSEUDOSTATEKIND))
                    getTitleLabel().setText("Fork State");
            if (ModelFacade.equalsPseudostateKind(kind,
                    ModelFacade.JOIN_PSEUDOSTATEKIND))
                    getTitleLabel().setText("Join State");
            if (ModelFacade.equalsPseudostateKind(kind,
                    ModelFacade.BRANCH_PSEUDOSTATEKIND))
                    /* TODO: This shall be changed into "Choice State" 
                     * for the 0.17.1 release */
                    getTitleLabel().setText("Branch State");
            if (ModelFacade.equalsPseudostateKind(kind,
                    ModelFacade.DEEPHISTORY_PSEUDOSTATEKIND))
                    getTitleLabel().setText("Deep History State");
            if (ModelFacade.equalsPseudostateKind(kind,
                    ModelFacade.SHALLOWHISTORY_PSEUDOSTATEKIND))
                    getTitleLabel().setText("Shallow History State");
            if (ModelFacade.equalsPseudostateKind(kind,
                    ModelFacade.INITIAL_PSEUDOSTATEKIND))
                    getTitleLabel().setText("Initial State");
            if (ModelFacade.equalsPseudostateKind(kind,
                    ModelFacade.JUNCTION_PSEUDOSTATEKIND))
                    getTitleLabel().setText("Junction State");
        }

    }
} /* end class PropPanelPseudostate */