// $Id: UMLDependencyClientListModel.java,v 1.5 2003/08/31 00:17:57 bobtarling Exp $
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

// $Id: UMLDependencyClientListModel.java,v 1.5 2003/08/31 00:17:57 bobtarling Exp $
package org.argouml.uml.ui.foundation.core;

import org.argouml.model.ModelFacade;
import org.argouml.uml.ui.UMLModelElementListModel2;

import ru.novosoft.uml.MBase;
import ru.novosoft.uml.foundation.core.MDependency;


/**
 * 
 * @author jaap.branderhorst@xs4all.nl	
 * @since Jan 3, 2003
 */
public class UMLDependencyClientListModel extends UMLModelElementListModel2 {

    /**
     * Constructor for UMLModelElementClientDependencyListModel.
     * @param container
     */
    public UMLDependencyClientListModel() {
        super("client");
    }

     /**
     * @see org.argouml.uml.ui.UMLModelElementListModel2#buildModelList()
     */
    protected void buildModelList() {
        if (_target != null) 
            setAllElements(ModelFacade.getClients(getTarget()));
    }

    /**
     * @see org.argouml.uml.ui.UMLModelElementListModel2#isValidElement(MBase)
     */
    protected boolean isValidElement(MBase o) {  
        return org.argouml.model.ModelFacade.isAModelElement(o) && ModelFacade.getClients(getTarget()).contains(o);
    }

}