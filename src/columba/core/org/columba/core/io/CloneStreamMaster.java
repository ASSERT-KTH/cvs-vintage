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
package org.columba.core.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;


/**
 * The model that needs to be instanciated if you want to create CloneInputStreams from a master.
 *
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class CloneStreamMaster {
    private static int uid = 0;
    private InputStream master;
    private int nextId;
    private List streamList;
    private File tempFile;
    private byte[] buffer;
    private int openClones;
    private boolean usesFile;
    private int size;

    /**
 * Constructs a CloneStreamMaster. Note that the master must NOT be read from after
 * the construction!
 *
 * @param master
 */
    public CloneStreamMaster(InputStream master) throws IOException {
        super();
        this.master = master;

        streamList = new ArrayList(2);

        if (master.available() > 51200) {
            tempFile = File.createTempFile("columba-stream-clone" + (uid++),
                    ".tmp");

            // make sure file is deleted automatically when closing VM
            tempFile.deleteOnExit();

            FileOutputStream tempOut = new FileOutputStream(tempFile);

            size = (int) StreamUtils.streamCopy(master, tempOut);

            tempOut.close();

            usesFile = true;
        } else {
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();

            size = (int) StreamUtils.streamCopy(master, tempOut);
            tempOut.close();

            buffer = tempOut.toByteArray();
            usesFile = false;
        }

        master.close();
    }

    /**
 * Gets a new clone of the master.
 *
 * @return Clone of the master
 */
    public CloneInputStream getClone() {
        if (usesFile) {
            try {
                // add a new inputstream to read from
                streamList.add(new FileInputStream(tempFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();

                // only if tempfile was corrupted
            }
        } else {
            streamList.add(new ByteArrayInputStream(buffer));
        }

        openClones++;

        return new CloneInputStream(this, nextId++);
    }

    public int read(int id) throws IOException {
        return ((InputStream) streamList.get(id)).read();
    }

    public int read(int id, byte[] out, int offset, int length)
        throws IOException {
        return ((InputStream) streamList.get(id)).read(out, offset, length);
    }

    /**
 * @return
 */
    public int available() throws IOException {
        return size;
    }

    /* (non-Javadoc)
 * @see java.lang.Object#finalize()
 */
    protected void finalize() throws Throwable {
        super.finalize();

        if (usesFile) {
            // Delete the tempfile immedietly
            tempFile.delete();
        }
    }

    /**
 * @param id
 */
    public void close(int id) throws IOException {
        ((InputStream) streamList.get(id)).close();
    }
}
