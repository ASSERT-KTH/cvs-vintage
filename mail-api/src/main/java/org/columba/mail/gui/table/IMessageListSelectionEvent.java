package org.columba.mail.gui.table;

import java.util.List;

public interface IMessageListSelectionEvent {

	public Object getSource();
	public List<String> getMessageIds();
}
