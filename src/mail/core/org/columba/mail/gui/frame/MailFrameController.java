package org.columba.mail.gui.frame;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.FrameController;
import org.columba.core.gui.FrameView;
import org.columba.core.gui.util.DialogStore;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.action.GlobalActionCollection;
import org.columba.mail.gui.attachment.AttachmentController;
import org.columba.mail.gui.composer.HeaderController;
import org.columba.mail.gui.frame.action.FrameActionListener;
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

	protected MailFrameView view;
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

	
	
	public MailFrameController( String id ) {
		super(id);
		
		
		
		

		new DialogStore(view);

		globalActionCollection = new GlobalActionCollection(this);

		actionListener = new FrameActionListener(this);
		
		createView();

		//selectionManager = new SelectionManager();

	}

	public void createView() {

		view = new MailFrameView(this);
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
		tableController.getTableSelectionManager().addMessageSelectionListener(
		attachmentController.getAttachmentSelectionManager());

		//attachmentController.setSelectionManager( selectionManager);

		//headerController = new HeaderController();

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
			messageController.getView(),
			attachmentController.getView(),
			statusBar);

		menu = new MailMenu(this);
		view.setJMenuBar(menu);

		/*
		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				
				close();

			}
		});
		*/

		view.pack();

		/*
		int count = MailConfig.getAccountList().count();
		if (count == 0) {
			view.maximize();
		} else {

			ViewItem viewItem =
				MailConfig.getMainFrameOptionsConfig().getViewItem();

			int x = viewItem.getInteger("window", "width");
			int y = viewItem.getInteger("window", "height");
			Dimension dim = new Dimension(x, y);
			view.setSize(dim);
		
		}
		*/
		//view.setVisible(true);
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
		ViewItem item = MailConfig.getMainFrameOptionsConfig().getViewItem();

		boolean folderInfo = item.getBoolean("toolbars", "show_folderinfo");
		boolean toolbar = item.getBoolean("toolbars", "show_main");

		if (toolbar == true) {

			((MailFrameView)getView()).hideToolbar(folderInfo);
			item.set("toolbars", "show_main", false);
		} else {

			((MailFrameView)getView()).showToolbar(folderInfo);
			item.set("toolbars", "show_main", true);
		}

		if (folderInfo == true) {

			((MailFrameView)getView()).hideFolderInfo(toolbar);
			item.set("toolbars", "show_folderinfo", false);
		} else {

			((MailFrameView)getView()).showFolderInfo(toolbar);
			item.set("toolbars", "show_folderinfo", true);
		}

	}
	
	public void close()
		{
			ColumbaLogger.log.info("closing MailFrameController");
			
			tableController.saveColumnConfig();
			super.close();
			
		}

}
