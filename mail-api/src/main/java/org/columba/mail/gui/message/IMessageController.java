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
package org.columba.mail.gui.message;

import org.columba.core.context.semantic.api.IContextListener;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.IMailbox;

/**
 * @author fdietz
 * 
 */
public interface IMessageController {

	/**
	 * Show message in messages viewer.
	 * <p>
	 * Should be called in Command.execute() or in another background thread.
	 * 
	 * @param folder
	 *            selected folder
	 * @param uid
	 *            selected message UID
	 * @throws Exception
	 */
	void showMessage(IMailbox folder, Object uid) throws Exception;

	/**
	 * Revalidate message viewer components.
	 * <p>
	 * Call this method after showMessage() to force a repaint():
	 * 
	 */
	void updateGUI() throws Exception;

	public IMailFolderCommandReference getSelectedReference();

	public IMailbox getSelectedFolder();

	public Object getSelectedMessageId();

	public void addMessageSelectionListener(IMessageSelectionListener l);

	public void removeMessageSelectionListener(IMessageSelectionListener l);

	public void clear();

	/**
	 * Return text.
	 * 
	 * @return
	 */
	public String getText();

	/**
	 * Return selected text
	 * 
	 * @return
	 */
	public String getSelectedText();

	/**
	 * Sets the position of the text insertion caret for the TextComponent. Note
	 * that the caret tracks change, so this may move if the underlying text of
	 * the component is changed. If the document is null, does nothing. The
	 * position must be between 0 and the length of the component's text or else
	 * an exception is thrown.
	 * 
	 * @param position
	 */
	public void setCaretPosition(int position);

	/**
	 * Moves the caret to a new position, leaving behind a mark defined by the
	 * last time setCaretPosition was called. This forms a selection. If the
	 * document is null, does nothing. The position must be between 0 and the
	 * length of the component's text or else an exception is thrown.
	 * 
	 * @param position
	 */
	public void moveCaretPosition(int position);

}