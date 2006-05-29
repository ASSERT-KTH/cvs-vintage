/**
 * Copyright 2005, 2006 ToolCafe, Inc. All rights reserved.
 */
package org.columba.mail.gui.composer;

import java.util.List;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.frapuccino.addresscombobox.PatternSeparatedAutoCompleter;
import org.frapuccino.addresscombobox.TextParser;

/**
 * @author Rick Horowitz
 *
 */
public class EmailNameAutoCompleter extends PatternSeparatedAutoCompleter {

	/**
	 * @param comp
	 * @param completionList
	 * @param separatorPattern
	 * @param ignoreCase
	 */
	public EmailNameAutoCompleter(JTextComponent comp, List completionList, Pattern separatorPattern, boolean ignoreCase) {
		super(comp, completionList, separatorPattern, ignoreCase);
	}

	/* (non-Javadoc)
	 * @see org.frapuccino.addresscombobox.PatternSeparatedAutoCompleter#acceptedListItem(java.lang.Object)
	 */
	@Override
	protected void acceptedListItem(Object selected) {
		if (selected == null)
			return;

		int caret = textComp.getCaretPosition();
		String value = TextParser.getItemAt(textComp.getText(), separatorPattern, textComp
				.getCaretPosition());

		try {
			Document doc = textComp.getDocument();
			// Remove leading space after the separator character so that it is not removed from the text component's document, below. 
			value = value.trim(); 
			int startingPos = caret - value.length();
			doc.remove(startingPos, value.length());
			
			// Surround the selected element with double-quotes, if necessary
			String selectedStr = selected.toString();
			String sep = separatorPattern.toString();
			if (selectedStr.contains(sep))
				selectedStr = '"' + selectedStr + '"';
			textComp.getDocument().insertString(startingPos, selectedStr + separatorPattern.toString() +" ", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		popup.setVisible(false);
	}

}
