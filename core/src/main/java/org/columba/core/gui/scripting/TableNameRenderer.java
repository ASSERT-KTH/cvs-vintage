package  org.columba.core.gui.scripting;

import javax.swing.table.DefaultTableCellRenderer;

import org.columba.core.scripting.model.ColumbaScript;

public class TableNameRenderer extends DefaultTableCellRenderer {

	protected void setValue(Object value) {
		setText(((ColumbaScript) value).getName());
	}
}
