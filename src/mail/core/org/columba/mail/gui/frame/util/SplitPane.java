// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.frame.util;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

public class SplitPane extends JSplitPane {
	public JSplitPane splitPane = new JSplitPane();
	JComponent header, message, attachment;
	boolean hide = false;
	int last = 0;
	int lastAttach = 0;

	public SplitPane() {
		super();
	}

	public SplitPane(
		JComponent header,
		JComponent message,
		JComponent attachment) {
		super();
		this.header = header;
		this.message = message;
		this.attachment = attachment;

		setBorder(null);
		splitPane.setBorder(null);
		//splitPane.setDividerSize(1);
		//setDividerSize(5);

		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		setOrientation(JSplitPane.VERTICAL_SPLIT);

		setDividerLocation(0.75);

		// this has to be set by themes
		//setDividerSize( 5 );

		setResizeWeight(0.25);

		splitPane.setDividerLocation(0.9);
		splitPane.setResizeWeight(0.9);

		// this has to be set by themes
		//splitPane.setDividerSize( 5 );

		add(header, JSplitPane.TOP);
		add(splitPane, JSplitPane.BOTTOM);
		splitPane.add(message, JSplitPane.TOP);
		splitPane.add(attachment, JSplitPane.BOTTOM);
		//splitPane.resetToPreferredSizes();

		//hideAttachmentViewer();

	}

	public void hideAttachmentViewer() {
		if (hide == true)
			return;

		last = getDividerLocation();
		lastAttach = splitPane.getDividerLocation();

		remove(splitPane);
		remove(header);

		add(header, JSplitPane.TOP);
		add(message, JSplitPane.BOTTOM);

		hide = true;

		setDividerLocation(last);

	}

	public void showAttachmentViewer() {
		if (hide == false)
			return;

		last = getDividerLocation();

		remove(header);
		remove(message);

		splitPane.add(message, JSplitPane.TOP);
		splitPane.add(attachment, JSplitPane.BOTTOM);

		add(header, JSplitPane.TOP);
		add(splitPane, JSplitPane.BOTTOM);

		setDividerLocation(last);
		splitPane.setDividerLocation(lastAttach);

		hide = false;

	}

}