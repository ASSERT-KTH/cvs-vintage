// $Id: ActionClassDiagram.java,v 1.15 2004/05/07 21:09:37 mvw Exp $
// Copyright (c) 1996-2003 The Regents of the University of California. All
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

package org.argouml.uml.ui;

import org.apache.log4j.Logger;
import org.argouml.model.ModelFacade;
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;
import org.argouml.uml.diagram.ui.UMLDiagram;

/** Action to trigger creation of new class diagram.
 *  @stereotype singleton
 */
public class ActionClassDiagram extends ActionAddDiagram {
    private static final Logger LOG = 
        Logger.getLogger(ActionClassDiagram.class);

    ////////////////////////////////////////////////////////////////
    // static variables

    public static ActionClassDiagram SINGLETON = new ActionClassDiagram();

    ////////////////////////////////////////////////////////////////
    // constructors

    private ActionClassDiagram() {
        super("action.class-diagram");
    }

    /**
     * @see org.argouml.uml.ui.ActionAddDiagram#createDiagram(Object)
     */
    public UMLDiagram createDiagram(Object ns) {
        if (ModelFacade.isANamespace(ns)) {
            return new UMLClassDiagram(ns);
        }
        LOG.error("No namespace as argument");
        LOG.error(ns);
        throw new IllegalArgumentException(
            "The argument " + ns + "is not a namespace.");
    }

    /**
     * @see org.argouml.uml.ui.ActionAddDiagram#isValidNamespace(Object)
     */
    public boolean isValidNamespace(Object handle) {
        if (!ModelFacade.isANamespace(handle)) {
            LOG.error("No namespace as argument");
            LOG.error(handle);
            throw new IllegalArgumentException(
                "The argument " + handle + "is not a namespace.");
        }
        return true;       
    }
} /* end class ActionClassDiagram */
