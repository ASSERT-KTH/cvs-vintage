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

import java.awt.Font;
import java.awt.Insets;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.columba.core.charset.CharsetOwnerInterface;
import org.columba.core.gui.util.FontProperties;
import org.columba.core.io.DiskIO;
import org.columba.core.io.TempFileStore;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.util.DocumentParser;
import org.columba.mail.main.MailInterface;
import org.columba.mail.parser.text.HtmlParser;
import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.CharsetDecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.StreamableMimePart;
import org.columba.ristretto.message.io.CharSequenceSource;

/**
 * Viewer displays message body text.
 * 
 * @author fdietz
 *  
 */
public class MessageBodytextViewer extends JTextPane implements Viewer,
        Observer {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger
            .getLogger("org.columba.mail.gui.message.viewer");

    //  parser to transform text to html
    private DocumentParser parser;

    private HTMLEditorKit htmlEditorKit;

    // stylesheet is created dynamically because
    // user configurable fonts are used
    private String css = "";

    //  enable/disable smilies configuration
    private XmlElement smilies;

    private boolean enableSmilies;

    // name of font
    private String name;

    // size of font
    private String size;

    // overwrite look and feel font settings
    private boolean overwrite;

    private String body;

    public MessageBodytextViewer() {
        super();

        setMargin(new Insets(5, 5, 5, 5));
        setEditable(false);

        htmlEditorKit = new HTMLEditorKit();
        setEditorKit(htmlEditorKit);

        setContentType("text/html");

        XmlElement gui = MailInterface.config.get("options").getElement(
                "/options/gui");
        XmlElement messageviewer = gui.getElement("messageviewer");

        if (messageviewer == null) {
            messageviewer = gui.addSubElement("messageviewer");
        }

        smilies = messageviewer.getElement("smilies");

        if (smilies == null) {
            smilies = messageviewer.addSubElement("smilies");
        }

        // register as configuration change listener
        smilies.addObserver(this);

        String enable = smilies.getAttribute("enabled", "true");

        if (enable.equals("true")) {
            enableSmilies = true;
        } else {
            enableSmilies = false;
        }

        XmlElement quote = messageviewer.getElement("quote");

        if (quote == null) {
            quote = messageviewer.addSubElement("quote");
        }

        // register as configuration change listener
        quote.addObserver(this);

        // TODO use value in initStyleSheet()
        String enabled = quote.getAttribute("enabled", "true");
        String color = quote.getAttribute("color", "0");

        // register for configuration changes
        Font font = FontProperties.getTextFont();
        name = font.getName();
        size = new Integer(font.getSize()).toString();

        XmlElement options = MainInterface.config.get("options").getElement(
                "/options");
        XmlElement gui1 = options.getElement("gui");
        XmlElement fonts = gui1.getElement("fonts");

        if (fonts == null) {
            fonts = gui1.addSubElement("fonts");
        }

        // register interest on configuratin changes
        fonts.addObserver(this);

        initStyleSheet();
    }

    /**
     * @see org.columba.mail.gui.message.viewer.Viewer#getViewer(org.columba.mail.folder.Folder,
     *      java.lang.Object)
     */
    public void view(MessageFolder folder, Object uid,
            MailFrameMediator mediator) throws Exception {
        StreamableMimePart bodyPart = null;

        MimeTree mimePartTree = folder.getMimePartTree(uid);

        XmlElement html = MailInterface.config.getMainFrameOptionsConfig()
                .getRoot().getElement("/options/html");
        // Which Bodypart shall be shown? (html/plain)
        if (Boolean.valueOf(html.getAttribute("prefer")).booleanValue())
            bodyPart = (StreamableMimePart) mimePartTree
                    .getFirstTextPart("html");
        else
            bodyPart = (StreamableMimePart) mimePartTree
                    .getFirstTextPart("plain");
        if (bodyPart == null) {
            bodyPart = new LocalMimePart(new MimeHeader());
            ((LocalMimePart) bodyPart).setBody(new CharSequenceSource(
                    "<No Message-Text>"));
        } else {

            bodyPart = (StreamableMimePart) folder.getMimePart(uid, bodyPart
                    .getAddress());
        }

        // Which Charset shall we use ?
        Charset charset = ((CharsetOwnerInterface) mediator).getCharset();

        // no charset specified -> automatic mode
        // -> try to determine charset based on content parameter
        if (charset == null) {
            String charsetName = bodyPart.getHeader().getContentParameter(
                    "charset");

            if (charsetName == null) {
                // There is no charset info -> the default system charset is
                // used
                charsetName = System.getProperty("file.encoding");
            }

            charset = Charset.forName(charsetName);

            //((CharsetOwnerInterface) mediator).setCharset(charset);

        }

        // Shall we use the HTML-Viewer?
        boolean htmlViewer = bodyPart.getHeader().getMimeType().getSubtype()
                .equals("html");

        InputStream bodyStream = ((StreamableMimePart) bodyPart)
                .getInputStream();

        int encoding = bodyPart.getHeader().getContentTransferEncoding();

        switch (encoding) {
        case MimeHeader.QUOTED_PRINTABLE:
            {
                bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);

                break;
            }

        case MimeHeader.BASE64:
            {
                bodyStream = new Base64DecoderInputStream(bodyStream);

                break;
            }
        }

        bodyStream = new CharsetDecoderInputStream(bodyStream, charset);

        StringBuffer text = new StringBuffer();
        int next = bodyStream.read();

        while (next != -1) {
            text.append((char) next);
            next = bodyStream.read();
        }

        setBodyText(text.toString(), htmlViewer);

        bodyStream.close();
    }

    /**
     * 
     * read text-properties from configuration and create a stylesheet for the
     * html-document
     *  
     */
    protected void initStyleSheet() {
        // read configuration from options.xml file
        // create css-stylesheet string
        // set font of html-element <P>
        css = "<style type=\"text/css\"><!-- .bodytext {font-family:\"" + name
                + "\"; font-size:\"" + size + "pt; \"}"
                + ".quoting {color:#949494;}; --></style>";
    }

    protected void setBodyText(String bodyText, boolean html) {
        if (html) {
            try {
                // this is a HTML message

                body = HtmlParser.htmlToText(bodyText);

                // try to fix broken html-strings

                String validated = HtmlParser.validateHTMLString(bodyText);
                LOG.info("validated bodytext:\n" + validated);

                // create temporary file
                File inputFile = TempFileStore.createTempFileWithSuffix("html");

                // save bodytext to file
                DiskIO.saveStringInFile(inputFile, bodyText);

                URL url = inputFile.toURL();

                // use asynchronous loading method setPage to display
                // URL correctly
                setPage(url);

                // this is the old method which doesn't work
                // for many html-messages
                /*
                 * getDocument().remove(0,getDocument().getLength()-1);
                 * 
                 * ((HTMLDocument) getDocument()).getParser().parse( new
                 * StringReader(validated), ((HTMLDocument)
                 * getDocument()).getReader(0), true);
                 */
                // scroll window to the beginning
                setCaretPosition(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // this is a text/plain message
            body = bodyText;

            try {
                // substitute special characters like:
                //  <,>,&,\t,\n
                String r = HtmlParser.substituteSpecialCharacters(bodyText);

                // parse for urls and substite with HTML-code
                r = HtmlParser.substituteURL(r);

                // parse for email addresses and substite with HTML-code
                r = HtmlParser.substituteEmailAddress(r);

                // parse for quotings and color the darkgray
                r = parser.markQuotings(r);

                // add smilies
                if (enableSmilies == true) {
                    r = parser.addSmilies(r);
                }

                // encapsulate bodytext in html-code
                r = transformToHTML(new StringBuffer(r));

                LOG.info("validated bodytext:\n" + r);

                // display bodytext
                setText(r);

                //		setup base url in order to be able to display images
                // in html-component
                URL baseUrl = DiskIO.getResourceURL("org/columba/core/images/");
                LOG.info(baseUrl.toString());
                ((HTMLDocument) getDocument()).setBase(baseUrl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // scroll window to the beginning
            setCaretPosition(0);
        }
    }

    /*
     * 
     * encapsulate bodytext in HTML code
     *  
     */
    protected String transformToHTML(StringBuffer buf) {
        // prepend
        buf.insert(0, "<HTML><HEAD>" + css
                + "</HEAD><BODY class=\"bodytext\"><P>");

        // append
        buf.append("</P></BODY></HTML>");

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.mail.gui.config.general.MailOptionsDialog
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable arg0, Object arg1) {
        Font font = FontProperties.getTextFont();
        name = font.getName();
        size = new Integer(font.getSize()).toString();

        initStyleSheet();
    }

    /**
     * @see javax.swing.text.JTextComponent#copy()
     */
    public void copy() {
        int start = this.getSelectionStart();
        int stop = this.getSelectionEnd();

        StringWriter htmlSelection = new StringWriter();

        try {
            htmlEditorKit.write(htmlSelection, getDocument(), start, stop
                    - start);

            Clipboard clipboard = getToolkit().getSystemClipboard();

            // Conversion of html text to plain
            //TODO: make a DataFlavor that can handle HTML text
            StringSelection selection = new StringSelection(HtmlParser
                    .htmlToText(htmlSelection.toString()));
            clipboard.setContents(selection, selection);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.columba.mail.gui.message.viewer.Viewer#getView()
     */
    public JComponent getView() {
        return this;
    }
}