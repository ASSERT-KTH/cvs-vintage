/*
 * Copyright 1999,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.net;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.SyslogQuietWriter;
import org.apache.log4j.helpers.SyslogWriter;
import org.apache.log4j.spi.LoggingEvent;


// Contributors: Yves Bossel <ybossel@opengets.cl>
//               Christopher Taylor <cstaylor@pacbell.net>


/**
 * Use SyslogAppender to send log messages to a remote syslog daemon.
 * @author Ceki G&uuml;lc&uuml;
 * @author Anders Kristensen
 */
public class SyslogAppender extends AppenderSkeleton {
  // The following constants are extracted from a syslog.h file
  // copyrighted by the Regents of the University of California
  // I hope nobody at Berkley gets offended.

  /** Kernel messages */
  public static final int LOG_KERN = 0;

  /** Random user-level messages */
  public static final int LOG_USER = 1 << 3;

  /** Mail system */
  public static final int LOG_MAIL = 2 << 3;

  /** System daemons */
  public static final int LOG_DAEMON = 3 << 3;

  /** security/authorization messages */
  public static final int LOG_AUTH = 4 << 3;

  /** messages generated internally by syslogd */
  public static final int LOG_SYSLOG = 5 << 3;

  /** line printer subsystem */
  public static final int LOG_LPR = 6 << 3;

  /** network news subsystem */
  public static final int LOG_NEWS = 7 << 3;

  /** UUCP subsystem */
  public static final int LOG_UUCP = 8 << 3;

  /** clock daemon */
  public static final int LOG_CRON = 9 << 3;

  /** security/authorization  messages (private) */
  public static final int LOG_AUTHPRIV = 10 << 3;

  /** ftp daemon */
  public static final int LOG_FTP = 11 << 3;

  // other codes through 15 reserved for system use


  /** reserved for local use */
  public static final int LOG_LOCAL0 = 16 << 3;

  /** reserved for local use */
  public static final int LOG_LOCAL1 = 17 << 3;

  /** reserved for local use */
  public static final int LOG_LOCAL2 = 18 << 3;

  /** reserved for local use */
  public static final int LOG_LOCAL3 = 19 << 3;

  /** reserved for local use */
  public static final int LOG_LOCAL4 = 20 << 3;

  /** reserved for local use */
  public static final int LOG_LOCAL5 = 21 << 3;

  /** reserved for local use */
  public static final int LOG_LOCAL6 = 22 << 3;

  /** reserved for local use*/
  public static final int LOG_LOCAL7 = 23 << 3;
  protected static final int SYSLOG_HOST_OI = 0;
  protected static final int FACILITY_OI = 1;
  static final String TAB = "    ";

  // Have LOG_USER as default
  int syslogFacility = LOG_USER;
  String facilityStr;
  boolean facilityPrinting = false;

  //SyslogTracerPrintWriter stp;
  SyslogQuietWriter sqw;
  String syslogHost;

  public SyslogAppender() {
    this.initSyslogFacilityStr();
  }

  public SyslogAppender(Layout layout, int syslogFacility) {
    this.layout = layout;
    this.syslogFacility = syslogFacility;
    this.initSyslogFacilityStr();
  }

  public SyslogAppender(Layout layout, String syslogHost, int syslogFacility) {
    this(layout, syslogFacility);
    setSyslogHost(syslogHost);
  }

  /**
   * Release any resources held by this SyslogAppender.
   * @since 0.8.4
   */
  public synchronized void close() {
    closed = true;

    // A SyslogWriter is UDP based and needs no opening. Hence, it
    // can't be closed. We just unset the variables here.
    sqw = null;
  }

  private void initSyslogFacilityStr() {
    facilityStr = getFacilityString(this.syslogFacility);

    if (facilityStr == null) {
      System.err.println(
        "\"" + syslogFacility
        + "\" is an unknown syslog facility. Defaulting to \"USER\".");
      this.syslogFacility = LOG_USER;
      facilityStr = "user:";
    } else {
      facilityStr += ":";
    }
  }

  /**
   * Returns the specified syslog facility as a lower-case String, e.g. "kern", 
   * "user", etc. 
   * */
  public static String getFacilityString(int syslogFacility) {
    switch (syslogFacility) {
    case LOG_KERN:
      return "kern";
    case LOG_USER:
      return "user";
    case LOG_MAIL:
      return "mail";
    case LOG_DAEMON:
      return "daemon";
    case LOG_AUTH:
      return "auth";
    case LOG_SYSLOG:
      return "syslog";
    case LOG_LPR:
      return "lpr";
    case LOG_NEWS:
      return "news";
    case LOG_UUCP:
      return "uucp";
    case LOG_CRON:
      return "cron";
    case LOG_AUTHPRIV:
      return "authpriv";
    case LOG_FTP:
      return "ftp";
    case LOG_LOCAL0:
      return "local0";
    case LOG_LOCAL1:
      return "local1";
    case LOG_LOCAL2:
      return "local2";
    case LOG_LOCAL3:
      return "local3";
    case LOG_LOCAL4:
      return "local4";
    case LOG_LOCAL5:
      return "local5";
    case LOG_LOCAL6:
      return "local6";
    case LOG_LOCAL7:
      return "local7";
    default:
      return null;
    }
  }

  /**
   * Returns the integer value corresponding to the named syslog facility, 
   * or -1 if it couldn't be recognized.
   * 
   * @param facilityName one of the case-insensitive strings KERN, USER, MAIL, 
   * DAEMON, AUTH, SYSLOG, LPR, NEWS, UUCP, CRON, AUTHPRIV, FTP, LOCAL0, LOCAL1, 
   * LOCAL2, LOCAL3, LOCAL4, LOCAL5, LOCAL6, LOCAL7. 
   * @since 1.1 
   * */
  public static int getFacility(String facilityName) {
    if (facilityName != null) {
      facilityName = facilityName.trim();
    }

    if ("KERN".equalsIgnoreCase(facilityName)) {
      return LOG_KERN;
    } else if ("USER".equalsIgnoreCase(facilityName)) {
      return LOG_USER;
    } else if ("MAIL".equalsIgnoreCase(facilityName)) {
      return LOG_MAIL;
    } else if ("DAEMON".equalsIgnoreCase(facilityName)) {
      return LOG_DAEMON;
    } else if ("AUTH".equalsIgnoreCase(facilityName)) {
      return LOG_AUTH;
    } else if ("SYSLOG".equalsIgnoreCase(facilityName)) {
      return LOG_SYSLOG;
    } else if ("LPR".equalsIgnoreCase(facilityName)) {
      return LOG_LPR;
    } else if ("NEWS".equalsIgnoreCase(facilityName)) {
      return LOG_NEWS;
    } else if ("UUCP".equalsIgnoreCase(facilityName)) {
      return LOG_UUCP;
    } else if ("CRON".equalsIgnoreCase(facilityName)) {
      return LOG_CRON;
    } else if ("AUTHPRIV".equalsIgnoreCase(facilityName)) {
      return LOG_AUTHPRIV;
    } else if ("FTP".equalsIgnoreCase(facilityName)) {
      return LOG_FTP;
    } else if ("LOCAL0".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL0;
    } else if ("LOCAL1".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL1;
    } else if ("LOCAL2".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL2;
    } else if ("LOCAL3".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL3;
    } else if ("LOCAL4".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL4;
    } else if ("LOCAL5".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL5;
    } else if ("LOCAL6".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL6;
    } else if ("LOCAL7".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL7;
    } else {
      return -1;
    }
  }

  public void append(LoggingEvent event) {
    if (!isAsSevereAsThreshold(event.getLevel())) {
      return;
    }

    // We must not attempt to append if sqw is null.
    if (sqw == null) {
      errorHandler.error(
        "No syslog host is set for SyslogAppedender named \"" + this.name
        + "\".");

      return;
    }

    String buffer =
      (facilityPrinting ? facilityStr : "") + layout.format(event);

    sqw.setLevel(event.getLevel().getSyslogEquivalent());
    sqw.write(buffer);

    String[] s = event.getThrowableStrRep();

    if (s != null) {
      int len = s.length;

      if (len > 0) {
        sqw.write(s[0]);

        for (int i = 1; i < len; i++) {
          sqw.write(TAB + s[i].substring(1));
        }
      }
    }
  }

  /**
   * This method returns immediately as options are activated when they are set.
   * */
  public void activateOptions() {
  }

  /**
   * The SyslogAppender requires a layout. Hence, this method returns 
   * <code>true</code>.
   * 
   * @since 0.8.4 
   * */
  public boolean requiresLayout() {
    return true;
  }

  /**
   * The <b>SyslogHost</b> option is the name of the the syslog host where log 
   * output should go.
   * 
   * <b>WARNING</b> If the SyslogHost is not set, then this appender will fail.
   */
  public void setSyslogHost(String syslogHost) {
    this.sqw =
      new SyslogQuietWriter(
        new SyslogWriter(syslogHost), syslogFacility, errorHandler);

    //this.stp = new SyslogTracerPrintWriter(sqw);
    this.syslogHost = syslogHost;
  }

  /**
   * Returns the value of the <b>SyslogHost</b> option.
   */
  public String getSyslogHost() {
    return syslogHost;
  }

  /**
   * Set the syslog facility. This is the <b>Facility</b> option.
   * 
   * <p>The <code>facilityName</code> parameter must be one of the strings KERN,
   * USER, MAIL, DAEMON, AUTH, SYSLOG, LPR, NEWS, UUCP, CRON, AUTHPRIV, FTP, 
   * LOCAL0, LOCAL1, LOCAL2, LOCAL3, LOCAL4, LOCAL5, LOCAL6, LOCAL7. Case is 
   * unimportant.
   * 
   * @since 0.8.1 
   * */
  public void setFacility(String facilityName) {
    if (facilityName == null) {
      return;
    }

    syslogFacility = getFacility(facilityName);

    if (syslogFacility == -1) {
      System.err.println(
        "[" + facilityName
        + "] is an unknown syslog facility. Defaulting to [USER].");
      syslogFacility = LOG_USER;
    }

    this.initSyslogFacilityStr();

    // If there is already a sqw, make it use the new facility.
    if (sqw != null) {
      sqw.setSyslogFacility(this.syslogFacility);
    }
  }

  /**
   * Returns the value of the <b>Facility</b> option.
   */
  public String getFacility() {
    return getFacilityString(syslogFacility);
  }

  /**
   * If the <b>FacilityPrinting</b> option is set to true, the printed message 
   * will include the facility name of the application. It is <em>false</em> by 
   * default.
   */
  public void setFacilityPrinting(boolean on) {
    facilityPrinting = on;
  }

  /**
   * Returns the value of the <b>FacilityPrinting</b> option.
   */
  public boolean getFacilityPrinting() {
    return facilityPrinting;
  }
}
