// $Id: UMLExpressionExpressionModel.java,v 1.2 2005/01/02 10:08:18 linus Exp $
// Copyright (c) 2004 The Regents of the University of California. All
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

import org.argouml.model.Model;
import org.argouml.model.ModelFacade;

/**
 * The model for a UML Expression that is obtained from its "parent"
 * by getExpression - hence: Guard, ChangeEvent, TimeEvent. 
 * 
 * @author Michiel
 */
public class UMLExpressionExpressionModel extends UMLExpressionModel2 {

    /**
     * The constructor.
     * 
     * @param c the container of UML user interface components
     * @param name the name of the property
     */
    public UMLExpressionExpressionModel(UMLUserInterfaceContainer c, 
            String name) {
        super(c, name);
    }

    /**
     * @see org.argouml.uml.ui.UMLExpressionModel2#getExpression()
     */
    public Object getExpression() {
        return ModelFacade.getExpression(getContainer().getTarget());
    }

    /**
     * @see org.argouml.uml.ui.UMLExpressionModel2#setExpression(java.lang.Object)
     */
    public void setExpression(Object expr) {
        ModelFacade.setExpression(getContainer().getTarget(), expr);
    }

    /**
     * @see org.argouml.uml.ui.UMLExpressionModel2#newExpression()
     */
    public Object newExpression() {
        return Model.getDataTypesFactory().createBooleanExpression("", "");
    }

}
