package org.columba.core.gui.contextualpanel.api;

import javax.swing.JComponent;


public interface IContextualPanel {

	public void search();
	
	public JComponent getView();

	public void register(IContextualProvider provider);
	public void unregister(IContextualProvider provider);
}
