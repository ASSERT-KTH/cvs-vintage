// $Id: CrUnnavigableAssoc.java,v 1.10 2004/03/25 22:30:00 mvw Exp $
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

// File: CrEmptyPackage.java
// Classes: CrEmptyPackage
// Original Author: jrobbins@ics.uci.edu
// $Id: CrUnnavigableAssoc.java,v 1.10 2004/03/25 22:30:00 mvw Exp $

package org.argouml.uml.cognitive.critics;

import java.util.Collection;
import java.util.Iterator;
import org.argouml.cognitive.Designer;
import org.argouml.cognitive.ToDoItem;
import org.argouml.model.ModelFacade;
/** A critic to detect when a class can never have instances (of
 *  itself of any subclasses). */

public class CrUnnavigableAssoc extends CrUML {

    public CrUnnavigableAssoc() {
	setHeadline("Make <ocl>self</ocl> Navigable");

	addSupportedDecision(CrUML.decRELATIONSHIPS);
	addTrigger("end_navigable");
    }

    public boolean predicate2(Object dm, Designer dsgr) {
	if (!(ModelFacade.isAAssociation(dm))) return NO_PROBLEM;
	Object asc = /*(MAssociation)*/ dm;
	Collection conn = ModelFacade.getConnections(asc);
	if (ModelFacade.isAAssociationRole(asc))
	    conn = ModelFacade.getConnections(asc);
	for (Iterator iter = conn.iterator(); iter.hasNext();) {
	    Object ae = /*(MAssociationEnd)*/ iter.next();
	    if (ModelFacade.isNavigable(ae)) return NO_PROBLEM;
	}
	return PROBLEM_FOUND;
    }

    public Class getWizardClass(ToDoItem item) { return WizNavigable.class; }

} /* end class CrUnnavigableAssoc */