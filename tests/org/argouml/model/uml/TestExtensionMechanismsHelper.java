// $Id: TestExtensionMechanismsHelper.java,v 1.11 2005/08/12 19:30:26 mvw Exp $
// Copyright (c) 1996-2005 The Regents of the University of California. All
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

package org.argouml.model.uml;

import java.util.Collection;

import junit.framework.TestCase;

import org.argouml.kernel.ProjectManager;
import org.argouml.model.CheckUMLModelHelper;
import org.argouml.model.Model;

/**
 * @since Oct 10, 2002
 * @author jaap.branderhorst@xs4all.nl
 */
public class TestExtensionMechanismsHelper extends TestCase {

    /**
     * Constructor for TestExtensionMechanismsHelper.
     *
     * @param arg0 is the name of the test case.
     */
    public TestExtensionMechanismsHelper(String arg0) {
        super(arg0);
    }

    /**
     * This test does not work yet since there are problems with
     * isolating the project from the projectbrowser.
     */
    public void testGetAllPossibleStereotypes1() {
        Object ns = Model.getCoreFactory().createNamespace();
        Object clazz = Model.getCoreFactory().buildClass(ns);
        Model model =
            (Model) ProjectManager.getManager().getCurrentProject()
            	.getModel();
        Collection models =
            ProjectManager.getManager().getCurrentProject()
            	.getModels();
        Object stereo1 = Model.getExtensionMechanismsFactory().buildStereotype(
            	        clazz,
            	        "test1",
            	        model,
            	        models);
        Object stereo2 = Model.getExtensionMechanismsFactory().buildStereotype(
	                clazz,
		        "test2",
		        model,
		        models);
        Collection col =
	    Model.getExtensionMechanismsHelper()
	        .getAllPossibleStereotypes(models, clazz);
        assertTrue("stereotype not in list of possible stereotypes",
		   col.contains(stereo1));
        assertTrue("stereotype not in list of possible stereotypes",
		   col.contains(stereo2));
    }

    /**
     * Test if we can create modelelements with the names given.
     */
    public void testGetMetaModelName() {
        CheckUMLModelHelper.metaModelNameCorrect(
                Model.getExtensionMechanismsFactory(),
		TestExtensionMechanismsFactory.getAllModelElements());
    }

    /**
     * Test if we can create a valid stereotype for all the modelelements.
     */
    public void testIsValidStereoType() {
        CheckUMLModelHelper.isValidStereoType(
                Model.getExtensionMechanismsFactory(),
		TestExtensionMechanismsFactory.getAllModelElements());
    }
}
