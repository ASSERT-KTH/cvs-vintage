package org.columba.core.gui.context.api;

import javax.swing.JComponent;


public interface IContextualPanel {

	public void search();
	
	public JComponent getView();

	public void register(IContextProvider provider);
	public void unregister(IContextProvider provider);
}
