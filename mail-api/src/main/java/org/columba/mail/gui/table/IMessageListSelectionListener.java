package org.columba.mail.gui.table;

import java.util.EventListener;

public interface IMessageListSelectionListener extends EventListener {

	public void selectionChanged(IMessageListSelectionEvent e);
}
