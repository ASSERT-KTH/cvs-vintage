Eudora Mail Import Filter plugin for Columba
============================================

Author:     Karl Peder Olesen
E-mail:     kpo@mail1.stofanet.dk
Version:    1.0 beta
Date:       2003-05-21

1. Status
---------
Version 1.0 beta is the first version of this plugin. This means that it has
been developed and tested, but only for:
    Eudora Pro 4.0
    Windows 2000
    Java 1.4 (should be usable with Java 1.3 also, else let me know!!)
This is the platform I have access to. People willing to do further testing
are VERY welcome, and I will appreciate ANY feedback!


2. Motivation
-------------
I believe that it's important to be able to read old mails, even after shifting
to a new and exciting mail client. Therefore import facilities are very 
important. I needed it for Eudora, and possibly others do as well. No one has
so far written the necessary plugin / code (as far as I know).
Therefore I did!


3. Installation and usage
-------------------------
Place EudoraMailImportFilter.jar and plugin.xml in the folder:
    <columba install>/org.columba.mail.EudoraMailImportFilter/
Columba then automatically registers the plugin upon startup.
In Columba, go the the Utilities => Import mailbox menu. The plugin should
now be available in the list of possible import filters.


4. What it does
---------------
Using the default mbox importer in Columba for Eudora mail boxes works well as
long as the mails are: 1) plain text, 2) without attachments and 3) from 
the Eudora inbox. The problems are:
1) Some html mails are marked with Content-Type: multipart/alternative. 
   Eudora only stores the html part and has deleted the boundaries, which 
   should be present in multipart messages
2) Eudora decodes attachments (and places them in a separate attachment
   folder), but the messages are still marked with
   Content-Type: multipart/mixed - again with no boundaries.
   For outgoing messages, the names of attachments are stored in a 
   X-Attachments header.
3) Outgoing messages are not stored with a proper Date header.

This plugin tries to solve these problems as described below.

4.1 Handling multipart/alternative messages
-------------------------------------------
If a message with Content-Type: multipart/alternative is found during import,
and the boundaries are missing, the plugin tries to guess the proper
Content-type (text/plain or text/html).

4.2 Handling attachments
------------------------
The plugin finds the file names of the original attachments and creates a
multipart message, where the 1st part is the original message and the 2nd part
is html containing a list of links to the attachments (shown in Columba as an
attachment, which can be opened in a browser).

4.3 Recreating Date header
--------------------------
If missing, the Date header is recreated (the date is indeed stored by
Eudora, but not in the proper format).

4.4 Charsets
------------
For html messages it scans the message for a <meta> tag containing the charset
used. If not found, or if it's a plain text message, the charset is set to
system default. I'm not quite sure whether this does any difference to
Columba, but it doesn't seem to harm anyway.


Using the plugin, most of the messages that I was able to test on was imported
correct - or at least much better than using the default mbox importer.


Hope it will be usefull...

21st of May 2003, Aalborg, Denmark
Karl Peder Olesen
