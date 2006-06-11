// $Id: GUITestMultiEditorPane.java,v 1.10 2006/06/11 15:43:56 mvw Exp $
// Copyright (c) 1996-2006 The Regents of the University of California. All
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

import junit.framework.TestCase;

/**
 * @author jaap.branderhorst@xs4all.nl
 * @since Apr 13, 2003
 */
public class GUITestMultiEditorPane extends TestCase {

    /**
     * Constructor for TestMultiEditorPane.
     *
     * @param arg0 is the test case name.
     */
    public GUITestMultiEditorPane(String arg0) {
        super(arg0);
    }

    /**
     * Tests the construction of the multieditorpane. Can we construct a
     * multieditorpane and even have an editor in it?
     */
    public void testConstruction() {
	MultiEditorPane pane = new MultiEditorPane();
	assertNotNull(pane);
	assertEquals(pane.getComponents().length, 1);
    }
}
