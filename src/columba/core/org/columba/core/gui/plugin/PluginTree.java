/*
 * Created on 06.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeModel;

import org.columba.core.gui.util.treetable.Tree;
import org.columba.core.gui.util.treetable.TreeTable;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PluginTree extends TreeTable {

	final static String[] columns = { "Description", "Version", "Enabled" };

	final static String[] CATEGORIES =
		{
			"Look and Feel",
			"Filter",
			"Filter Action",
			"Spam",
			"Mail Import",
			"Addressbook Import",
			"Interpreter Language",
			"Examples",
			"Uncategorized" };

	protected Map map;
	protected PluginTreeTableModel model;

	/**
	 * 
	 */
	public PluginTree() {
		super();

		map = new HashMap();

		model = new PluginTreeTableModel(columns);
		model.setTree((Tree) getTree());
		((DefaultTreeModel) model.getTree().getModel()).setAsksAllowsChildren(
			true);

		initTree();

		setModel(model);

		getTree().setCellRenderer(new DescriptionTreeRenderer());

		// make "version" column fixed size
		TableColumn tc = getColumn(columns[1]);
		tc.setCellRenderer(new VersionRenderer());
		tc.setMaxWidth(80);
		tc.setMinWidth(80);

		// make "enabled" column fixed size
		tc = getColumn(columns[2]);
		//tc.setCellRenderer(new EnabledRenderer());
		//tc.setCellEditor(new EnabledEditor());
		tc.setMaxWidth(80);
		tc.setMinWidth(80);

	}

	protected void initTree() {
		PluginNode root = new PluginNode();
		root.setId("root");

		initCategories(root);

		List list = MainInterface.pluginManager.getIds();
		ListIterator it = list.listIterator();
		while (it.hasNext()) {
			// plugin id
			String id = (String) it.next();

			XmlElement pluginElement =
				MainInterface.pluginManager.getPluginElement(id);

			// plugin wasn't correctly loaded
			if (pluginElement == null)
				continue;

			String category = pluginElement.getAttribute("category");
			if (category == null) {
				// this plugin doesn't define a category to which it belongs
				category = "Uncategorized";
			}

			PluginNode childNode = new PluginNode();
			childNode.setId(pluginElement.getAttribute("id"));
			childNode.setTooltip(pluginElement.getAttribute("description"));
			childNode.setVersion(pluginElement.getAttribute("version"));
			String enabled = pluginElement.getAttribute("enabled");
			if (enabled == null)
				enabled = "true";
			childNode.setEnabled(Boolean.getBoolean(enabled));

			PluginNode node = (PluginNode) map.get(category);
			if (node == null) {
				// unknown category found 
				// -> just add this plugin to "Uncategorized"
				category = "Uncategorized";
				node = (PluginNode) map.get(category);
			}

			node.add(childNode);
		}

		model.set(root);

	}

	protected void initCategories(PluginNode root) {
		for (int i = 0; i < CATEGORIES.length; i++) {
			String c = CATEGORIES[i];
			PluginNode node = new PluginNode();
			node.setAllowsChildren(true);
			node.setId(c);
			node.setEnabled(true);
			node.setCategory(true);
			root.add(node);
			map.put(c, node);
		}
	}

}
