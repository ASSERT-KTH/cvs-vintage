// $Id: CrNoTriggerOrGuard.java,v 1.12 2005/01/09 14:58:36 linus Exp $
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

// $Id: CrNoTriggerOrGuard.java,v 1.12 2005/01/09 14:58:36 linus Exp $
package org.argouml.uml.cognitive.critics;

import org.argouml.cognitive.Designer;
import org.argouml.cognitive.critics.Critic;
import org.argouml.model.ModelFacade;
/**
 * A critic that checks for missing trigger and/or guard.
 *
 *
 * @author jrobbins
 */
public class CrNoTriggerOrGuard extends CrUML {

    /**
     * The constructor.
     *
     */
    public CrNoTriggerOrGuard() {
	setHeadline("Add Trigger or Guard to Transistion");
	addSupportedDecision(CrUML.DEC_STATE_MACHINES);
	setKnowledgeTypes(Critic.KT_COMPLETENESS);
	addTrigger("trigger");
	addTrigger("guard");
    }

    /**
     * @see org.argouml.uml.cognitive.critics.CrUML#predicate2(
     * java.lang.Object, org.argouml.cognitive.Designer)
     */
    public boolean predicate2(Object dm, Designer dsgr) {
	if (!(ModelFacade.isATransition(dm))) return NO_PROBLEM;
	Object tr = /*(MTransition)*/ dm;
	Object/*MEvent*/ t = ModelFacade.getTrigger(tr);
	Object g = ModelFacade.getGuard(tr);
	Object sv = ModelFacade.getSource(tr);
	if (!(ModelFacade.isAState(sv))) return NO_PROBLEM;
	if (ModelFacade.getDoActivity(sv) != null) return NO_PROBLEM;
	boolean hasTrigger =
	    (t != null
            && ModelFacade.getName(t) != null
            && ModelFacade.getName(t).length() > 0);
	if (hasTrigger) return NO_PROBLEM;
	boolean noGuard = (g == null
            || ModelFacade.getExpression(g) == null
            || ModelFacade.getBody(ModelFacade.getExpression(g)) == null
            || ModelFacade.getBody(ModelFacade.getExpression(g)).toString()
                                .length() == 0);
	if (noGuard) return PROBLEM_FOUND;
	return NO_PROBLEM;
    }

} /* end class CrNoTriggerOrGuard */
