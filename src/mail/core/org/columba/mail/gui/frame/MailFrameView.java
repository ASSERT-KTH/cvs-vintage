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

package org.columba.mail.gui.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.FrameController;
import org.columba.core.gui.FrameView;
import org.columba.core.gui.statusbar.StatusBar;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.attachment.AttachmentView;
import org.columba.mail.gui.composer.HeaderView;
import org.columba.mail.gui.frame.util.SplitPane;
import org.columba.mail.gui.message.MessageView;
import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.table.TableView;
import org.columba.mail.gui.tree.TreeView;
import org.columba.mail.gui.tree.util.FolderInfoPanel;

public class MailFrameView extends FrameView {
	private MailToolBar toolbar;

	//private StatusBar statusBar;
	public JSplitPane mainSplitPane;
	public JSplitPane rightSplitPane;

	private JPanel pane;

	private FolderInfoPanel folderInfoPanel;

	public ResourceBundle guiLabels;

	private JPanel tablePanel;

	FilterToolbar filterToolbar;
	HeaderView header;

	public MailFrameView( FrameController frameController ) {
		super( frameController );
		
		//MainInterface.mainFrame = this;
		

		//changeToolbars();
		//MainInterface.frameModel.register(this);

	}

	

	/*
	public void showAttachmentViewer() {
		rightSplitPane.showAttachmentViewer();
	}

	public void hideAttachmentViewer() {
		rightSplitPane.hideAttachmentViewer();
	}
	*/
	
	public void setFolderInfoPanel(FolderInfoPanel f) {
		this.folderInfoPanel = f;
	}

	public void init(
		TreeView tree,
		TableView table,
		FilterToolbar filterToolbar,
		MessageView message,
		StatusBar statusBar) {

		this.filterToolbar = filterToolbar;

		
		

		//this.statusBar = statusBar;

		mainSplitPane = new JSplitPane();
		//mainSplitPane.setDividerSize(5);

		//mainSplitPane.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));

		this.getContentPane().add(mainSplitPane, BorderLayout.CENTER);

		mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

		mainSplitPane.add(new JScrollPane(tree), JSplitPane.LEFT);

		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		//messagePanel.add( header, BorderLayout.NORTH );
		messagePanel.add(message, BorderLayout.CENTER);

		tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());

		ViewItem item = MailConfig.getMainFrameOptionsConfig().getViewItem();
		

		if (item.getBoolean("toolbars", "show_filter") == true)
			tablePanel.add(filterToolbar, BorderLayout.NORTH);

		JScrollPane tableScrollPane = new JScrollPane(table);
		
		
		tableScrollPane.getViewport().setBackground(Color.white);
		tablePanel.add(tableScrollPane, BorderLayout.CENTER);
		rightSplitPane =
			new JSplitPane();
		rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		rightSplitPane.add(tablePanel, JSplitPane.LEFT);
		rightSplitPane.add(messagePanel, JSplitPane.RIGHT);
			
		mainSplitPane.add(rightSplitPane, JSplitPane.RIGHT);

		pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		// same as menu

		if (item.getBoolean("toolbars", "show_main") == true)
			pane.add(toolbar);

		if (item.getBoolean("toolbars", "show_folderinfo") == true)
			pane.add(folderInfoPanel);

		getContentPane().add(pane, BorderLayout.NORTH);

		int count = MailConfig.getAccountList().count();

		if (count == 0) {
			pack();
			rightSplitPane.setDividerLocation(150);
		} else {
			mainSplitPane.setDividerLocation(
				item.getInteger("splitpanes", "main"));

			rightSplitPane.setDividerLocation(
				item.getInteger("splitpanes", "header"));
		}

	}

	public void setToolBar(MailToolBar toolBar) {
		this.toolbar = toolBar;
	}

	public void hideToolbar(boolean b) {
		pane.remove(toolbar);

		validate();
		repaint();

	}

	public void showToolbar(boolean b) {

		if (b) {
			pane.removeAll();
			pane.add(toolbar);
			pane.add(folderInfoPanel);

			validate();
			repaint();
		} else {

			pane.add(toolbar);
			validate();
			repaint();
		}

	}

	public void hideFolderInfo(boolean b) {

		pane.remove(folderInfoPanel);
		validate();
		repaint();

	}

	public void showFolderInfo(boolean b) {

		if (b) {
			pane.removeAll();
			pane.add(toolbar);
			pane.add(folderInfoPanel);

			validate();
			repaint();
		} else {

			pane.add(folderInfoPanel);

			validate();
			repaint();
		}

	}

	public void showFilterToolbar() {
		tablePanel.add(filterToolbar, BorderLayout.NORTH);
		tablePanel.validate();
		repaint();
	}

	public void hideFilterToolbar() {
		tablePanel.remove(filterToolbar);
		tablePanel.validate();
		repaint();
	}

	public void saveWindowPosition(ViewItem viewItem) {

		super.saveWindowPosition(viewItem);

		viewItem.set("splitpanes", "main", mainSplitPane.getDividerLocation());
		viewItem.set(
			"splitpanes",
			"header",
			rightSplitPane.getDividerLocation());

	}

	

}