// $Id: ActionAddAttribute.java,v 1.28 2004/04/07 13:26:27 d00mst Exp $
// Copyright (c) 1996-2004 The Regents of the University of California. All
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

import java.awt.event.ActionEvent;

import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.ModelFacade;
import org.argouml.model.uml.UmlFactory;
import org.argouml.ui.ProjectBrowser;
import org.argouml.ui.targetmanager.TargetManager;

/** Action to add an attribute to a classifier.
 *  @stereotype singleton
 * @deprecated since 0.15.2, replace with {@link 
 * org.argouml.uml.diagram.ui.ActionAddAttribute}, remove 0.15.3, alexb
 */
public class ActionAddAttribute extends UMLChangeAction {

    ////////////////////////////////////////////////////////////////
    // static variables

    public static ActionAddAttribute SINGLETON = new ActionAddAttribute();

    ////////////////////////////////////////////////////////////////
    // constructors

    public ActionAddAttribute() { super("button.new-attribute"); }

    ////////////////////////////////////////////////////////////////
    // main methods

    public void actionPerformed(ActionEvent ae) {	
	Object target = TargetManager.getInstance().getModelTarget();
	Object/*MClassifier*/ cls = null;

	if (ModelFacade.isAClassifier(target))
	    cls = target;
	else if (ModelFacade.isAFeature(target)
		 && ModelFacade.isAClass(ModelFacade.getOwner(target)))
	    cls = ModelFacade.getOwner(target);
	else
	    return;

	Object/*MAttribute*/ attr = UmlFactory.getFactory().getCore().buildAttribute(cls);
	TargetManager.getInstance().setTarget(attr);
	super.actionPerformed(ae);
    }

    public boolean shouldBeEnabled() {
	ProjectBrowser pb = ProjectBrowser.getInstance();
	Object target =  TargetManager.getInstance().getModelTarget();
	/*
	if (target instanceof MInterface) {
		return Notation.getDefaultNotation().getName().equals("Java");
	}
	*/
	return super.shouldBeEnabled()
	       && (ModelFacade.isAClass(target)
		   || (ModelFacade.isAFeature(target)
		       && ModelFacade.isAClass(ModelFacade.getOwner(target))));
    }
} /* end class ActionAddAttribute */
