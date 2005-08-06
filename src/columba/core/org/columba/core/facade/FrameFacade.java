package org.columba.core.facade;

import org.columba.core.gui.frame.FrameManager;
import org.columba.core.gui.frame.IFrameManager;

public class FrameFacade {

	public IFrameManager getFrameManager() {
		return (IFrameManager) FrameManager.getInstance();
	}
}
