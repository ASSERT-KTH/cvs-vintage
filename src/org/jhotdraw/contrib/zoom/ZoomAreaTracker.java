/*
 * File:   ZoomAreaTracker.java
 * Author: Andre Spiegel <spiegel@gnu.org>
 *
 * $Id: ZoomAreaTracker.java,v 1.1 2002/04/30 20:40:06 mrfloppy Exp $
 */

package CH.ifa.draw.contrib.zoom;

import CH.ifa.draw.framework.DrawingEditor;

import java.awt.*;
import java.awt.event.MouseEvent;

public class ZoomAreaTracker extends AreaTracker {

	public ZoomAreaTracker(DrawingEditor editor) {
		super(editor);
	}

	public void mouseUp(MouseEvent e, int x, int y) {
		Rectangle zoomArea = getArea();
		super.mouseUp(e, x, y);
		if (zoomArea.width > 4 && zoomArea.height > 4)
			((ZoomDrawingView) view()).zoom(zoomArea.x, zoomArea.y,
					zoomArea.width, zoomArea.height);
	}

}


