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
package org.columba.core.gui.plugin;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.columba.api.plugin.PluginMetadata;
import org.columba.core.plugin.PluginManager;


/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

public class DescriptionTreeRenderer extends DefaultTreeCellRenderer {
    /* (non-Javadoc)
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row,
        boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded,
            leaf, row, hasFocus);

        PluginNode node = (PluginNode) value;

        String id = node.getId();
        
        String name = null;
        
        PluginMetadata metadata = PluginManager.getInstance().getPluginMetadata(id);
        
        if ( metadata != null)
        	name = metadata.getName();
        else
        	name = id;
        
        setText(name);

        String tooltip = node.getTooltip();
        setToolTipText(tooltip);

        return this;
    }

    public void paint(Graphics g) {
        Rectangle bounds = g.getClipBounds();
        Font font = getFont();
        FontMetrics fontMetrics = g.getFontMetrics(font);

     

        int iconOffset = 0;

        //int iconOffset = getHorizontalAlignment() + getIcon().getIconWidth() + 1;
        if ((bounds.x == 0) && (bounds.y == 0)) {
            bounds.width -= iconOffset;

            String labelStr = layout(this, fontMetrics, getText(), bounds);
            setText(labelStr);
        }

        super.paint(g);
    }

    private String layout(JLabel label, FontMetrics fontMetrics, String text,
        Rectangle viewR) {
        Rectangle iconR = new Rectangle();
        Rectangle textR = new Rectangle();

        return SwingUtilities.layoutCompoundLabel(fontMetrics, text, null,
            SwingConstants.RIGHT, SwingConstants.RIGHT, SwingConstants.RIGHT,
            SwingConstants.RIGHT, viewR, iconR, textR, 0);
    }
}
