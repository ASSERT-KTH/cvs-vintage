// $Id: TestUMLExtendExtensionComboBoxModel.java,v 1.12 2005/01/02 10:08:17 linus Exp $
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

package org.argouml.uml.ui.behavior.use_cases;

import junit.framework.TestCase;

import org.argouml.kernel.ProjectManager;
import org.argouml.model.Model;
import org.argouml.ui.targetmanager.TargetEvent;

import ru.novosoft.uml.MFactoryImpl;
import ru.novosoft.uml.behavior.use_cases.MExtend;
import ru.novosoft.uml.behavior.use_cases.MUseCase;
import ru.novosoft.uml.model_management.MModel;

/**
 * @since Oct 31, 2002
 * @author jaap.branderhorst@xs4all.nl
 */
public class TestUMLExtendExtensionComboBoxModel extends TestCase {

    private int oldEventPolicy;
    private MUseCase[] extensions;
    private UMLExtendExtensionComboBoxModel model;
    private MExtend elem;
    
    /**
     * Constructor for TestUMLExtendExtensionComboBoxModel.
     * @param arg0 is the name of the test case.
     */
    public TestUMLExtendExtensionComboBoxModel(String arg0) {
        super(arg0);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        elem = Model.getUseCasesFactory().createExtend();
        oldEventPolicy = MFactoryImpl.getEventPolicy();
        MFactoryImpl.setEventPolicy(MFactoryImpl.EVENT_POLICY_IMMEDIATE);
        model = new UMLExtendExtensionComboBoxModel();
        model.targetSet(new TargetEvent(this, "set", new Object[0], 
                new Object[] {elem}));
        extensions = new MUseCase[10];
        MModel m = Model.getModelManagementFactory().createModel();
        ProjectManager.getManager().getCurrentProject().setRoot(m);
        for (int i = 0; i < 10; i++) {
            extensions[i] = Model.getUseCasesFactory().createUseCase();
            m.addOwnedElement(extensions[i]);
        }
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        Model.getUmlFactory().delete(elem);
        for (int i = 0; i < 10; i++) {
            Model.getUmlFactory().delete(extensions[i]);
        }
        MFactoryImpl.setEventPolicy(oldEventPolicy);
        model = null;
    }
    
    /**
     * Test setup.
     */
    public void testSetUp() {
        assertEquals(10, model.getSize());
        assertTrue(model.contains(extensions[5]));
        assertTrue(model.contains(extensions[0]));
        assertTrue(model.contains(extensions[9]));
    }
    
    /**
     * Test setExtension().
     */
    public void testSetBase() {
        elem.setExtension(extensions[0]);
        assertTrue(model.getSelectedItem() == extensions[0]);
    }
    
    /**
     * Test setExtension() with null argument.
     */
    public void testSetBaseToNull() {
        elem.setExtension(null);
        assertNull(model.getSelectedItem());
    }
    
    /**
     * Test delete().
     */
    public void testRemoveBase() {
        Model.getUmlFactory().delete(extensions[9]);
        assertEquals(9, model.getSize());
        assertTrue(!model.contains(extensions[9]));
    } 
}
