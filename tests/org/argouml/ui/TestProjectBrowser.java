// $Id: TestProjectBrowser.java,v 1.6 2003/04/29 19:03:32 kataka Exp $
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

package org.argouml.ui;

import org.argouml.application.security.ArgoSecurityManager;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.uml.UmlFactory;
import org.argouml.model.uml.modelmanagement.ModelManagementFactory;
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;

import ru.novosoft.uml.MFactoryImpl;

import junit.framework.TestCase;

/**
 * @since Nov 23, 2002
 * @author jaap.branderhorst@xs4all.nl
 */
public class TestProjectBrowser extends TestCase {

    /**
     * Constructor for TestProjectBrowser.
     * @param arg0
     */
    public TestProjectBrowser(String arg0) {
        super(arg0);
    }

    /**
     * Tests wether it is possible to construct a standalone projectbrowser
     */
    public void testConstruction() {
        ProjectBrowser pb;
        try {          
            assertNotNull(ProjectBrowser.getInstance());
        } catch (java.lang.InternalError e) {
            // This is when we cannot connect to the display system.
            // The test is inconclusive
        }
    }

    /**
     * Tests the construction of the splashscreen
     */
    public void testSplashScreen() {
        ProjectBrowser pb;
        try {
            ProjectBrowser.TheInstance = null;
            ProjectBrowser.setSplash(true);
            assertNotNull(ProjectBrowser.getInstance().getSplashScreen());
        } catch (java.lang.NoClassDefFoundError e) {
            // Some problem caused by the lack of display system.
            // The test is inconclusive
        }
    }
    

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ArgoSecurityManager.getInstance().setAllowExit(true);
    }
    
    public void testSetTarget() {
        ProjectBrowser pb;
        try {
            pb = ProjectBrowser.getInstance();
            MFactoryImpl.setEventPolicy(MFactoryImpl.EVENT_POLICY_IMMEDIATE);
            Object package1 = ModelManagementFactory.getFactory().buildPackage("test1", null);
            Object package2 = ModelManagementFactory.getFactory().buildPackage("test2", null);
            UMLClassDiagram diagram1 = new UMLClassDiagram(package1);
            UMLClassDiagram diagram2 = new UMLClassDiagram(package2);
            pb.setTarget(diagram1);
            assertEquals(diagram1, pb.getTarget());
            pb.setTarget(diagram2);
            assertEquals(diagram2, pb.getTarget());
            UmlFactory.getFactory().delete(package2);
            assertEquals(ProjectManager.getManager().getCurrentProject().getDiagrams().get(0), pb.getTarget());
            
        }
        catch (java.lang.NoClassDefFoundError e) {
                    // Some problem caused by the lack of display system.
                    // The test is inconclusive
                }
    }

}
