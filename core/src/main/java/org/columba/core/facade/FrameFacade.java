package org.columba.core.facade;

import org.columba.api.gui.frame.IFrameManager;
import org.columba.core.gui.frame.FrameManager;

public class FrameFacade {

	public IFrameManager getFrameManager() {
		return (IFrameManager) FrameManager.getInstance();
	}
}
