// $Id: UMLFeatureOwnerScopeCheckBox.java,v 1.5 2003/09/28 19:10:53 bobtarling Exp $
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

// $header$
package org.argouml.uml.ui.foundation.core;

import org.argouml.model.ModelFacade;
import org.argouml.application.api.Argo;
import org.argouml.uml.ui.UMLCheckBox2;

//import ru.novosoft.uml.foundation.data_types.MScopeKind;

/**
 * @since Nov 6, 2002
 * @author jaap.branderhorst@xs4all.nl
 */
public class UMLFeatureOwnerScopeCheckBox extends UMLCheckBox2 {

    /**
     * Constructor for UMLFeatureOwnerScopeCheckBox.
     * @param container
     * @param text
     * @param a
     */
    public UMLFeatureOwnerScopeCheckBox() {
        super(Argo.localize("UMLMenu", "static"), ActionSetFeatureOwnerScope.SINGLETON, "ownerScope");
    }

    /**
     * @see org.argouml.uml.ui.UMLCheckBox2#buildModel()
     */
    public void buildModel() {
        Object scope = ModelFacade.getOwnerScope(getTarget());
        if (scope != null && scope.equals(ModelFacade.CLASSIFIER_SCOPEKIND)) {
            setSelected(true);
        } else
            setSelected(false);
    }

}