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
package org.columba.core.gui.logdisplay;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;


/**
 * A List that contains all log records.
 * @author redsolo
 */
public final class LogRecordList extends Handler implements TableModel, ListModel {

    /** Singleton instance for all log memory. */
    private static LogRecordList instance;

    /** Columns in the table. */
    public static final String[] COLUMNS = new String[] {"Level", "Message", "Class", "Method", "Time", "Seq. nr", "Thread id" };

    private List logMessages = new LinkedList();
    private Set listeners = new HashSet();

    private boolean isStarted;

    /**
     *
     */
    private LogRecordList() {
        super();
        startLogging();
    }

    /**
     * Returns the singleton instance of the log record list.
     * @return the LogRecordList object.
     */
    public static LogRecordList getInstance() {
        if (instance == null) {
            instance = new LogRecordList();
        }
        return instance;
    }

    /**
     * Returns the LogRecord for the specified row;
     * @param rowIndex the row.
     * @return the LogRecord
     */
    public LogRecord getLogRecord(int rowIndex) {
        if ((rowIndex >= 0) && (rowIndex < logMessages.size())) {
            return (LogRecord) logMessages.get(rowIndex);
        } else {
            throw new IndexOutOfBoundsException("No such row in the table model [" + rowIndex + "]");
        }
    }

    /**
     * Clears all log messages.
     */
    public void clear() {
        int size = logMessages.size();
        logMessages.clear();
        fireListRemoveEvent(0, size);
    }

    /**
     * Starts storing the log messages.
     * All log messages issued after this method, will be stored in this object.
     */
    public void startLogging() {
        if (!isStarted) {
            Logger log = Logger.getLogger("org.columba");
            log.addHandler(this);
            isStarted = true;
        }
    }

    /**
     * Stops storing the log messages.
     * All log messages issued after this method, will not be stored in this object. This
     * will not alter the actual log messages list.
     */
    public void stopLogging() {
        if (isStarted) {
            Logger log = Logger.getLogger("org.columba");
            log.removeHandler(this);
            isStarted = false;
        }
    }

    //
    // Logging stuff
    //

    /** {@inheritDoc} */
    public void close() {
    }

    /** {@inheritDoc} */
    public void flush() {
    }

    /** {@inheritDoc} */
    public void publish(LogRecord record) {
        logMessages.add(record);
        if (logMessages.size() > 1000) {
            logMessages.remove(0);
            fireListUpdatedEvent();
            //notifyTableListeners(new TableModelEvent(this, 0, logMessages.size()));
        } else {
            fireNewRowEvent(logMessages.size());
            //notifyTableListeners(new TableModelEvent(this, logMessages.size(), logMessages.size(), TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
        }
    }

    //
    // Table model stuff
    //

    /**
     * Notify all table listeners.
     * @param event the event to notify with.
     */
    /*private void notifyTableListeners(TableModelEvent event) {
        for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            TableModelListener listener = (TableModelListener) iterator.next();
            listener.tableChanged(event);
        }
    }*/

    /** {@inheritDoc} */
    public int getColumnCount() {
        return COLUMNS.length;
    }

    /** {@inheritDoc} */
    public int getRowCount() {
        return logMessages.size();
    }

    /** {@inheritDoc} */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /** {@inheritDoc} */
    public Class getColumnClass(int columnIndex) {
        Class value;
        switch (columnIndex) {
            default:
                throw new IndexOutOfBoundsException("No such column in the table model [" + columnIndex + "]");
            case 1:
            case 2:
            case 3:
                value = String.class;
                break;
            case 0:
                value = Level.class;
                break;
            case 4:
                value = Date.class;
                break;
            case 5:
                value = Long.class;
                break;
            case 6:
                value = Integer.class;
                break;
        }
        return value;
    }

    /** {@inheritDoc} */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value;
        if ((rowIndex >= 0) && (rowIndex < logMessages.size())) {
            LogRecord record = (LogRecord) logMessages.get(rowIndex);
            switch (columnIndex) {
                default:
                    throw new IndexOutOfBoundsException("No such column in the table model [" + columnIndex + "]");
                case 0:
                    value = record.getLevel();
                    break;
                case 1:
                    value = record.getMessage();
                    break;
                case 2:
                    value = record.getSourceClassName();
                    break;
                case 3:
                    value = record.getSourceMethodName();
                    break;
                case 4:
                    value = new Date(record.getMillis());
                    break;
                case 5:
                    value = new Long(record.getSequenceNumber());
                    break;
                case 6:
                    value = new Integer(record.getThreadID());
                    break;
            }
        } else {
            throw new IndexOutOfBoundsException("No such row in the table model [" + rowIndex + "]");
        }
        return value;
    }

    /** {@inheritDoc} */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        throw new UnsupportedOperationException("The table model is not editable");
    }

    /** {@inheritDoc} */
    public String getColumnName(int columnIndex) {
        return COLUMNS[columnIndex];
    }

    /** {@inheritDoc} */
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    /** {@inheritDoc} */
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    //
    // LIST model stuff
    //

    /** {@inheritDoc} */
    public int getSize() {
        return logMessages.size();
    }

    /** {@inheritDoc} */
    public Object getElementAt(int index) {
        return getLogRecord(index);
    }

    /** {@inheritDoc} */
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    /** {@inheritDoc} */
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    /**
     * Notifies all list data listener that a row has been inserted.
     * @param row the new row.
     */
    private void fireNewRowEvent(int row) {
        ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, row, row);
        for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            ListDataListener listener = (ListDataListener) iterator.next();
            listener.intervalAdded(event);
        }
    }

    /**
     * Notifies all list data listener that the contents has changed..
     */
    private void fireListUpdatedEvent() {
        ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, logMessages.size());
        for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            ListDataListener listener = (ListDataListener) iterator.next();
            listener.contentsChanged(event);
        }
    }

    /**
     * Notifies all list data listener that rows has been removed
     * @param rowStart the first row removed.
     * @param rowEnd the last row removed.
     */
    private void fireListRemoveEvent(int rowStart, int rowEnd) {
        ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, rowStart, rowEnd);
        for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            ListDataListener listener = (ListDataListener) iterator.next();
            listener.intervalRemoved(event);
        }
    }

}
