/*
 * Created on 05.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.frame;

import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.toolbar.ToolBar;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class AbstractSingleFrameView extends AbstractFrameView {

	public AbstractSingleFrameView(AbstractFrameController frameController)
	{
		super(frameController);
	}
	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameView#createMenu(org.columba.core.gui.frame.AbstractFrameController)
	 */
	protected abstract Menu createMenu(AbstractFrameController controller);

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameView#createToolbar(org.columba.core.gui.frame.AbstractFrameController)
	 */
	protected abstract ToolBar createToolbar(AbstractFrameController controller);

	

}
