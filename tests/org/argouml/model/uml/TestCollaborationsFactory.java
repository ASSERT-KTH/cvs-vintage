// $Id: TestCollaborationsFactory.java,v 1.7 2005/08/12 19:30:26 mvw Exp $
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

import java.lang.ref.WeakReference;

import junit.framework.TestCase;

import org.argouml.model.CollaborationsFactory;
import org.argouml.model.Model;

/**
 * Test the collaborations factory in the NSUML implementation.<p>
 *
 * No imports from org.argouml.model package!
 */
public class TestCollaborationsFactory extends TestCase {

    /**
     * All the ModelElements we are going to test.
     */
    private static String[] allModelElements =
    {
	"AssociationEndRole",
	"AssociationRole",
	"ClassifierRole",
	"Collaboration",
	"Interaction",
	"Message",
    };

    /**
     * The constructor.
     *
     * @param n the name
     */
    public TestCollaborationsFactory(String n) {
        super(n);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() {
    		//this should instantiate a new implementation
    		Model.getFacade();
    }

    /**
     * Test the creation of the elements.
     */
    public void testCreates() {

        String[] objs = {
	    "AssociationEndRole",
	    "AssociationRole",
	    "ClassifierRole",
	    "Collaboration",
	    "Interaction",
	    "Message",
	    null,
	};

        CheckNSUMLModelHelper.createAndRelease(
            this,
            Model.getCollaborationsFactory(),
            objs);

    }

    /**
     * Test for deletion.
     */
    public void testDeleteComplete() {
        CheckNSUMLModelHelper.deleteComplete(
            this,
            Model.getCollaborationsFactory(),
            allModelElements);
    }

    /**
     * Test to check whether elements which are attached to a
     * ClassifierRole get deleted upon deletion of the
     * ClassifierRole. These elements are Interaction, Message,
     * AssociationRole.
     */
    public void testDeleteClassifierRole() {
        Object model = Model.getModelManagementFactory().createModel();

        CollaborationsFactory collabFactory = Model.getCollaborationsFactory();
        Object collab = collabFactory.buildCollaboration(model);
        Object cr1 = collabFactory.createClassifierRole();
        Object cr2 = collabFactory.createClassifierRole();
        Object role = collabFactory.buildAssociationRole(cr1, cr2);
        Object inter = collabFactory.buildInteraction(collab);
        Object mes = collabFactory.buildMessage(inter, role);

        WeakReference cr1wr = new WeakReference(cr1);
        WeakReference rolewr = new WeakReference(role);
        WeakReference interwr = new WeakReference(inter);
        WeakReference meswr = new WeakReference(mes);

        Model.getUmlFactory().delete(cr1);
        cr1 = null;
        role = null;
        inter = null;
        mes = null;
        System.gc();
        assertNull("ClassifierRole not removed", cr1wr.get());
        assertNull("AssociationRole not removed", rolewr.get());
        // Interaction should not be removed when removing
        // classifierrole...  maybe if the last message is removed
        // from the interaction but even then it's doubtfull since it
        // will probably lead to backward compatibility problems in
        // save formats.
        // assertNull("Interaction not removed", interwr.get());
        assertNull("Message not removed", meswr.get());

    }

    /**
     * @return Returns the allModelElements.
     */
    static String[] getAllModelElements() {
        return allModelElements;
    }
}
