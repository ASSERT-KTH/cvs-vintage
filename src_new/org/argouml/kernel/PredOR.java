// $Id: PredOR.java,v 1.3 2003/08/21 20:42:38 alexb Exp $
// Copyright (c) 2003 The Regents of the University of California. All
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

/*
 * PredOR.java
 */

package org.argouml.kernel;

import org.tigris.gef.util.*;

/**
 * Predicate to provide logical <B>OR</B> between two other predicates.  
 * 
 * This class belongs in org.argouml.util
 *
 * @author Eugenio Alvarez
 *
 */
public class PredOR implements Predicate {
    protected Predicate _predicate1;
    protected Predicate _predicate2;

    public PredOR(Predicate predicate1, Predicate predicate2) { 
	_predicate1 = predicate1; 
	_predicate2 = predicate2;
    }

    /**
     * Returns true if at least one of its internal Predicates
     * return true; 
     *
     * @param  Object to test. 
     *
     * @return Returns true if at least one of its internal 
     *         Predicates return true.
     */
    public boolean predicate(Object obj) {
	return  _predicate1.predicate(obj) || _predicate2.predicate(obj);      
    }

} /* end class PredOR */

