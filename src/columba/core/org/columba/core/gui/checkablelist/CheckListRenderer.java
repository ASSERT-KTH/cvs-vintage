//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.gui.checkablelist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;


/**
 * Renderer using a JCheckBox and JLabel.
 * 
 * @author fdietz
 */
class CheckListRenderer extends JPanel implements ListCellRenderer {
    private JCheckBox check;
    protected TreeLabel label;

    //private JLabel label;
    public CheckListRenderer() {
        check = new JCheckBox();

        check.setBackground(UIManager.getColor("Tree.textBackground"));

        //label = new JLabel();
        label = new TreeLabel();

        label.setForeground(UIManager.getColor("Tree.textForeground"));

        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
        add(check, BorderLayout.WEST);
    }

    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean hasFocus) {
        setEnabled(list.isEnabled());
        check.setSelected(((CheckableItem) value).isSelected());
        label.setFont(list.getFont());
        label.setText(value.toString());
        label.setSelected(isSelected);
        label.setFocus(hasFocus);

        Icon icon = ((CheckableItem) value).getIcon();

        if (icon != null) {
            label.setIcon(icon);
        }

        return this;
    }

    public Dimension getPreferredSize() {
        Dimension checkDimension = check.getPreferredSize();
        Dimension checkLabel = label.getPreferredSize();

        return new Dimension(checkDimension.width + checkLabel.width,
            ((checkDimension.height < checkLabel.height) ? checkLabel.height
                                                         : checkDimension.height));
    }

    public void doLayout() {
        Dimension checkDimension = check.getPreferredSize();
        Dimension labelDimension = label.getPreferredSize();
        int labelY = 0;
        int y_label = 0;

        if (checkDimension.height < labelDimension.height) {
            labelY = (labelDimension.height - checkDimension.height) / 2;
        } else {
            y_label = (checkDimension.height - labelDimension.height) / 2;
        }

        check.setLocation(0, labelY);
        check.setBounds(0, labelY, checkDimension.width, checkDimension.height);
        label.setLocation(checkDimension.width, y_label);
        label.setBounds(checkDimension.width, y_label, labelDimension.width,
            labelDimension.height);
    }

    public void setBackground(Color color) {
        if (color instanceof ColorUIResource) {
            color = null;
        }

        super.setBackground(color);
    }

    public class TreeLabel extends JLabel {
        boolean isSelected;
        boolean hasFocus;

        public TreeLabel() {
        }

        public void setBackground(Color color) {
            if (color instanceof ColorUIResource) {
                color = null;
            }

            super.setBackground(color);
        }

        public void paint(Graphics g) {
            String str;

            if ((str = getText()) != null) {
                if (0 < str.length()) {
                    if (isSelected) {
                        g.setColor(UIManager.getColor(
                                "Tree.selectionBackground"));
                    } else {
                        g.setColor(UIManager.getColor("Tree.textBackground"));
                    }

                    Dimension d = getPreferredSize();
                    int imageOffset = 0;
                    Icon currentI = getIcon();

                    if (currentI != null) {
                        imageOffset = currentI.getIconWidth() +
                            Math.max(0, getIconTextGap() - 1);
                    }

                    g.fillRect(imageOffset, 0, d.width - 1 - imageOffset,
                        d.height);

                    if (hasFocus) {
                        g.setColor(UIManager.getColor(
                                "Tree.selectionBorderColor"));
                        g.drawRect(imageOffset, 0, d.width - 1 - imageOffset,
                            d.height - 1);
                    }
                }
            }

            super.paint(g);
        }

        public Dimension getPreferredSize() {
            Dimension retDimension = super.getPreferredSize();

            if (retDimension != null) {
                retDimension = new Dimension(retDimension.width + 3,
                        retDimension.height);
            }

            return retDimension;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public void setFocus(boolean hasFocus) {
            this.hasFocus = hasFocus;
        }
    }
}
