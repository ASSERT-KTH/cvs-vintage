//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
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