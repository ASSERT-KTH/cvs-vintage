/*
 * Created on 2003-nov-01
 */
package org.columba.mail.gui.config.filter.plugins;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;

import org.columba.core.gui.util.ColorFactory;
import org.columba.core.gui.util.ColorItem;
import org.columba.core.gui.util.ColorItemRenderer;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.gui.config.filter.ActionList;

/**
 * A configuration panel for the <code>ColorMessageFilterAction</code>
 * This displays a <code>JComboBox</code> filled with different colors.
 *  
 * @author redsolo
 */
public class ColorActionConfig extends DefaultActionRow implements ActionListener {

	private JComboBox colorsComboBox;
	
	/**
	 * @param list the action list (?)
	 * @param action the action to configure.
	 */
	public ColorActionConfig(ActionList list, FilterAction action) {
		super(list, action);
	}

	/** {@inheritDoc} */
	public void initComponents() {
		super.initComponents();
		colorsComboBox = new JComboBox();

		// Add the default colors items.
		colorsComboBox.addItem( new ColorItem( Color.black, "Black"));
		colorsComboBox.addItem( new ColorItem( Color.blue, "Blue"));
		colorsComboBox.addItem( new ColorItem( Color.gray, "Gray"));
		colorsComboBox.addItem( new ColorItem( Color.green, "Green"));
		colorsComboBox.addItem( new ColorItem( Color.red, "Red"));
		colorsComboBox.addItem( new ColorItem( Color.yellow, "Yellow"));
	
		// Add the custom color item.
		int rgb = getFilterAction().getInteger("rgb", Color.black.getRGB());
		colorsComboBox.addItem(new ColorItem( ColorFactory.getColor(rgb), "Custom"));		
		
		ColorItemRenderer renderer = new ColorItemRenderer();
		colorsComboBox.setRenderer(renderer);
		
		addComponent(colorsComboBox);
	}
	
	/** {@inheritDoc} */
	public void updateComponents(boolean b) {
		super.updateComponents(b);
		
		if (b) {	
			ComboBoxModel comboBoxModel = colorsComboBox.getModel();
			String string = getFilterAction().get("color");		
			
			if (string == null) {
				colorsComboBox.setSelectedIndex(0);
				
			} else {
				for (int i = 0; i < comboBoxModel.getSize(); i++) {
					ColorItem object = (ColorItem) comboBoxModel.getElementAt(i);
					if ( object.getName().equalsIgnoreCase( string )) {
						colorsComboBox.setSelectedIndex(i);
						break;
					}
				}
			}			
			colorsComboBox.addActionListener(this);
			
		} else {			
			ColorItem object = (ColorItem) colorsComboBox.getSelectedItem();
			if (object != null) {
				getFilterAction().set("color", object.getName());
				getFilterAction().set("rgb", object.getColor().getRGB());
			}			
		}
	}

	/** {@inheritDoc} */
	public void actionPerformed(ActionEvent e) {
		ColorItem item = (ColorItem) colorsComboBox.getSelectedItem();
		if (item.getName().equalsIgnoreCase("custom")) {
			Color newColor = JColorChooser.showDialog(
								 null,
								 "Choose Background Color",
								 item.getColor());
			item.setColor( newColor );
			colorsComboBox.repaint();
		}
	}
}