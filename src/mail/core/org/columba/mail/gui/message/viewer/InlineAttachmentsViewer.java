// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.message.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.io.ColumbaDesktop;
import org.columba.core.plugin.IExtension;
import org.columba.core.plugin.PluginManager;
import org.columba.core.plugin.exception.PluginException;
import org.columba.core.plugin.exception.PluginHandlerNotFoundException;
import org.columba.core.plugin.exception.PluginLoadingFailedException;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.temp.TempFolder;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.message.command.OpenAttachmentCommand;
import org.columba.mail.gui.message.command.SaveAttachmentAsCommand;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.plugin.ViewerExtensionHandler;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;
import org.columba.ristretto.parser.MimeTypeParser;

/**
 * @author fdietz
 * 
 */
public class InlineAttachmentsViewer extends JPanel implements ICustomViewer {

	private Vector attachmentPanels;

	private Vector viewers;

	private MessageController mediator;

	private ViewerExtensionHandler handler;

	private int counter;

	/**
	 * 
	 */
	public InlineAttachmentsViewer(MessageController mediator) {
		super();

		this.mediator = mediator;
		attachmentPanels = new Vector();
		viewers = new Vector();

		try {
			handler = (ViewerExtensionHandler) PluginManager.getInstance()
					.getHandler(ViewerExtensionHandler.NAME);
		} catch (PluginHandlerNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#view(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object, org.columba.mail.gui.frame.MailFrameMediator)
	 */
	public void view(IMailbox folder, Object uid, MailFrameMediator mediator)
			throws Exception {

		attachmentPanels.clear();
		viewers.clear();
		counter = 0;

		MimeTree mimePartTree = folder.getMimePartTree(uid);
		MimePart parent = mimePartTree.getRootMimeNode();

		MailFolderCommandReference ref = new MailFolderCommandReference(folder,
				new Object[] { uid }, parent.getAddress());

		MimeType mt = parent.getHeader().getMimeType();

		if (mt.getType().equals("multipart")) {
			if (mt.getSubtype().equals("alternative")) {
				traverseAlternativePart(parent, ref);
			} else {
				traverseChildren(parent, ref);
			}
		} else
			createChild(parent, ref);
	}

	/**
	 * @param folder
	 * @param uid
	 * @param list
	 * @throws PluginLoadingFailedException
	 * @throws Exception
	 */
	private void traverseChildren(MimePart parent,
			MailFolderCommandReference ref)
			throws PluginLoadingFailedException, Exception {

		List list = parent.getChilds();
		for (int i = 0; i < list.size(); i++) {
			MimePart mp = (MimePart) list.get(i);

			if (mp.getHeader().getMimeType().getType().equals("multipart")) {
				ref.setAddress(mp.getAddress());
				if (mp.getHeader().getMimeType().getSubtype().equals(
						"alternative")) {
					traverseAlternativePart(mp, ref);
				} else {
					traverseChildren(mp, ref);
				}
			} else {

				ref.setAddress(mp.getAddress());
				createChild(mp, ref);
			}
		}
	}

	/**
	 * @param parent
	 * @param ref
	 * @throws PluginLoadingFailedException
	 * @throws Exception
	 */
	private void createChild(MimePart child, MailFolderCommandReference ref)
			throws PluginLoadingFailedException, Exception {

		MimeHeader parentHeader = child.getHeader();

		if (parentHeader.getMimeType().equals(
				new MimeType("multipart", "alternative"))) {
			traverseAlternativePart(child, ref);
		} else {
			JPanel panel = createPanel(new MailFolderCommandReference(ref
					.getSourceFolder(), ref.getUids(), ref.getAddress()));
			attachmentPanels.add(panel);
		}

		counter++;
	}

	/**
	 * @param ref
	 * @param mp
	 * @return
	 * @throws Exception
	 */
	private void traverseAlternativePart(MimePart mp,
			MailFolderCommandReference ref) throws Exception {
		JPanel panel = null;
		if (prefersHTMLMimePart()) {
			// search for HTML mimepart
			for (int j = 0; j < mp.countChilds(); j++) {
				MimePart alternativePart = mp.getChild(j);
				if (alternativePart.getHeader().getMimeType().equals(
						new MimeType("text", "html"))) {
					ref.setAddress(alternativePart.getAddress());

					panel = createPanel(ref);
					attachmentPanels.add(panel);
					break;
				} else if(alternativePart.getHeader().getMimeType().getType().equals("multipart") ) {
					traverseChildren(alternativePart,ref);
				}
				
				
			}
		} else {
			// search for text mimepart
			for (int j = 0; j < mp.countChilds(); j++) {
				MimePart alternativePart = mp.getChild(j);
				if (alternativePart.getHeader().getMimeType().equals(
						new MimeType("text", "plain"))) {
					ref.setAddress(alternativePart.getAddress());
					panel = createPanel(ref);
					attachmentPanels.add(panel);
					break;
				} else if(alternativePart.getHeader().getMimeType().getType().equals("multipart") ) {
					traverseChildren(alternativePart,ref);
				}
			}
		}
	}

	/**
	 * @return
	 */
	private boolean prefersHTMLMimePart() {
		XmlElement html = MailConfig.getInstance().getMainFrameOptionsConfig()
				.getRoot().getElement("/options/html");
		boolean preferHtml = Boolean.valueOf(html.getAttribute("prefer"))
				.booleanValue();
		return preferHtml;
	}

	private JPanel createPanel(MailFolderCommandReference ref) throws Exception {

		IMailbox folder = (IMailbox) ref.getSourceFolder();
		Object uid = ref.getUids()[0];
		Integer[] address = ref.getAddress();
		MimePart mp = folder.getMimePartTree(uid).getFromAddress(address);
		MimeHeader h = mp.getHeader();

		String type = h.getMimeType().getType();
		String subtype = h.getMimeType().getSubtype();
		// Integer[] address = mp.getAddress();

		if (type.equals("application") && (subtype.equals("octet-stream"))
				&& h.getFileName() != null) {
			// Try to find out MIME type from Filename
			String extension = h.getFileName();
			extension = extension.substring(extension.lastIndexOf('.') + 1);

			MimeType systemMimeType = MimeTypeParser.parse(ColumbaDesktop
					.getInstance().getMimeType(extension));
			type = systemMimeType.getType();
			subtype = systemMimeType.getSubtype();
		}

		JPanel panel = null;
		if (type.equalsIgnoreCase("message")) {
			// rfc822 message

			ICustomViewer viewer = new Rfc822MessageViewer(mediator);

			panel = createMessagePane(viewer, ref);

			ref = createNewReference(h, mp, folder, uid);
			viewer.view((IMailbox) ref.getSourceFolder(), ref.getUids()[0],
					mediator.getFrameController());

			viewers.add(viewer);
		} else {
			String description = null;
			if (h.getFileName() != null)
				description = h.getFileName();
			else
				description = type + "/" + subtype;

			IMimePartViewer viewer = getViewer(type, subtype);
			if (viewer != null) {
				panel = createFileAttachmentPanel(description, viewer, ref);

				viewer.view((IMailbox) ref.getSourceFolder(), ref.getUids()[0],
						address, mediator.getFrameController());
				viewers.add(viewer);

			} else if (type.equalsIgnoreCase("text")) {

				// TODO: try to re-use instances of TextViewer
				viewer = new TextViewer(mediator);

				panel = createFileAttachmentPanel(description, viewer, ref);

				viewer.view((IMailbox) ref.getSourceFolder(), ref.getUids()[0],
						address, mediator.getFrameController());
				viewers.add(viewer);
			} else if (type.equalsIgnoreCase("image")) {

				viewer = new ImageViewer(mediator);

				panel = createFileAttachmentPanel(description, viewer, ref);

				viewer.view((IMailbox) ref.getSourceFolder(), ref.getUids()[0],
						address, mediator.getFrameController());
				viewers.add(viewer);
			} else {
				panel = createBasicPanel(description, ref, true);
			}

		}

		return panel;
	}

	/**
	 * @param mediator
	 * @param type
	 * @param subtype
	 * @return
	 * @throws PluginLoadingFailedException
	 */
	private IMimePartViewer getViewer(String type, String subtype)
			throws PluginException {
		IMimePartViewer viewer = null;

		// try to be specific: use type/subtype
		// -> example: "image/jpeg" or "text/html"
		if (handler.exists(type + "/" + subtype)) {
			IExtension extension = handler.getExtension(type + "/" + subtype);

			viewer = (IMimePartViewer) extension
					.instanciateExtension(new Object[] { mediator });
		}

		// use type-only instead
		// -> example: "image" or "text"
		if (handler.exists(type)) {
			IExtension extension = handler.getExtension(type + "/" + subtype);

			viewer = (IMimePartViewer) extension
					.instanciateExtension(new Object[] { mediator });
		}
		return viewer;
	}

	/**
	 * @param mimePartTree
	 */
	private Integer[] getBodyPartAddress(MimeTree mimePartTree) {
		MimePart bodyPart = null;
		XmlElement html = MailConfig.getInstance().getMainFrameOptionsConfig()
				.getRoot().getElement("/options/html");

		// Which Bodypart shall be shown? (html/plain)
		if ((Boolean.valueOf(html.getAttribute("prefer")).booleanValue())
				&& hasHtmlPart(mimePartTree.getRootMimeNode())) {
			bodyPart = mimePartTree.getFirstTextPart("html");
		} else {
			bodyPart = mimePartTree.getFirstTextPart("plain");
		}

		return bodyPart.getAddress();
	}

	private boolean hasHtmlPart(MimePart mimeTypes) {

		if (mimeTypes.getHeader().getMimeType().equals(
				new MimeType("text", "html")))
			return true; // exit immediately

		java.util.List children = mimeTypes.getChilds();

		for (int i = 0; i < children.size(); i++) {
			if (hasHtmlPart(mimeTypes.getChild(i)))
				return true;
		}

		return false;

	}

	/**
	 * @param viewer
	 * @param ref
	 * @return
	 */
	private JPanel createMessagePane(IViewer viewer,
			MailFolderCommandReference ref) {

		JPanel centerPanel = createBasicPanel("Rfc822/message", ref,
				counter != 0);

		JPanel viewerPanel = new JPanel();
		viewerPanel.setLayout(new BorderLayout());
		viewerPanel.add(viewer.getView(), BorderLayout.CENTER);
		viewerPanel.setBackground(UIManager.getColor("TextArea.background"));
		viewerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		centerPanel.add(viewerPanel, BorderLayout.CENTER);

		return centerPanel;
	}

	private MailFolderCommandReference createNewReference(MimeHeader h,
			MimePart mp, IMailbox folder, Object uid) throws Exception {

		String fileName = h.getFileName();

		InputStream is = folder.getMimePartBodyStream(uid, mp.getAddress());

		TempFolder tempFolder = FolderTreeModel.getInstance().getTempFolder();
		Object tempMessageUid = null;
		try {
			tempMessageUid = tempFolder.addMessage(is);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new MailFolderCommandReference(tempFolder,
				new Object[] { tempMessageUid });
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#updateGUI()
	 */
	public void updateGUI() throws Exception {
		removeAll();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		Iterator it = viewers.iterator();
		while (it.hasNext()) {
			IViewer viewer = (IViewer) it.next();
			viewer.updateGUI();
		}

		it = attachmentPanels.iterator();
		while (it.hasNext()) {
			JPanel panel = (JPanel) it.next();
			add(panel);
		}

		revalidate();
	}

	/**
	 * @return
	 */
	private JPanel createFileAttachmentPanel(String name, IViewer viewer,
			MailFolderCommandReference ref) {

		JPanel centerPanel = createBasicPanel(name, ref, counter != 0);

		centerPanel.add(viewer.getView(), BorderLayout.CENTER);
		return centerPanel;
	}

	/**
	 * @param name
	 * @return
	 */
	private JPanel createBasicPanel(String name,
			MailFolderCommandReference ref, boolean withHeader) {

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(UIManager.getColor("TextArea.background"));
		// buttonPanel.setLayout(new GridLayout(1, 3, 5, 0));

		JToggleButton hideButton = new JToggleButton("Hide/Show");
		JButton openButton = new JButton("Open");
		openButton.setIcon(ImageLoader.getImageIcon("folder-open.png"));
		JButton saveButton = new JButton("Save As...");
		saveButton.setIcon(ImageLoader.getImageIcon("stock_save_as-16.png"));

		JLabel label = new JLabel(name);
		label.setFont(label.getFont().deriveFont(Font.BOLD));

		buttonPanel.add(hideButton);
		buttonPanel.add(openButton);
		buttonPanel.add(saveButton);

		JPanel internPanel = new JPanel();
		internPanel.setBackground(UIManager.getColor("TextArea.background"));
		internPanel.setLayout(new BorderLayout());

		internPanel.add(label, BorderLayout.EAST);
		internPanel.add(Box.createHorizontalStrut(10), BorderLayout.CENTER);
		internPanel.add(buttonPanel, BorderLayout.WEST);

		JPanel topPanel = new JPanel();
		topPanel.setBackground(UIManager.getColor("TextArea.background"));
		topPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		topPanel.setLayout(new BorderLayout());
		topPanel.add(internPanel, BorderLayout.WEST);

		JPanel centerPanel = new JPanel();
		if (!withHeader) {
			centerPanel.setLayout(new BorderLayout());
		} else {
			centerPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory
							.createCompoundBorder(BorderFactory
									.createEtchedBorder(), BorderFactory
									.createEmptyBorder(5, 5, 5, 5))));
			centerPanel
					.setBackground(UIManager.getColor("TextArea.background"));
			centerPanel.setLayout(new BorderLayout());
			centerPanel.add(topPanel, BorderLayout.NORTH);

		}

		hideButton.addActionListener(new HideActionListener(centerPanel,
				topPanel, hideButton));

		openButton.addActionListener(new OpenActionListener(ref));
		openButton.setEnabled(ColumbaDesktop.getInstance().supportsOpen());
		saveButton.addActionListener(new SaveAsActionListener(ref));
		return centerPanel;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#getView()
	 */
	public JComponent getView() {
		return this;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#isVisible()
	 */
	public boolean isVisible() {
		return true;
	}

	class OpenActionListener implements ActionListener {

		MailFolderCommandReference ref;

		public OpenActionListener(MailFolderCommandReference ref) {
			this.ref = ref;

		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			CommandProcessor.getInstance()
					.addOp(new OpenAttachmentCommand(ref));
		}

	}

	class HideActionListener implements ActionListener {

		private JPanel center;

		private JPanel top;

		private JToggleButton button;

		private Component contents;

		public HideActionListener(JPanel center, JPanel top,
				JToggleButton button) {
			this.center = center;
			this.top = top;
			this.button = button;

			if (center.getComponentCount() == 2)
				contents = center.getComponent(1);
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			if (!button.isSelected()) {
				// show contents
				if (contents != null)
					center.add(contents);

			} else {
				// hide contents
				if (center.getComponentCount() == 2)
					contents = center.getComponent(1);
				if (contents != null)
					center.remove(contents);
			}
			center.revalidate();
		}

	}

	class SaveAsActionListener implements ActionListener {

		MailFolderCommandReference ref;

		public SaveAsActionListener(MailFolderCommandReference ref) {
			this.ref = ref;

		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			CommandProcessor.getInstance().addOp(
					new SaveAttachmentAsCommand(ref));
		}

	}

}