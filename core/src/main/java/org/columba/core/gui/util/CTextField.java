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
package org.columba.core.gui.util;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.columba.core.gui.base.UndoDocument;

/**
 * Additionally registers at the FocusManager.
 * 
 * @author fdietz
 */

public class CTextField extends JTextField {
	/**
	 * 
	 */
	public CTextField() {
		super();

		setDocument(new UndoDocument());
	}

	/**
	 * @param arg0
	 */
	public CTextField(String arg0) {
		this();

		setText(arg0);
	}

	/** ****************** FocusOwner implementation ************************* */
	public JComponent getComponent() {
		return this;
	}

	public boolean isCopyActionEnabled() {
		if (getSelectedText() == null) {
			return false;
		}

		if (getSelectedText().length() > 0) {
			return true;
		}

		return false;
	}

	public boolean isCutActionEnabled() {
		if (getSelectedText() == null) {
			return false;
		}

		if (getSelectedText().length() > 0) {
			return true;
		}

		return false;
	}

	public boolean isDeleteActionEnabled() {
		if (getSelectedText() == null) {
			return false;
		}

		if (getSelectedText().length() > 0) {
			return true;
		}

		return false;
	}

	public boolean isPasteActionEnabled() {
		return true;
	}

	public boolean isRedoActionEnabled() {
		// TODO: use UndoableEditEvent to make this really work
		return true;
	}

	public boolean isSelectAllActionEnabled() {
		return true;
	}

	public boolean isUndoActionEnabled() {
		// TODO: use UndoableEditEvent to make this really work
		return true;
	}

	public void delete() {
		replaceSelection("");
	}

	public void redo() {
		((UndoDocument) getDocument()).redo();
	}

	public void undo() {
		((UndoDocument) getDocument()).undo();
	}
}
