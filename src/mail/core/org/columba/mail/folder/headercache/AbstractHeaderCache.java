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
package org.columba.mail.folder.headercache;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.util.BooleanCompressor;

import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;

import java.io.File;

import java.util.List;
import java.util.Vector;


/**
 * Provides basic support for saving and loading email headers as fast as
 * possible.
 * <p>
 * It therefor tries to compress the data to make it as small as possible,
 * which improves performance dramatically
 *
 * @see CachedHeaderfields
 *
 * @author fdietz
 */
public abstract class AbstractHeaderCache {
    protected HeaderList headerList;
    protected File headerFile;
    private boolean headerCacheLoaded;
    protected String[] columnNames;
    protected ObjectWriter writer;
    protected ObjectReader reader;
    protected List additionalHeaderfields;

    /**
     * @param folder
     */
    public AbstractHeaderCache(File headerFile) {
        this.headerFile = headerFile;

        headerList = new HeaderList();

        headerCacheLoaded = false;

        columnNames = null;

        additionalHeaderfields = new Vector();
    }

    /**
     * @return
     */
    public ColumbaHeader createHeaderInstance() {
        return new ColumbaHeader();
    }

    /**
     * @return
     */
    public boolean isHeaderCacheLoaded() {
        return headerCacheLoaded;
    }

    /**
     * @param uid
     * @return @throws
     *         Exception
     */
    public boolean exists(Object uid) throws Exception {
        return headerList.containsKey(uid);
    }

    /**
     * @return
     */
    public int count() {
        return headerList.count();
    }

    /**
     * @param uid
     * @throws Exception
     */
    public void remove(Object uid) throws Exception {
        ColumbaLogger.log.info("trying to remove message UID=" + uid);

        if (headerList.containsKey(uid)) {
            ColumbaLogger.log.info("remove UID=" + uid);

            headerList.remove(uid);
        }
    }

    /**
     * @param header
     * @throws Exception
     */
    public void add(ColumbaHeader header) throws Exception {
        headerList.add(header, header.get("columba.uid"));
    }

    /**
     * Get or (re)create the header cache file.
     *
     * @return the HeaderList
     * @throws Exception
     */
    public HeaderList getHeaderList() throws Exception {
        boolean needToRelease = false;

        // if there exists a ".header" cache-file
        //  try to load the cache
        if (!headerCacheLoaded) {
            if (headerFile.exists()) {
                try {
                    load();
                } catch (Exception e) {
                    e.printStackTrace();

                    headerCacheLoaded = true;
                    headerList = new HeaderList();

                    return headerList;
                }
            }

            headerCacheLoaded = true;
        }

        return headerList;
    }

    /**
     * @throws Exception
     */
    public abstract void load() throws Exception;

    /**
     * @throws Exception
     */
    public abstract void save() throws Exception;

    protected void loadHeader(ColumbaHeader h) throws Exception {
        // load boolean headerfields, which are compressed in one int value
        int compressedFlags = ((Integer) reader.readObject()).intValue();

        for (int i = 0;
                i < CachedHeaderfields.INTERNAL_COMPRESSED_HEADERFIELDS.length;
                i++) {
            h.set(CachedHeaderfields.INTERNAL_COMPRESSED_HEADERFIELDS[i],
                BooleanCompressor.decompress(compressedFlags, i));
        }

        // load other internal headerfields, non-boolean type
        String[] columnNames = CachedHeaderfields.INTERNAL_HEADERFIELDS;

        for (int j = 0; j < columnNames.length; j++) {
            h.set(columnNames[j], reader.readObject());
        }

        //		load default headerfields, as defined in RFC822
        columnNames = CachedHeaderfields.getDefaultHeaderfields();

        for (int j = 0; j < columnNames.length; j++) {
            h.set(columnNames[j], reader.readObject());
        }

        // load user-specified additional headerfields
        // Note, that we use keys loaded from the headercache
        // file.
        for (int j = 0; j < additionalHeaderfields.size(); j++) {
            h.set((String) additionalHeaderfields.get(j),
                (String) reader.readObject());
        }
    }

    protected void saveHeader(ColumbaHeader h) throws Exception {
        // save boolean headerfields, compressing them to one int value
        Boolean[] b = new Boolean[CachedHeaderfields.INTERNAL_COMPRESSED_HEADERFIELDS.length];

        for (int i = 0; i < b.length; i++) {
            b[i] = (Boolean) h.get(CachedHeaderfields.INTERNAL_COMPRESSED_HEADERFIELDS[i]);

            // if value doesn't exist, use false as default
            if (b[i] == null) {
                b[i] = Boolean.FALSE;
            }
        }

        writer.writeObject(new Integer(BooleanCompressor.compress(b)));

        // save other internal headerfields, of non-boolean type
        String[] columnNames = CachedHeaderfields.INTERNAL_HEADERFIELDS;
        Object o;

        for (int j = 0; j < columnNames.length; j++) {
            writer.writeObject(h.get(columnNames[j]));
        }

        // save default headerfields, as defined in RFC822
        columnNames = CachedHeaderfields.DEFAULT_HEADERFIELDS;

        for (int j = 0; j < columnNames.length; j++) {
            writer.writeObject(h.get(columnNames[j]));
        }

        // -> also save additional headerfields specified by user
        // we use the keys as specified in CachedHeaderfields
        // Note: This is different from loading, where we use the
        // keys from the headercache file
        columnNames = CachedHeaderfields.getUserDefinedHeaderfields();

        if (columnNames != null) {
            for (int j = 0; j < columnNames.length; j++) {
                writer.writeObject((String) h.get(columnNames[j]));
            }
        }
    }

    /**
     * @param b
     */
    public void setHeaderCacheLoaded(boolean b) {
        headerCacheLoaded = b;
    }
}
