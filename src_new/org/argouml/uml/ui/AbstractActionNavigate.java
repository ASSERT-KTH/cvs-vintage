// $Id: AbstractActionNavigate.java,v 1.1 2004/07/26 22:33:41 mkl Exp $
// Copyright (c) 2003-2004 The Regents of the University of California. All
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

package org.argouml.uml.ui;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;

import org.argouml.application.helpers.ResourceLoaderWrapper;
import org.argouml.i18n.Translator;
import org.argouml.model.ModelFacade;
import org.argouml.ui.targetmanager.TargetManager;

/**
 * 
 * @author mkl
 *  
 */
public abstract class AbstractActionNavigate extends UMLAction {

    public AbstractActionNavigate() {
        this(Translator.localize(
                "UMLMenu", "button.go-up"), HAS_ICON);
    }

    /**
     * @param name
     * @param hasIcon
     */
    public AbstractActionNavigate(String name, boolean hasIcon) {
        super(name, hasIcon);
        putValue(Action.SMALL_ICON, ResourceLoaderWrapper.lookupIconResource("NavigateUp"));
    }
    
    public AbstractActionNavigate setIcon(Icon newIcon) {
        putValue(Action.SMALL_ICON, newIcon);        
        return this;
    }
    /**
     * Abstract method to do the navigation. The actual navigation is performed
     * by actionPerformed.
     * 
     * @param source
     *            the object to navigate from
     * @return the object to navigate to
     */
    protected abstract Object navigateTo(Object source);

    /** Perform the work the action is supposed to do. */
    public void actionPerformed(ActionEvent e) {
        Object target = TargetManager.getInstance().getTarget();
        if (ModelFacade.isAModelElement(target)) {
            Object elem = /* (MModelElement) */target;
            Object nav = navigateTo(elem);
            if (nav != null) {
                TargetManager.getInstance().setTarget(nav);
            }
        }
    }
}