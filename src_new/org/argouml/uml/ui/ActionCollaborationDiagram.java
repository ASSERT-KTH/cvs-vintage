// $Id: ActionCollaborationDiagram.java,v 1.41 2005/01/08 23:27:29 linus Exp $
// Copyright (c) 1996-2005 The Regents of the University of California. All
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
import org.argouml.model.Model;
import org.argouml.model.ModelFacade;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.diagram.collaboration.ui.UMLCollaborationDiagram;
import org.argouml.uml.diagram.ui.UMLDiagram;

/** Action to trigger creation of new collaboration diagram.
 *  @stereotype singleton
 */
public class ActionCollaborationDiagram extends ActionAddDiagram {
    
    private static final Logger LOG = 
        Logger.getLogger(ActionCollaborationDiagram.class);

    /**
     * The singleton.
     */
    public static final ActionCollaborationDiagram SINGLETON =
        new ActionCollaborationDiagram();

    /**
     * Constructor.
     */
    private ActionCollaborationDiagram() {
        super("action.collaboration-diagram");
    }

    /**
     * @see org.argouml.uml.ui.ActionAddDiagram#createDiagram(Object)
     */
    public UMLDiagram createDiagram(Object namespace) {
        if (!ModelFacade.isANamespace(namespace)) {
            LOG.error("No namespace as argument");
            LOG.error(namespace);
            throw new IllegalArgumentException(
                "The argument " + namespace + "is not a namespace.");
        }
        Object target = TargetManager.getInstance().getModelTarget();
        Object collaboration = null;
        if (ModelFacade.isAOperation(target)) {
            collaboration = Model.getCollaborationsFactory()
                            .buildCollaboration(namespace, target);
        } else if (ModelFacade.isAClassifier(target)) {
            collaboration = Model.getCollaborationsFactory()
                            .buildCollaboration(namespace, target);
//        } else if (ModelFacade.isAModel(target)) {
//            collaboration = Model.getCollaborationsFactory()
//                            .buildCollaboration(target);
//        } else if (ModelFacade.isAInteraction(target)) {
//            collaboration = ModelFacade.getContext(target);
//        } else if (target instanceof UMLCollaborationDiagram) {
//            Object owner = ((UMLCollaborationDiagram) target).getOwner();
//            if (ModelFacade.isACollaboration(owner)) {
//                //preventing backward compat problems
//                collaboration = owner;
//            }
//        } else if (ModelFacade.isACollaboration(target)) {
//            collaboration = target;
//        } else {
//            collaboration =
//                Model.getCollaborationsFactory().buildCollaboration(
//                    namespace);
        }
        UMLCollaborationDiagram d = 
            new UMLCollaborationDiagram(namespace, collaboration);
        return d;
    }

    /**
     * @see org.argouml.uml.ui.ActionAddDiagram#isValidNamespace(java.lang.Object)
     */
    public boolean isValidNamespace(Object handle) {
        if (!ModelFacade.isANamespace(handle)) {
            LOG.error("No namespace as argument");
            LOG.error(handle);
            throw new IllegalArgumentException(
                "The argument " + handle + "is not a namespace.");
        }
        Object/*MNamespace*/ ns = handle;
        return Model.getCollaborationsHelper()
                                    .isAddingCollaborationAllowed(ns);
    }

    /**
     * @see org.argouml.uml.ui.UMLAction#shouldBeEnabled()
     */
    public boolean shouldBeEnabled() {
        Object target = TargetManager.getInstance().getModelTarget();
        if (ModelFacade.isAOperation(target))
            return super.shouldBeEnabled() 
                && Model.getCollaborationsHelper()
                    .isAddingCollaborationAllowed(target);
        else if (ModelFacade.isANamespace(target))
                return super.shouldBeEnabled() && isValidNamespace(target);
        return false;
    }

} /* end class ActionCollaborationDiagram */
