// $Id: CrMultipleRealization.java,v 1.11 2005/01/29 16:28:45 linus Exp $
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

package org.argouml.language.java.cognitive.critics;

import java.util.Collection;

import org.argouml.cognitive.Designer;
import org.argouml.model.Model;
import org.argouml.model.ModelFacade;
import org.argouml.uml.cognitive.critics.CrUML;

// related to issue 570

/**
 * Critic to check whether in java no inerface realizes another interface.
 *
 * @author jrobbins
 */
public class CrMultipleRealization extends CrUML {

    /**
     * The constructor.
     */
    public CrMultipleRealization() {
	setHeadline("Interface cannot realize another interface");
	addSupportedDecision(CrUML.DEC_INHERITANCE);
	addSupportedDecision(CrUML.DEC_CODE_GEN);
	addTrigger("generalization");
    }

    /**
     * @see org.argouml.uml.cognitive.critics.CrUML#predicate2(
     * java.lang.Object, org.argouml.cognitive.Designer)
     */
    public boolean predicate2(Object dm, Designer dsgr) {
	if (!(ModelFacade.isAInterface(dm))) {
	    return NO_PROBLEM;
	}
	Object inter = /*(MInterface)*/ dm;

	Collection realize =
	    Model.getCoreHelper().getSpecifications(inter);

	if (realize != null && realize.size() > 0) {
	    return PROBLEM_FOUND;
	}
	return NO_PROBLEM;
    }
} /* end class CrMultipleRealization.java */
