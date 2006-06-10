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
package org.columba.mail.gui.composer.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


public class QuoteFilterInputStream extends FilterInputStream {
    private static final int QUOTE = 0;
    private static final int BODY = 1;
    private static final int BODYSTART = 2;
    private byte[] quotePrefix;
    private int mode;
    private int quotePos;
    private int preRead;

    /**
     * @param arg0
     */
    public QuoteFilterInputStream(InputStream arg0, String prefix)
        throws IOException {
        super(arg0);

        quotePrefix = prefix.getBytes();

        preRead = arg0.read();

        if (preRead == -1) {
            mode = BODYSTART;
        } else {
            // First print a quote
            mode = QUOTE;
            quotePos = 0;
        }
    }

    /**
     * @param arg0
     */
    public QuoteFilterInputStream(InputStream arg0) throws IOException {
        this(arg0, "> ");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        int result = -1;

        switch (mode) {
        case QUOTE: {
            if (quotePos < quotePrefix.length) {
                result = (int) quotePrefix[quotePos++];
            } else {
                // reset
                mode = BODY;
                quotePos = 0;
                result = preRead;
            }

            break;
        }

        case BODYSTART: {
            mode = BODY;
            result = preRead;

            break;
        }

        case BODY: {
            result = in.read();

            break;
        }
        }

        // Do we have to insert a quoteprefix?
        if (result == '\n') {
            preRead = in.read();

            if (preRead == -1) {
                mode = BODYSTART;
            } else {
                mode = QUOTE;
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        int next;

        for (int i = 0; i < arg2; i++) {
            next = read();

            if (next == -1) {
                if (i == 0) {
                    return -1;
                } else {
                    return i;
                }
            }

            arg0[arg1 + i] = (byte) next;
        }

        return arg2;
    }
}
