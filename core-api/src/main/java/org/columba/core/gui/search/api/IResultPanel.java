package org.columba.core.gui.search.api;

import javax.swing.JComponent;

import org.columba.api.plugin.IExtensionInterface;
import org.columba.core.search.api.IResultListener;

public interface IResultPanel extends IExtensionInterface, IResultListener {

	public String getSearchCriteriaTechnicalName();

	public String getProviderTechnicalName();

	public JComponent getView();

}
