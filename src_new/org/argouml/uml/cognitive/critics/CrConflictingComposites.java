// $Id: CrConflictingComposites.java,v 1.7 2003/08/31 11:09:56 alexb Exp $
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



// File: CrConflictingComposites.java
// Classes: CrConflictingComposites
// Original Author: jrobbins@ics.uci.edu
// $Id: CrConflictingComposites.java,v 1.7 2003/08/31 11:09:56 alexb Exp $

package org.argouml.uml.cognitive.critics;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.argouml.cognitive.Designer;
import org.argouml.cognitive.critics.Critic;
import org.argouml.model.ModelFacade;
import org.argouml.model.uml.UmlHelper;

import ru.novosoft.uml.foundation.core.MAssociation;
import ru.novosoft.uml.foundation.core.MAssociationEnd;
import ru.novosoft.uml.foundation.core.MClassifier;
import ru.novosoft.uml.foundation.data_types.MAggregationKind;
import ru.novosoft.uml.foundation.data_types.MMultiplicity;



/** Well-formedness rule [2] for MAssociationEnd. See page 28 of UML 1.1
 *  Semantics. OMG document ad/97-08-04. */

public class CrConflictingComposites extends CrUML {

    public CrConflictingComposites() {
	setHeadline("Remove Conflicting Composite Associations");

	addSupportedDecision(CrUML.decCONTAINMENT);
	setKnowledgeTypes(Critic.KT_SEMANTICS);
	// no good trigger
    }

    public boolean predicate2(Object dm, Designer dsgr) {
	if (!(ModelFacade.isAClassifier(dm))) return NO_PROBLEM;
	MClassifier cls = (MClassifier) dm;
	Collection conns = cls.getAssociationEnds();
	if (conns == null) return NO_PROBLEM;
	int compositeCount = 0;
	Iterator enum = conns.iterator();
	while (enum.hasNext()) {
            Object myEnd = enum.next();
	    if (UmlHelper.getHelper().getCore()
                .equalsAggregationKind(myEnd,"composite"))
		continue;
	    if (ModelFacade.getLower(myEnd) == 0) continue;
	    Object asc = ModelFacade.getAssociation(myEnd);
	    if (asc != null &&
                UmlHelper.getHelper().getCore().hasCompositeEnd(asc)){
                    compositeCount++;
            }
	}
	if (compositeCount > 1) return PROBLEM_FOUND;
	return NO_PROBLEM;
    }
} /* end class CrConflictingComposites.java */
