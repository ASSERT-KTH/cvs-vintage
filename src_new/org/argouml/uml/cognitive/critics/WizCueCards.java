// $Id: WizCueCards.java,v 1.7 2004/08/29 17:03:26 mvw Exp $
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



// File: WizCueCards.java
// Classes: WizCueCards
// Original Author: jrobbins@ics.uci.edu
// $Id: WizCueCards.java,v 1.7 2004/08/29 17:03:26 mvw Exp $

package org.argouml.uml.cognitive.critics;

import java.util.Vector;
import javax.swing.JPanel;
import org.argouml.cognitive.ui.WizStepCue;
import org.argouml.kernel.Wizard;
import org.tigris.gef.util.VectorSet;
/** A non-modal wizard to help the user change navigability
 *  of an association. */

public class WizCueCards extends Wizard {

    private Vector cues = new Vector();
    private WizStepCue steps[] = null;

    /**
     * The constructor.
     */
    public WizCueCards() { }

    /**
     * @see org.argouml.kernel.Wizard#getNumSteps()
     */
    public int getNumSteps() { return cues.size(); }

    /**
     * @return the offending modelelement
     */
    public Object getModelElement() {
	if (_item != null) {
	    VectorSet offs = _item.getOffenders();
	    if (offs.size() >= 1) {
		Object me = /*(MModelElement)*/ offs.elementAt(0);
		return me;
	    }
	}
	return null;
    }

    /**
     * @param s
     */
    public void addCue(String s) { cues.addElement(s); }

    /** 
     * Create a new panel for the given step.
     * Returns a newly created panel or null if there isn't that many steps.
     *
     * @see org.argouml.kernel.Wizard#makePanel(int)
     */
    public JPanel makePanel(int newStep) {
	if (newStep <= getNumSteps()) {
	    String c = (String) cues.elementAt(newStep - 1);
	    return new WizStepCue(this, c);
	}
	return null;
    }

    /**
     * This wizard never takes action, it just displays step by step
     * instructions.
     * 
     * @see org.argouml.kernel.Wizard#doAction(int)
     */
    public void doAction(int oldStep) {  }

    /** 
     * This wizard cannot automatically finish the task. It can only be
     * finished when the user is on the last step. 
     * 
     * @see org.argouml.kernel.Wizard#canFinish()
     */
    public boolean canFinish() {
	return _step == getNumSteps();
    }


} /* end class WizCueCards */