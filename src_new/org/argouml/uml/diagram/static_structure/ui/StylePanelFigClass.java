// $Id: StylePanelFigClass.java,v 1.7 2004/08/15 22:39:23 bobtarling Exp $
// Copyright (c) 1996-99 The Regents of the University of California. All
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

package org.argouml.uml.diagram.static_structure.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;

import org.argouml.ui.StylePanelFigNodeModelElement;

/**
 * Stylepanel which adds an attributes and operations checkbox and depends on
 * FigClass.
 * 
 * @see FigClass
 *  
 */
public class StylePanelFigClass extends StylePanelFigNodeModelElement {

    protected JCheckBox _attrCheckBox = new JCheckBox("Attributes");

    protected JCheckBox _operCheckBox = new JCheckBox("Operations");

    protected JLabel _displayLabel = new JLabel("Display: ");

    /**
     * Flag to indicate that a refresh is going on.
     */
    private boolean _refreshTransaction = false;

    ////////////////////////////////////////////////////////////////
    // contructors

    public StylePanelFigClass() {
        super();
        GridBagLayout gb = (GridBagLayout) getLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.ipadx = 0;
        c.ipady = 0;

        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy = 0;
        c.weightx = 0.0;
        gb.setConstraints(_displayLabel, c);
        add(_displayLabel);

        c.gridx = 1;
        c.gridwidth = 1;
        c.gridy = 0;
        c.weightx = 0.0;
        JPanel pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.LEFT));
        pane.add(_attrCheckBox);
        pane.add(_operCheckBox);
        gb.setConstraints(pane, c);
        add(pane);

        _attrCheckBox.setSelected(false);
        _operCheckBox.setSelected(false);
        _attrCheckBox.addItemListener(this);
        _operCheckBox.addItemListener(this);
    }

    /**
     * Only refresh the tab if the bounds propertyChange event arrives.
     * 
     * @see org.argouml.ui.StylePanel#refresh(java.beans.PropertyChangeEvent)
     */
    public void refresh(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        if (propertyName.equals("bounds")) {
            refresh();
        }
    }

    ////////////////////////////////////////////////////////////////
    // accessors

    public void refresh() {
        _refreshTransaction = true;
        super.refresh();
        FigClass tc = (FigClass) _target;
        _attrCheckBox.setSelected(tc.isAttributesVisible());
        _operCheckBox.setSelected(tc.isOperationsVisible());
        _refreshTransaction = false;
    }

    ////////////////////////////////////////////////////////////////
    // event handling

    public void insertUpdate(DocumentEvent e) {
        super.insertUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
        insertUpdate(e);
    }

    public void itemStateChanged(ItemEvent e) {
        if (!_refreshTransaction) {
            Object src = e.getSource();

            if (src == _attrCheckBox) {
                ((FigClass) _target).setAttributesVisible(_attrCheckBox
                        .isSelected());
                markNeedsSave();
            } else if (src == _operCheckBox) {
                ((FigClass) _target).setOperationsVisible(_operCheckBox
                        .isSelected());
                markNeedsSave();
            } else
                super.itemStateChanged(e);
        }
    }

} /* end class StylePanelFigClass */

