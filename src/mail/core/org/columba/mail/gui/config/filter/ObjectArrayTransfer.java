/*
 * Created on 2003-nov-11
 */
package org.columba.mail.gui.config.filter;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;


/**
 * An Object array transfer.
 *
 *
 * @author redsolo
 */
public class ObjectArrayTransfer implements Transferable {
    /** The only <code>DataFlavor</code> that this transfer allows. */
    public static DataFlavor FLAVOR;

    static {
        try {
            FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** The JComponent that created this Transferable */
    private JComponent source;

    /** The data that is being transfered */
    private Object[] objectArray;

    /**
 * Creates an object array transfer from the specified source.
 * @param comp source component.
 * @param arr the object array.
 */
    public ObjectArrayTransfer(JComponent comp, Object[] arr) {
        source = comp;
        objectArray = arr;
    }

    /** {@inheritDoc}
 * @return this object (ObjectArrayTransfer) */
    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }

        return this;
    }

    /** {@inheritDoc} */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return FLAVOR.equals(flavor);
    }

    /** {@inheritDoc} */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { FLAVOR };
    }

    /**
 * Returns the source of this transfer.
 * @return the source of this transfer.
 */
    public JComponent getSource() {
        return source;
    }

    /**
 * Returns the object array.
 * @return the object array.
 */
    public Object[] getData() {
        return objectArray;
    }
}
