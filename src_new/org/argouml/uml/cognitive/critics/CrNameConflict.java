// $Id: CrNameConflict.java,v 1.6 2003/11/05 21:58:47 linus Exp $
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



// File: CrNameConflict.java
// Classes: CrNameConflict
// Original Author: jrobbins@ics.uci.edu
// $Id: CrNameConflict.java,v 1.6 2003/11/05 21:58:47 linus Exp $

package org.argouml.uml.cognitive.critics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.argouml.cognitive.Designer;
import org.argouml.cognitive.ToDoItem;
import org.argouml.cognitive.critics.Critic;
import org.argouml.kernel.Wizard;
import org.argouml.model.ModelFacade;


/** Well-formedness rule [1] for MNamespace. See page 33 of UML 1.1
 *  Semantics. OMG document ad/97-08-04. */

public class CrNameConflict extends CrUML {

    public CrNameConflict() {
        setHeadline("Revise Name to Avoid Conflict");
        addSupportedDecision(CrUML.decNAMING);
        setKnowledgeTypes(Critic.KT_SYNTAX);
        addTrigger("name");
        addTrigger("feature_name");
    }

    public boolean predicate2(Object dm, Designer dsgr) {      
        boolean problem = NO_PROBLEM;
        if (ModelFacade.isANamespace(dm)) {
            Iterator it = ModelFacade.getOwnedElements(dm).iterator();
            Collection names = new ArrayList(); 
            while (it.hasNext()) {  
                String name = ModelFacade.getName(it.next());
		if (name == null)
		    continue;
		if ("".equals(name))
		    continue;
                if (names.contains(name)) {  
                    problem = PROBLEM_FOUND; 
                    break;   
                }
                names.add(name); 
            } 
        } 
        return problem; 
    }

    public void initWizard(Wizard w) {
        if (w instanceof WizMEName) {
            ToDoItem item = w.getToDoItem();
            Object me = item.getOffenders().elementAt(0);
            String sug = ModelFacade.getName(me);
            String ins = "Change the name to something different.";
            ((WizMEName) w).setInstructions(ins);
            ((WizMEName) w).setSuggestion(sug);
            ((WizMEName) w).setMustEdit(true);
        }
    }

    public Class getWizardClass(ToDoItem item) { return WizMEName.class; }


} /* end class CrNameConflict.java */

