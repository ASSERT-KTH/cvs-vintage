From MAILER-DAEMON Thu Jan 20 00:37:28 2000
Date: Thu, 20 Jan 2000 00:37:28 -0800 (PST)
From: Mail System Internal Data <MAILER-DAEMON@costin>
Subject: DON'T DELETE THIS MESSAGE -- FOLDER INTERNAL DATA
X-IMAP: 0948357448 0000000000
Status: RO

This text is part of the internal format of your mail folder, and is not
a real message.  It is created automatically by the mail system software.
If deleted, important folder data will be lost, and it will be re-created
with the data reset to initial values.

From costin  Thu Jan 20 00:14:35 2000
Received: from localhost
	by localhost with IMAP (fetchmail-5.1.0)
	for costin@localhost (single-drop); Thu, 20 Jan 2000 00:14:35 -0800 (PST)
Received: from locus.apache.org ([63.211.145.10])
	by costin.dnt.ro (8.9.3+Sun/8.9.1) with SMTP id HAA12921
	for <costin@costin.dnt.ro>; Thu, 20 Jan 2000 07:18:42 -0800 (PST)
Received: (qmail 77679 invoked by uid 500); 20 Jan 2000 15:18:38 -0000
Mailing-List: contact tomcat-dev-help@jakarta.apache.org; run by ezmlm
Precedence: bulk
X-No-Archive: yes
list-help: <mailto:tomcat-dev-help@jakarta.apache.org>
list-unsubscribe: <mailto:tomcat-dev-unsubscribe@jakarta.apache.org>
list-post: <mailto:tomcat-dev@jakarta.apache.org>
Reply-To: tomcat-dev@jakarta.apache.org
Delivered-To: mailing list tomcat-dev@jakarta.apache.org
Delivered-To: moderator for tomcat-dev@jakarta.apache.org
Received: (qmail 467 invoked from network); 20 Jan 2000 04:35:34 -0000
X-Authentication-Warning: csmail.net: Host ppp-62.newinet.net [208.11.194.62] claimed to be dustin
Message-ID: <001a01bf6300$1ea93a20$3ec20bd0@sourcestream.com>
From: "Dustin Callaway" <callaway@sourcestream.com>
To: <tomcat-dev@jakarta.apache.org>
Subject: SimpleStartup.java fix...
Date: Wed, 19 Jan 2000 21:37:50 -0700
MIME-Version: 1.0
Content-Type: multipart/alternative;
	boundary="----=_NextPart_000_0017_01BF62C5.6F91AAC0"
X-Priority: 3
X-MSMail-Priority: Normal
X-Mailer: Microsoft Outlook Express 5.00.2314.1300
X-MimeOLE: Produced By Microsoft MimeOLE V5.00.2314.1300
Content-Length: 1982
Status: RO
X-Status: 
X-Keywords:

------=_NextPart_000_0017_01BF62C5.6F91AAC0
Content-Type: text/plain;
	charset="iso-8859-1"
Content-Transfer-Encoding: quoted-printable

The following SimpleStartup.java class starts the Tomcat HttpServer from =
within a Java application. This allows for servlet debugging from within =
an IDE. Many thanks to Jim Rudnicki for the sample code (which I just =
cleaned up and simplified). Place this application in the /tomcat =
directory and run it from there.


import org.apache.tomcat.server.HttpServer;
import java.net.URL;

/**
 * SimpleStartup starts the Tomcat HttpServer in a Java
 * application to allow for debugging within an IDE.
 */
public class SimpleStartup
{
  public static void main(String[] args)
  {
    try
    {
      HttpServer server =3D new HttpServer(8080, null, null);

      URL url =3D resolveURL("webpages");
      server.getContextManager().setDocumentBase(url);

      url =3D resolveURL("examples");
      server.addContext("/examples", url);

      server.start(); //start the server
    }
    catch (Exception e)
    {
      System.out.println("Error: " + e);
    }
  }


  private static URL resolveURL(String s) throws Exception
  {
    // if the string contains :/, then we assume that it's a real URL =
and do nothing
    if (s.indexOf(":/") > -1)
    {
        return new URL(s);
    }

    // otherwise, we assume that we've got a file name and
    // need to construct a file url appropriatly.
    if (s.startsWith("/"))
    {
      return new URL("file", null, s);
    }
    else
    {
      String pwd =3D System.getProperty("user.dir");
      return new URL("file", null, pwd + "/" + s);
    }
  }
}


That's it. Register your servlet in the web.xml file, set a breakpoint =
in your servlet using an IDE (JBuilder, Visual Cafe, etc.), and invoke =
the servlet from a browser (execution should stop at your breakpoint). =
Thanks for the tip, Jim!

Dustin Callaway
(callaway@sourcestream.com)

------=_NextPart_000_0017_01BF62C5.6F91AAC0--

