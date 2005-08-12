// $Id: TestUseCasesFactory.java,v 1.9 2005/08/12 19:30:26 mvw Exp $
// Copyright (c) 2002-2005 The Regents of the University of California. All
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

import junit.framework.TestCase;

import org.argouml.model.CheckUMLModelHelper;
import org.argouml.model.Model;



/**
 * Test the UseCasesFactory class.
 *
 */
public class TestUseCasesFactory extends TestCase {
    /**
     * List of elements to test.
     */
    private static String[] allModelElements = {
        "Actor",
        "Extend",
        "ExtensionPoint",
        "Include",
        "UseCase",
        "UseCaseInstance",
    };

    /**
     * The constructor.
     *
     * @param n the name of the test
     */
    public TestUseCasesFactory(String n) { super(n); }



    /**
     * Test if the UseCasesFactory is really a singleton.
     */
    public void testSingleton() {
	Object o1 = Model.getUseCasesFactory();
	Object o2 = Model.getUseCasesFactory();
	assertTrue("Different singletons", o1 == o2);
    }

    /**
     * Test creation.
     */
    public void testCreates() {
	String [] objs = {
	    "Actor",
	    "Extend",
	    "ExtensionPoint",
	    "Include",
	    "UseCase",
	    "UseCaseInstance",
	    null,
	};

	CheckUMLModelHelper.createAndRelease(Model.getUseCasesFactory(),
					     objs);
    }

    /**
     * Test building extensions.
     */
    public void testBuildExtend1() {
        Object base = Model.getUseCasesFactory().createUseCase();
        Object extension = Model.getUseCasesFactory().createUseCase();
        Object point = Model.getUseCasesFactory()
            	.buildExtensionPoint(base);
        Object extend = Model.getUseCasesFactory()
            	.buildExtend(base, extension, point);
        assertTrue("extensionpoint not added to base",
		   !Model.getFacade().getExtensionPoints(base).isEmpty());
        //TODO: Check that extended use cases are getExtends2 (else use getExtendingUseCases)
        assertTrue("extend not added to base", !Model.getUseCasesHelper().getExtendedUseCases(base).isEmpty());
        //TODO: Check that this is ok to use getExtends, and not getExtends2
        assertTrue("extend not added to extension",
		   !Model.getFacade().getExtends(extension).isEmpty());
        assertTrue("extend not added to correct extensionpoint",
		   (Model.getFacade().getExtensionPoints(extend).contains(point)
		    && Model.getFacade().getExtensionPoints(extend).size() == 1));
    }



    /**
     * @return Returns the allModelElements.
     */
    static String[] getAllModelElements() {
        return allModelElements;
    }
}

