// $Id: TestActionUseCaseDiagram.java,v 1.2 2003/05/11 14:13:51 linus Exp $
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

package org.argouml.uml.ui;

/**
 * Test for actionusecasediagram.
 * @author jaap.branderhorst@xs4all.nl	
 * @since Jan 9, 2003
 */
public class TestActionUseCaseDiagram
    extends GUITestActionUseCaseDiagram
{

    /**
     * Constructor
     * @param arg0 test case name.
     */
    public TestActionUseCaseDiagram(String arg0) {
        super(arg0);
    }

    /**
     * Disable the test that doesn't work without head.
     */
    public void testCreateDiagram() { }

    /**
     * Disable the test that doesn't work without head.
     */
    public void testDifferentNames() { }

    /**
     * Disable the test that doesn't work without head.
     */
    // public void testValidTestNamespace() { }

    /**
     * Disable the test that doesn't work without head.
     */
    // public void testValidNamespaces() { }

}
