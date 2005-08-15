// $Id: TestUmlFactoryBuildNode.java,v 1.1 2005/08/15 21:10:30 linus Exp $
// Copyright (c) 2005 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
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

package org.argouml.model;

import junit.framework.TestCase;

/**
 * Checks that the {@link UmlFactory#buildNode(Object)} method works with
 * all conceivable alternatives.
 */
public class TestUmlFactoryBuildNode extends TestCase {
    /**
     * Testing Core elements.
     */
    public void testBuildCoreNodes() {
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getUMLClass()));
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getModel()));
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getPackage()));
        // ...
    }

    // ActivityGraphs

    /**
     * Testing Use Cases elements.
     */
    public void testBuildUseCasesNodes() {
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getActor()));
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getUseCase()));
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getPackage()));
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getClassifier()));
        // ...
    }

    /**
     * Tests for StateMachines elements.
     */
    public void testBuildStateMachineNodes() {
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getCompositeState()));
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getFinalState()));
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getPseudostate()));
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getState()));
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getSynchState()));
        // ...
    }

    /**
     * Tests for Collaborations elements.
     */
    public void testBuildCollaborationsNodes() {
        assertNotNull(Model.getUmlFactory().buildNode(
                Model.getMetaTypes().getUMLClass()));
        // ...
    }

    // CommonBehaviorFactory
    // DataTypesFactory
    // ExtensionMechanismsFactory
    // ModelManagementFactory
}
