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
package org.columba.mail.gui.composer.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FieldCellEditor extends DefaultCellEditor 
{
	JComboBox comboBox;
	AddressbookTableView table;
	
	public FieldCellEditor( JComboBox comboBox, AddressbookTableView table ) {
		
		super(comboBox);
		
		this.comboBox = (JComboBox) comboBox;
		this.table = table;
		
		
		editorComponent = comboBox;
		setClickCountToStart(1); //This is usually 1 or 2.


			
		//Must do this so that editing stops when appropriate.
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("fieldcelleditor->fireEditingStopped");
				
				
				
				fireEditingStopped();
				
				
			}
		});
		
	}
	
	protected void fireEditingStopped() {
		super.fireEditingStopped();
		
		table.appendRow();
		
		
	}

	public Object getCellEditorValue() {
		return comboBox.getSelectedItem();
	}

	public Component getTableCellEditorComponent(
		JTable table,
		Object value,
		boolean isSelected,
		int row,
		int column) {
		
		comboBox.setSelectedItem( value.toString() );
		
		return editorComponent;
	}
	
	

}
