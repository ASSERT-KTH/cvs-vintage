// $Id: UMLLinkedList.java,v 1.16 2004/07/31 08:31:57 mkl Exp $
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

// $header$
package org.argouml.uml.ui;

import java.awt.Color;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * An UMLList that implements 'jump' behaviour. As soon as the user
 * doubleclicks on an element in the list, that element is selected in
 * argouml.
 *
 * @since Oct 2, 2002
 * @author jaap.branderhorst@xs4all.nl
 */
public class UMLLinkedList extends UMLList2 implements ListSelectionListener {
    
    private UMLLinkMouseListener _mouseListener;

    /**
     * Constructor for UMLLinkedList.
     *
     * @param dataModel
     */
    public UMLLinkedList(
        UMLModelElementListModel2 dataModel, boolean showIcon) {
        super(dataModel, new UMLLinkedListCellRenderer(showIcon));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setForeground(Color.blue);
        setSelectionForeground(Color.blue.darker());
        _mouseListener = new UMLLinkMouseListener(this);
        addMouseListener(_mouseListener);
        addListSelectionListener(this);
    }
    
    public UMLLinkedList(UMLModelElementListModel2 dataModel) {
        this(dataModel, true);
    }

    /**
     * @see org.argouml.uml.ui.UMLList2#doIt(
     *          javax.swing.event.ListSelectionEvent)
     */
    protected void doIt(ListSelectionEvent e) {
    }
    
    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {       
        super.valueChanged(e);
        _mouseListener.setSelectedValue(getSelectedValue());
    }
}
