package org.columba.core.gui.util.treetable;

import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

/**
	 * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
	 * to listen for changes in the ListSelectionModel it maintains. Once
	 * a change in the ListSelectionModel happens, the paths are updated
	 * in the DefaultTreeSelectionModel.
	 */
class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel {
	/** Set to true when we are updating the ListSelectionModel. */
	protected boolean updatingListSelectionModel;

	protected boolean update = false;

	protected JTree tree;
	
	public ListToTreeSelectionModelWrapper( JTree tree) {
		super();

		this.tree = tree;
		
		getListSelectionModel().addListSelectionListener(
			createListSelectionListener());

	}

	/**
	 * Returns the list selection model. ListToTreeSelectionModelWrapper
	 * listens for changes to this model and updates the selected paths
	 * accordingly.
	 */

	ListSelectionModel getListSelectionModel() {
		return listSelectionModel;
	}

	/**
	 * This is overridden to set <code>updatingListSelectionModel</code>
	 * and message super. This is the only place DefaultTreeSelectionModel
	 * alters the ListSelectionModel.
	 */

	public void resetRowSelection() {
		if (!updatingListSelectionModel) {
			updatingListSelectionModel = true;

			try {
				super.resetRowSelection();
			} finally {
				updatingListSelectionModel = false;
			}
		}
		// Notice how we don't message super if
		// updatingListSelectionModel is true. If
		// updatingListSelectionModel is true, it implies the
		// ListSelectionModel has already been updated and the
		// paths are the only thing that needs to be updated.
	}

	/**
	 * Creates and returns an instance of ListSelectionHandler.
	 */

	protected ListSelectionListener createListSelectionListener() {
		return new ListSelectionHandler();
	}

	/**
	 * If <code>updatingListSelectionModel</code> is false, this will
	 * reset the selected paths from the selected rows in the list
	 * selection model.
	 */

	public void updateSelectedPathsFromSelectedRows() {

		if (!updatingListSelectionModel) {
			updatingListSelectionModel = true;

			try {

				// This is way expensive, ListSelectionModel needs an
				// enumerator for iterating.
				int min = listSelectionModel.getMinSelectionIndex();
				int max = listSelectionModel.getMaxSelectionIndex();

				this.clearSelection();

				if (min != -1 && max != -1) {
					for (int counter = min; counter <= max; counter++) {

						if (listSelectionModel.isSelectedIndex(counter)) {
							TreePath selPath = tree.getPathForRow(counter);

							if (selPath != null) {

								addSelectionPath(selPath);
							}
						}

					}
				}

			} finally {
				updatingListSelectionModel = false;
			}
		}

	}

	/**
	 * Class responsible for calling updateSelectedPathsFromSelectedRows
	 * when the selection of the list changse.
	 */

	class ListSelectionHandler implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {

			updateSelectedPathsFromSelectedRows();

		}
	}

}