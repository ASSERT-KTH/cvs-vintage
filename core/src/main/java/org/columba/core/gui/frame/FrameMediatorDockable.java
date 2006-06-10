// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.gui.frame;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.columba.api.gui.frame.IDockable;
import org.columba.core.gui.docking.DockableView;

public class FrameMediatorDockable implements IDockable {

	private String id;

	private String name;

	private DockableView comp;

	private JPopupMenu popup;

	public FrameMediatorDockable(String id, String name, JComponent comp,
			JPopupMenu popup) {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (name == null)
			throw new IllegalArgumentException("name == null");
		if (comp == null)
			throw new IllegalArgumentException("comp == null");

		this.id = id;
		this.name = name;
		this.comp = new DockableView(id, name);
		this.comp.setContentPane(comp);

		if (popup != null) {
			this.popup = popup;
			this.comp.setPopupMenu(this.popup);
		}
	}

	public String getId() {
		return id;
	}

	public String resolveName() {
		return name;
	}

	public JComponent getView() {
		return (JComponent) comp.getComponent();
	}

	public JPopupMenu getPopupMenu() {
		return popup;
	}

	public void setPopupMenu(JPopupMenu popup) {
		if (popup == null)
			throw new IllegalArgumentException("popup == null");

		this.popup = popup;
		
		this.comp.setPopupMenu(this.popup);
	}

	public void setTitle(String title) {
		comp.setTitle(title);
	}

}
