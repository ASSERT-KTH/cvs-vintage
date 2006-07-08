package org.columba.mail.gui.message;

import java.util.EventListener;

public interface IMessageSelectionListener extends EventListener{

	public void selectionChanged(IMessageSelectionEvent event);
}
