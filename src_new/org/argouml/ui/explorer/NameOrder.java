// $Id: NameOrder.java,v 1.7 2004/09/01 15:43:20 mvw Exp $
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

package org.argouml.ui.explorer;

import java.util.*;
import javax.swing.tree.DefaultMutableTreeNode;

import org.argouml.model.ModelFacade;

/**
 * Sorts explorer nodes by their user object name.
 *
 * @author  alexb
 * @since 0.15.2, Created on 28 September 2003, 10:02
 */
public class NameOrder 
    implements Comparator {
    
    /** Creates a new instance of NameOrder */
    public NameOrder() {
    }
    
    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object obj1, Object obj2) {
	if (obj1 instanceof DefaultMutableTreeNode) {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj1;
	    obj1 = node.getUserObject();
	}

	if (obj2 instanceof DefaultMutableTreeNode) {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj2;
	    obj2 = node.getUserObject();
	}
        
        return compareUserObjects(obj1, obj2);
    }
    
    /**
     * alphabetic ordering of user object names instead of type names
     *
     * @param obj Diagram or Base
     * @param obj1 Diagram or Base
     * @return 0 if invalid params. 0 if the objects are equally named. 
     *         A positive or negative int if the names differ.
     */
    protected int compareUserObjects(Object obj, Object obj1) {
        if ((ModelFacade.isADiagram(obj)
	     || ModelFacade.isABase(obj) )
	    && (ModelFacade.isADiagram(obj1)
		|| ModelFacade.isABase(obj1) )) {
	    String name =
		ModelFacade.getName(obj) == null
		? "" : ModelFacade.getName(obj);
	    String name1 =
		ModelFacade.getName(obj1) == null
		? "" : ModelFacade.getName(obj1);
            int ret = name.compareTo(name1);

	    return ret;
	}

	return 0;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Order By Name";
        // TODO: i18n
    }
}
