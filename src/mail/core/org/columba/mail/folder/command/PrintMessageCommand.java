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
package org.columba.mail.folder.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.io.DiskIO;
import org.columba.core.io.StreamUtils;
import org.columba.core.io.TempFileStore;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.core.print.cCmUnit;
import org.columba.core.print.cDocument;
import org.columba.core.print.cHGroup;
import org.columba.core.print.cHTMLPart;
import org.columba.core.print.cLine;
import org.columba.core.print.cParagraph;
import org.columba.core.print.cPrintObject;
import org.columba.core.print.cPrintVariable;
import org.columba.core.print.cVGroup;
import org.columba.core.xml.XmlElement;

import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.attachment.AttachmentModel;
import org.columba.mail.main.MailInterface;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.parser.text.HtmlParser;
import org.columba.mail.util.MailResourceLoader;

import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.CharsetDecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.StreamableMimePart;
import org.columba.ristretto.message.io.CharSequenceSource;

import java.awt.Color;
import java.awt.Font;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.Array;

import java.net.MalformedURLException;
import java.net.URL;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;


/**
 * Print the selected message.
 *
 * @author karlpeder
 */
public class PrintMessageCommand extends FolderCommand {
    private cPrintObject mailHeader;
    private cPrintObject mailFooter;
    private DateFormat mailDateFormat;
    private String[] headerKeys = { "From", "To", "Date", "Subject" };
    private String dateHeaderKey = "Date"; // the header key for date field
    private String attHeaderKey = "attachment";
    private Charset charset;

    /**
         * Constructor for PrintMessageCommdn.
         *
         * @param frameMediator
         * @param references
         */
    public PrintMessageCommand(DefaultCommandReference[] references,
        Charset charset) {
        super(references);
        this.charset = charset;

        // Header
        cParagraph columbaParagraph = new cParagraph();
        columbaParagraph.setText("The Columba Project");
        columbaParagraph.setColor(Color.lightGray);
        columbaParagraph.setFontStyle(Font.BOLD);

        cParagraph link = new cParagraph();
        link.setText(" - http://sourceforge.columba.net");
        link.setTextAlignment(cParagraph.LEFT);
        link.setLeftMargin(columbaParagraph.getSize(new cCmUnit(100)).getWidth());
        link.setColor(Color.lightGray);

        cPrintVariable date = new cPrintVariable();
        date.setCodeString("%DATE_TODAY%");
        date.setTextAlignment(cParagraph.RIGHT);
        date.setColor(Color.lightGray);

        cHGroup headerText = new cHGroup();
        headerText.add(columbaParagraph);
        headerText.add(link);
        headerText.add(date);

        cLine headerLine = new cLine();

        headerLine.setThickness(1);
        headerLine.setColor(Color.lightGray);
        headerLine.setTopMargin(new cCmUnit(0.1));

        cVGroup header = new cVGroup();
        header.add(headerText);
        header.add(headerLine);
        header.setBottomMargin(new cCmUnit(0.5));

        mailHeader = header;

        // Footer
        cPrintVariable footer = new cPrintVariable();
        footer.setTextAlignment(cParagraph.CENTER);
        footer.setCodeString("%PAGE_NR% / %PAGE_COUNT%");
        footer.setTopMargin(new cCmUnit(0.5));
        footer.setColor(Color.lightGray);

        mailFooter = footer;

        // DateFormat
        mailDateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
                DateFormat.MEDIUM);
    }

    public cPrintObject getMailHeader() {
        return mailHeader;
    }

    public cPrintObject getMailFooter() {
        return mailFooter;
    }

    public String[] getHeaderKeys() {
        return headerKeys;
    }

    public DateFormat getMailDateFormat() {
        return mailDateFormat;
    }

    /**
         * @see org.columba.core.command.Command#updateGUI()
         */
    public void updatedGUI() throws Exception {
    }

    /**
         * This method executes the print action, i.e. it prints the selected
         * messages.
         *
         * @see org.columba.core.command.Command#execute(Worker)
         */
    public void execute(WorkerStatusController worker)
        throws Exception {
        /*
                 * *20030604, karlpeder* Fixed minor flaws to be able to print text
                 * messages. Further more added support for html messages.
                 */
        FolderCommandReference[] r = (FolderCommandReference[]) getReferences();

        Object[] uids = r[0].getUids(); // uid for messages to print

        Folder srcFolder = (Folder) r[0].getFolder();

        //		register for status events
        ((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

        // Print each message
        for (int j = 0; j < uids.length; j++) {
            Object uid = uids[j];
            ColumbaLogger.log.info("Printing UID=" + uid);

            ColumbaMessage message = new ColumbaMessage();
            Header header = srcFolder.getHeaderFields(uids[j], getHeaderKeys());

            MimeTree mimePartTree = srcFolder.getMimePartTree(uid);

            // Does the user prefer html or plain text?
            XmlElement html = MailInterface.config.getMainFrameOptionsConfig()
                                                  .getRoot().getElement("/options/html");
            boolean viewhtml = Boolean.valueOf(html.getAttribute("prefer"))
                                      .booleanValue();

            // Which Bodypart shall be shown? (html/plain)
            MimePart bodyPart = null;

            if (viewhtml) {
                bodyPart = mimePartTree.getFirstTextPart("html");
            } else {
                bodyPart = mimePartTree.getFirstTextPart("plain");
            }

            if (bodyPart == null) {
                bodyPart = new LocalMimePart(new MimeHeader(header));
                ((LocalMimePart) bodyPart).setBody(new CharSequenceSource(
                        "<No Message-Text>"));
            } else {
                bodyPart = srcFolder.getMimePart(uid, bodyPart.getAddress());
            }

            // Setup print document for message
            cDocument messageDoc = new cDocument();
            messageDoc.setHeader(getMailHeader());
            messageDoc.setFooter(getMailFooter());

            String[] headerKeys = getHeaderKeys();
            cParagraph hKey;
            cParagraph hValue;
            cHGroup hLine;
            Object value;

            // Add header information to print
            for (int i = 0; i < Array.getLength(headerKeys); i++) {
                hKey = new cParagraph();

                // *20030531, karlpeder* setting headerKeys to lowercase for
                // lookup!
                hKey.setText(MailResourceLoader.getString("header",
                        headerKeys[i].toLowerCase()));
                hKey.setFontStyle(Font.BOLD);

                hValue = new cParagraph();

                /*
                 * *20031216, karlpeder* Changed handling of dates.
                 * Previously columba.date header was used. Now we
                 * use the Date header instead
                 */
                //if (headerKeys[i].equalsIgnoreCase("date")) {
                //    value = header.get("columba.date");
                //} else {
                //	value = header.get(headerKeys[i]);
                //}
                value = header.get(headerKeys[i]);

                if (headerKeys[i].equalsIgnoreCase(dateHeaderKey)) {
                    // special handling for dates
                    SimpleDateFormat formatter = new SimpleDateFormat(
                            "d MMM yyyy HH:mm:ss Z");
                    String dateStr = (String) value;

                    // ignore leading weekday name (e.g. "Mon,"), since this
                    // seems to give problems during parsing 
                    ParsePosition pos = new ParsePosition(dateStr.indexOf(',') +
                            1);
                    Date d = formatter.parse((String) value, pos);

                    if (d != null) {
                        hValue.setText(getMailDateFormat().format(d));
                    } else {
                        // fall back to use the Date header contents directly
                        hValue.setText((String) value);
                    }
                } else {
                    hValue.setText((String) value);
                }

                hValue.setLeftMargin(new cCmUnit(3.0));

                hLine = new cHGroup();
                hLine.add(hKey);
                hLine.add(hValue);

                messageDoc.appendPrintObject(hLine);
            }

            // Add list of attachments if applicable
            AttachmentModel attMod = new AttachmentModel();
            attMod.setCollection(mimePartTree);

            List attachments = attMod.getDisplayedMimeParts();

            for (int i = 0; i < attachments.size(); i++) {
                StreamableMimePart mp = (StreamableMimePart) attachments.get(i);
                String contenttype = mp.getHeader().getMimeType().getType();
                String contentSubtype = mp.getHeader().getMimeType().getSubtype();

                if (mp.getHeader().getFileName() != null) {
                    // one line is added to the header for each attachment
                    // (which has a filename defined)
                    hKey = new cParagraph();
                    hKey.setText(MailResourceLoader.getString("header",
                            attHeaderKey));
                    hKey.setFontStyle(Font.BOLD);

                    hValue = new cParagraph();
                    hValue.setText(mp.getHeader().getFileName());
                    hValue.setLeftMargin(new cCmUnit(3.0));

                    hLine = new cHGroup();
                    hLine.add(hKey);
                    hLine.add(hValue);

                    messageDoc.appendPrintObject(hLine);
                }
            }

            // Add body of message to print
            String mimesubtype = bodyPart.getHeader().getMimeType().getSubtype();

            if (mimesubtype.equals("html")) {
                messageDoc.appendPrintObject(getHTMLBodyPrintObject(
                        (StreamableMimePart) bodyPart));
            } else {
                messageDoc.appendPrintObject(getPlainBodyPrintObject(
                        (StreamableMimePart) bodyPart));
            }

            // print the print document (i.e. the message)
            messageDoc.print();
        }

        // end of for loop over uids to print
    }

    /**
         * Private utility to create a print object representing the body of a
         * plain text message. The messagebody is decoded according to present
         * charset. <br>Precondition: Mime subtype is "plain".
         *
         * @param bodyPart
         *            Body part of message
         * @return Print object ready to be appended to the print document
         * @author Karl Peder Olesen (karlpeder), 20030531
         */
    private cPrintObject getPlainBodyPrintObject(StreamableMimePart bodyPart)
        throws IOException {
        // decode message body with respect to charset
        String decodedBody = getDecodedMessageBody(bodyPart);

        // create a print object and return it
        cParagraph printBody = new cParagraph();
        printBody.setTopMargin(new cCmUnit(1.0));
        printBody.setText(decodedBody);

        return printBody;
    }

    /**
         * retrieve printer options from configuration file
         *
         * @return true, if scaling is allowed false, otherwise
         */
    protected boolean isScalingAllowed() {
        XmlElement options = MainInterface.config.get("options").getElement("/options");
        XmlElement printer = null;

        if (options != null) {
            printer = options.getElement("/printer");
        }

        // no configuration available, create default config
        if (printer == null) {
            // create new local xml treenode
            ColumbaLogger.log.info(
                "printer config node not found - creating new");
            printer = new XmlElement("printer");
            printer.addAttribute("allow_scaling", "true");

            // add to options if possible (so it will be saved)
            if (options != null) {
                ColumbaLogger.log.info("storing new printer config node");
                options.addElement(printer);
            }
        }

        return Boolean.valueOf(printer.getAttribute("allow_scaling", "true"))
                      .booleanValue();
    }

    /**
         * Private utility to create a print object representing the body of a html
         * message. <br>Precondition: Mime subtype is "html".
         *
         * @param bodyPart
         *            Body part of message
         * @return Print object ready to be appended to the print document
         * @author Karl Peder Olesen (karlpeder), 20030531
         */
    private cPrintObject getHTMLBodyPrintObject(StreamableMimePart bodyPart)
        throws IOException {
        // decode message body with respect to charset
        String decodedBody = getDecodedMessageBody(bodyPart);

        // try to fix broken html-strings
        String validated = HtmlParser.validateHTMLString(decodedBody);

        try {
            // create temporary file and save validated body
            File tempFile = TempFileStore.createTempFileWithSuffix("html");
            DiskIO.saveStringInFile(tempFile, validated);

            URL url = tempFile.toURL();

            boolean allowScaling = isScalingAllowed();
            cHTMLPart htmlBody = new cHTMLPart(allowScaling);

            // true ~ scaling allowed
            htmlBody.setTopMargin(new cCmUnit(1.0));
            htmlBody.setHTML(url);

            return htmlBody;
        } catch (MalformedURLException e) {
            ColumbaLogger.log.severe("Error loading html for print: " +
                e.getMessage());

            return null;
        } catch (IOException e) {
            ColumbaLogger.log.severe("Error loading html for print: " +
                e.getMessage());

            return null;
        }
    }

    /**
         * Private utility to decode the message body with the proper charset
         *
         * @param bodyPart
         *            The body of the message
         * @return Decoded message body
         * @author Karl Peder Olesen (karlpeder), 20030601
         */
    private String getDecodedMessageBody(StreamableMimePart bodyPart)
        throws IOException {
        MimeHeader header = bodyPart.getHeader();

        // Decode message according to charset
        InputStream bodyStream = bodyPart.getInputStream();
        int encoding = header.getContentTransferEncoding();

        switch (encoding) {
        case MimeHeader.QUOTED_PRINTABLE: {
            bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);

            break;
        }

        case MimeHeader.BASE64: {
            bodyStream = new Base64DecoderInputStream(bodyStream);

            break;
        }
        }

        // First determine which charset to use
        if (charset == null) {
            // get charset from message
            try {
                charset = Charset.forName(bodyPart.getHeader()
                                                  .getContentParameter("charset"));
            } catch (UnsupportedCharsetException ex) {
                // decode using default charset
                ColumbaLogger.log.severe("The charset " + ex.getCharsetName() +
                    " is not supported. " +
                    "System default encoding will be used.");
                charset = Charset.forName(System.getProperty("file.encoding"));
            }
        }

        bodyStream = new CharsetDecoderInputStream(bodyStream, charset);

        return StreamUtils.readInString(bodyStream).toString();
    }
}
