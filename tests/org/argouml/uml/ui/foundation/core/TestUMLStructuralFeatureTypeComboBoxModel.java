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

// $header$
package org.argouml.uml.ui.foundation.core;

import junit.framework.TestCase;

import org.argouml.application.security.ArgoSecurityManager;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.uml.UmlFactory;
import org.argouml.model.uml.foundation.core.CoreFactory;
import org.argouml.model.uml.modelmanagement.ModelManagementFactory;

import ru.novosoft.uml.MFactoryImpl;
import ru.novosoft.uml.foundation.core.MAttribute;
import ru.novosoft.uml.foundation.core.MClassifier;
import ru.novosoft.uml.model_management.MModel;

/**
 * @since Nov 2, 2002
 * @author jaap.branderhorst@xs4all.nl
 */
public class TestUMLStructuralFeatureTypeComboBoxModel extends TestCase {

    private int oldEventPolicy;
    private MClassifier[] types;
    private UMLStructuralFeatureTypeComboBoxModel model;
    private MAttribute elem;
    
    /**
     * Constructor for TestUMLStructuralFeatureTypeComboBoxModel.
     * @param arg0
     */
    public TestUMLStructuralFeatureTypeComboBoxModel(String arg0) {
        super(arg0);
    }
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ArgoSecurityManager.getInstance().setAllowExit(true);
        UmlFactory.getFactory().setGuiEnabled(false);
        elem = CoreFactory.getFactory().createAttribute();
        oldEventPolicy = MFactoryImpl.getEventPolicy();
        MFactoryImpl.setEventPolicy(MFactoryImpl.EVENT_POLICY_IMMEDIATE);
        model = new UMLStructuralFeatureTypeComboBoxModel();
        model.targetChanged(elem);
        types = new MClassifier[10];
        MModel m = ModelManagementFactory.getFactory().createModel();
        ProjectManager.getManager().getCurrentProject().setRoot(m);
        elem.setNamespace(m);
        for (int i = 0 ; i < 10; i++) {
            types[i] = CoreFactory.getFactory().createClassifier();
            m.addOwnedElement(types[i]);
        }      
        elem.setType(types[0]);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        UmlFactory.getFactory().delete(elem);
        for (int i = 0 ; i < 10; i++) {
            UmlFactory.getFactory().delete(types[i]);
        }
        MFactoryImpl.setEventPolicy(oldEventPolicy);
        model = null;
    }
    
    public void testSetUp() {
        assertTrue(model.contains(types[5]));
        assertTrue(model.contains(types[0]));
        assertTrue(model.contains(types[9]));
    }
    
    public void testSetType() {
        elem.setType(types[0]);
        assertTrue(model.getSelectedItem() == types[0]);
    }
    
    public void testSetTypeToNull() {
        elem.setType(null);
        assertNull(model.getSelectedItem());
    }
    
    public void testRemoveType() {
        UmlFactory.getFactory().delete(types[9]);
        assertTrue(!model.contains(types[9]));
    } 


}
