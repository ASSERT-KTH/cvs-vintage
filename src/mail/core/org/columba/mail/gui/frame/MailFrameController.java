package org.columba.mail.gui.frame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import org.columba.core.config.Config;
import org.columba.core.gui.FrameController;
import org.columba.core.gui.statusbar.ImageSequenceTimer;
import org.columba.core.gui.util.DialogStore;
import org.columba.main.MainInterface;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.mh.CachedMHFolder;
import org.columba.mail.folder.outbox.OutboxFolder;
import org.columba.mail.gui.action.GlobalActionCollection;
import org.columba.mail.gui.attachment.AttachmentController;
import org.columba.mail.gui.frame.action.FrameActionListener;
import org.columba.mail.gui.header.HeaderController;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.tree.TreeController;
import org.columba.mail.gui.tree.util.FolderInfoPanel;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MailFrameController extends FrameController {

	protected FrameView view;
	//public SelectionManager selectionManager;

	public TreeController treeController;
	public TableController tableController;
	public MessageController messageController;
	public AttachmentController attachmentController;
	public HeaderController headerController;
	public FilterToolbar filterToolbar;

	public FolderInfoPanel folderInfoPanel;

	private FrameActionListener actionListener;
	private MailToolBar toolBar;
	private MailMenu menu;

	public GlobalActionCollection globalActionCollection;
	
	

	public MailFrameController() {
		super();

		view = new FrameView();

		new DialogStore(view);
		
		

		globalActionCollection = new GlobalActionCollection(this);

		actionListener = new FrameActionListener(this);

		//selectionManager = new SelectionManager();

		treeController = new TreeController(this, MainInterface.treeModel);
		//treeController.setSelectionManager(selectionManager);

		tableController = new TableController(this);
		treeController.getTreeSelectionManager().addFolderSelectionListener(
			tableController.getTableSelectionManager());
		//tableController.setSelectionManager(selectionManager);

		messageController = new MessageController(this);
		tableController.getTableSelectionManager().addMessageSelectionListener(
			messageController);
		//messageController.setSelectionManager( selectionManager);

		attachmentController = new AttachmentController(this);

		//attachmentController.setSelectionManager( selectionManager);

		headerController = new HeaderController();

		folderInfoPanel = new FolderInfoPanel();
		view.setFolderInfoPanel(folderInfoPanel);

		toolBar = new MailToolBar(this);
		view.setToolBar(toolBar);

		filterToolbar = new FilterToolbar(tableController);

		globalActionCollection.addActionListeners();

		view.init(
			treeController.getView(),
			tableController.getView(),
			filterToolbar,
			headerController.getView(),
			messageController.getView(),
			attachmentController.getView(),
			statusBar);

		menu = new MailMenu(this);
		view.setJMenuBar(menu);

		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				/*
				ExitWorker worker = new ExitWorker();
				//worker.register(MainInterface.taskManager);
				worker.start();
				*/

				close();

			}
		});

		view.pack();

		int count = MailConfig.getAccountList().count();
		if (count == 0) {
			view.maximize();
		} else {

			java.awt.Dimension dim =
				MailConfig
					.getMainFrameOptionsConfig()
					.getWindowItem()
					.getDimension();
			view.setSize(dim);
		}
		view.setVisible(true);

	}

	public void close() {
		view.saveWindowPosition();

		tableController.saveColumnConfig();

		Config.save();

		MainInterface.popServerCollection.saveAll();

		saveAllFolders();

		System.exit(1);

	}

	public MailMenu getMenu() {
		return menu;
	}

	public FrameView getView() {
		return view;
	}

	public FrameActionListener getActionListener() {
		return actionListener;
	}

	protected void changeToolbars() {
		boolean folderInfo =
			MailConfig
				.getMainFrameOptionsConfig()
				.getWindowItem()
				.isShowFolderInfo();
		boolean toolbar =
			MailConfig
				.getMainFrameOptionsConfig()
				.getWindowItem()
				.isShowToolbar();

		if (toolbar == true) {

			getView().hideToolbar(folderInfo);
			MailConfig
				.getMainFrameOptionsConfig()
				.getWindowItem()
				.setShowToolbar(
				"false");
		} else {

			getView().showToolbar(folderInfo);
			MailConfig
				.getMainFrameOptionsConfig()
				.getWindowItem()
				.setShowToolbar(
				"true");
		}

		if (folderInfo == true) {

			getView().hideFolderInfo(toolbar);
			MailConfig
				.getMainFrameOptionsConfig()
				.getWindowItem()
				.setShowFolderInfo(
				"false");
		} else {

			getView().showFolderInfo(toolbar);
			MailConfig
				.getMainFrameOptionsConfig()
				.getWindowItem()
				.setShowFolderInfo(
				"true");
		}

	}

	public void saveAllFolders() {
		FolderTreeNode rootFolder =
			(FolderTreeNode) MainInterface.treeModel.getRoot();

		saveFolder(rootFolder);
	}

	public void saveFolder(FolderTreeNode parentFolder) {

		int count = parentFolder.getChildCount();
		FolderTreeNode child;
		FolderTreeNode folder;

		for (Enumeration e = parentFolder.children(); e.hasMoreElements();) {

			child = (FolderTreeNode) e.nextElement();

			if (child instanceof CachedMHFolder) {
				CachedMHFolder mhFolder = (CachedMHFolder) child;
				try {
					mhFolder.save();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (child instanceof OutboxFolder) {
				OutboxFolder outboxFolder= (OutboxFolder) child;
				try {
					outboxFolder.save();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (child instanceof IMAPFolder) {
				IMAPFolder imapFolder = (IMAPFolder) child;

				try {
					imapFolder.save();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			saveFolder(child);
		}
	}

}
