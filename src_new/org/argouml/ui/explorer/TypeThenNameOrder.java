// $Id: TypeThenNameOrder.java,v 1.7 2004/04/22 21:56:26 d00mst Exp $
// Copyright (c) 1996-2001 The Regents of the University of California. All
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

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Sorts by user object type,
 * diagrams first,
 * then packages,
 * then other types.
 *
 * sorts by name within type groups.
 *
 * @since 0.15.2, Created on 28 September 2003, 10:02
 *
 * @author  alexb
 */
public class TypeThenNameOrder extends NameOrder{
    
    /** Creates a new instance of TypeThenNameOrder */
    public TypeThenNameOrder() {
    }
    
    public int compare(Object obj1, Object obj2) {
	if (obj1 instanceof DefaultMutableTreeNode) {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj1;
	    obj1 = node.getUserObject();
	}

	if (obj2 instanceof DefaultMutableTreeNode) {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj2;
	    obj2 = node.getUserObject();
	}

        String typeName = obj1.getClass().getName();
        String typeName1 = obj2.getClass().getName();

        // all diagram types treated equally, see issue 2260
	// if (typeName.indexOf("Diagram") != -1
	//     && typeName1.indexOf("Diagram") != -1)
	//     return compareUserObjects(obj1, obj2);

        int typeNameOrder = typeName.compareTo(typeName1);
        if (typeNameOrder == 0)
            return compareUserObjects(obj1, obj2);

        if (typeName.indexOf("Diagram") == -1
	    && typeName1.indexOf("Diagram") != -1)
            return 1;

        if (typeName.indexOf("Diagram") != -1
	    && typeName1.indexOf("Diagram") == -1)
            return -1;

        if (typeName.indexOf("Package") == -1
	    && typeName1.indexOf("Package") != -1)
            return 1;

        if (typeName.indexOf("Package") != -1
	    && typeName1.indexOf("Package") == -1)
            return -1;

        return typeNameOrder;
    }
    
    public String toString(){
        return "Order By Type, Name";
    }
}
