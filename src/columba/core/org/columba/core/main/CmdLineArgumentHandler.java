/*
 * Created on Apr 28, 2003
 * File CmdLineArgumentHandler.java
 * 
 */
package org.columba.core.main;

import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.parser.MailUrlParser;

/**
 * This class handles given arguments (in style of commandline arguments. If for example the 
 * argument --composer is given, on startup the composer window is viewed. All other arguments like
 * subject ect. also given to the composer, so any values to write a mail can given here as 
 * arguments and then a composer window with all values are opened.
 * TODO: test, why any commandline options are not seen in the composer window (eg. subject).
 * @author waffel
 */
public class CmdLineArgumentHandler {
  
  /**
   * Constructs a new CommandLineArgumentHandler. This Handler parsed the given commandline 
   * Options an if needed starts a composer window. If any commandlineargument unknown a message
   * is printed out to the error console and the system will exit.
   * @param args Commandline Arguments to be parsed.
   */
  public CmdLineArgumentHandler(String[] args) {
    ColumbaCmdLineParser cmdLineParser = new ColumbaCmdLineParser();
    cmdLineParser.initCmdLine(args);
    ColumbaLogger.log.debug("cmdLineArgumentHandler");
    
    String mailURL = cmdLineParser.getMailurlOption();
    if ( mailURL != null) {
      if (MailUrlParser.isMailUrl(mailURL)) {
        MailUrlParser mailToParser = new MailUrlParser(mailURL);
        cmdLineParser.setComposerOption(true);
        cmdLineParser.setRcptOption((String)mailToParser.get("mailto:"));
        cmdLineParser.setSubjectOption((String)mailToParser.get("subject="));
        cmdLineParser.setCcOption((String)mailToParser.get("cc="));
        cmdLineParser.setBccOption((String)mailToParser.get("bcc="));
        cmdLineParser.setBodyOption((String)mailToParser.get("body="));
      }
    }
    ColumbaLogger.log.debug("Option Debug: "+cmdLineParser.getComposerOption());
    ColumbaLogger.log.debug("Option subject: "+cmdLineParser.getSubjectOption());
    if (cmdLineParser.getComposerOption()) {
      ComposerController controller = new ComposerController();
      if (cmdLineParser.getRcptOption() != null) {
        controller.getModel().setTo(cmdLineParser.getRcptOption());
      }
      if (cmdLineParser.getSubjectOption() != null) {
        controller.getModel().setSubject(cmdLineParser.getSubjectOption());
      }
      if (cmdLineParser.getCcOption() != null) {
        controller.getModel().setHeaderField("Cc", cmdLineParser.getCcOption());
      }
      if (cmdLineParser.getBccOption() != null) {
        controller.getModel().setHeaderField("Bcc", cmdLineParser.getBccOption());
      }
      if (cmdLineParser.getBodyOption() != null) {
        controller.getModel().setBodyText(cmdLineParser.getBodyOption());
      }
      controller.openView();
    }
  }
}
