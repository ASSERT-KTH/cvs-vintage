
// $Id: UMLMultiplicityComboBoxModel.java,v 1.5 2003/09/28 19:10:53 bobtarling Exp $
// Copyright (c) 1996-2002 The Regents of the University of California. All
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

// $Id: UMLMultiplicityComboBoxModel.java,v 1.5 2003/09/28 19:10:53 bobtarling Exp $
package org.argouml.uml.ui;

import java.util.ArrayList;
import java.util.List;
import org.argouml.model.ModelFacade;

//import ru.novosoft.uml.foundation.data_types.MMultiplicity;

/**
 * A model for multiplicities. This model is instantiated with a few default
 * values. 
 * @author jaap.branderhorst@xs4all.nl	
 * @since Jan 5, 2003
 */
public abstract class UMLMultiplicityComboBoxModel extends UMLComboBoxModel2 {
    
    private static List multiplicityList = new ArrayList();
    
    static {
        multiplicityList.add(ModelFacade.M0_N_MULTIPLICITY);
        multiplicityList.add(ModelFacade.M0_1_MULTIPLICITY);
        multiplicityList.add(ModelFacade.M1_1_MULTIPLICITY);
        multiplicityList.add(ModelFacade.M1_N_MULTIPLICITY);
    }

    /**
     * Constructor for UMLMultiplicityComboBoxModel.
     * @param propertySetName
     * @param clearable
     */
    public UMLMultiplicityComboBoxModel(String propertySetName) {
        super(propertySetName, false);
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#isValidElement(ru.novosoft.uml.MBase)
     */
    protected boolean isValidElement(Object element) {
        return org.argouml.model.ModelFacade.isAMultiplicity(element);
    }

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#buildModelList()
     */
    protected void buildModelList() {
        setElements(multiplicityList);
    }    

    /**
     * @see org.argouml.uml.ui.UMLComboBoxModel2#addElement(java.lang.Object)
     */
    public void addElement(Object o) {
        if (o == null) return;
        if (!multiplicityList.contains(o) && isValidElement(o)) {
            multiplicityList.add(o);
        }
        super.addElement(o);        
    }    

    

    /**
     * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
     */
    public void setSelectedItem(Object anItem) {        
        if (!contains(anItem) && org.argouml.model.ModelFacade.isAMultiplicity(anItem)) {
            addElement(anItem);
        }        
        super.setSelectedItem(anItem);        
    }

}