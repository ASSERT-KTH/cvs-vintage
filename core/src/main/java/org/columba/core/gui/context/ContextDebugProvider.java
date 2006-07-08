package org.columba.core.gui.context;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.columba.core.context.base.api.IName;
import org.columba.core.context.base.api.IStructureValue;
import org.columba.core.context.semantic.api.ISemanticContext;
import org.columba.core.gui.context.api.IContextProvider;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;

public class ContextDebugProvider extends JPanel implements IContextProvider {

	private JTree tree;

	private DefaultTreeModel treeModel;

	public ContextDebugProvider() {
		super();

		setLayout(new BorderLayout());
		tree = new JTree();
		add(tree, BorderLayout.CENTER);
//		JScrollPane scrollPane = new JScrollPane(tree);
//		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		
//		add(scrollPane, BorderLayout.CENTER);

	}

	public String getName() {
		return "Context Debug View";
	}

	public String getDescription() {
		return "Context Debug View - only visible if Columba is in DEBUG mode";
	}

	public ImageIcon getIcon() {
		return ImageLoader.getSmallIcon(IconKeys.COMPUTER);
	}

	public int getTotalResultCount() {
		return 5;
	}

	public void search(ISemanticContext context, int startIndex, int resultCount) {
		IStructureValue value = context.getValue();
		if ( value == null ) return;
		
		StringBuffer buf = new StringBuffer();
		buf.append(value.getName());
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(buf.toString());

		createTree(root, value);
		treeModel = new DefaultTreeModel(root);
	}

	private void createTree(DefaultMutableTreeNode parent, IStructureValue value) {
		Iterator<IName> it = value.getAllAttributeNames();
		while (it.hasNext()) {
			IName name = it.next();
			String str = value.getObject(name.getName(), name.getNamespace()).toString();
			
			StringBuffer buf = new StringBuffer();
			buf.append(name.getName());
			buf.append(":");
			buf.append(str);
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(buf.toString());
			parent.add(child);
		}

		Iterator<IName> it2 = value.getAllChildNames();
		while (it2.hasNext()) {
			IName name = it2.next();
			Iterator<IStructureValue> childIt = value.getChildIterator(name
					.getName(), name.getNamespace());
			while (childIt.hasNext()) {
				IStructureValue child = childIt.next();
				StringBuffer buf = new StringBuffer();
				buf.append(child.getName());
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(buf.toString());
				parent.add(childNode);
				createTree(childNode, child);
			}
		}
	}

	public JComponent getView() {
		return this;
	}

	public void showResult() {
		tree.setModel(treeModel);
	}

	public void clear() {
		treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
		tree.setModel(treeModel);
	}

}
