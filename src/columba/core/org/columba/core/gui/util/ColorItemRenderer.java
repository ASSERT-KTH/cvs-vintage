/*
 * Created on 2003-nov-01
 */
package org.columba.core.gui.util;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * A <code>JComboBox</code> item renderer.
 *
 * @author redsolo
 */
public class ColorItemRenderer extends JLabel implements ListCellRenderer {
    /**
     * Creates a <code>ColorItemRenderer</code>.
     */
    public ColorItemRenderer() {
        /*setIconTextGap(5);
        setVerticalAlignment(JLabel.CENTER);*/
    }

    /** {@inheritDoc} */
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        if ((value != null) && (value instanceof ColorItem)) {
            ColorItem color = (ColorItem) value;
            setText(color.getName());
            setIcon(color.getIcon());
        } else {
            setText("Internal error");
            setIcon(null);
        }

        return this;
    }
}
