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
package org.columba.mail.parser;

import java.io.StringWriter;
import java.util.StringTokenizer;

import org.columba.mail.coder.EncodedWordDecoder;
import org.columba.mail.message.MimeHeader;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.Rfc822Header;


public class MimeParser extends AbstractParser {

    String[] message;
    Rfc822Header rfc822Header;
    int action;

    public MimeParser()
    {
    }


    public MimeParser( Rfc822Header h, int action )
    {
        this.rfc822Header = h;
        this.action = action;
    }

    public boolean checkMimeVer( String mimeVer )
        {
            return mimeVer.equals("1.0");
        }

    public MimePart parse( String input )
    {
        MimeHeader mimeHeader;
        MimeTypeParser typeParser;
        MimePart attachments = null;

        message = divideMessage( input );

        if( message != null ) {
            mimeHeader = parseHeader( message[0] );
            typeParser = MimeRouter.getInstance().getTypeParser( mimeHeader );
//            typeParser.setHeader( rfc822Header );
//            typeParser.setAction( action );
            attachments = typeParser.parse( mimeHeader, message[1] );
        }

	//System.out.println("Parsed Body:");
	//System.out.println( attachments.get(0).getBody() );

        return attachments;
    }

    public MimeHeader parseHeader(String input)
    {
        MimeHeader output = new MimeHeader();
        String actParam;
        int charPos;
        EncodedWordDecoder decoder = new EncodedWordDecoder();
        StringWriter decodedString;


        if( input == null) return output;

        StringTokenizer paramTokenizer;
        HeaderTokenizer lineTokenizer = new HeaderTokenizer(input);
        String actLine = lineTokenizer.nextLine();

        while( actLine != null ) {

              // Parsing the Mime-Content-Types

            if( actLine.startsWith("Content-Type") ) {
                charPos = actLine.indexOf(':');
                if( charPos != -1 ) {
                    paramTokenizer = new StringTokenizer(actLine.substring(charPos+1),";");
                    actParam = paramTokenizer.nextToken().trim();
                    charPos = actParam.indexOf('/');

                    if( charPos != -1 ) {
                        output.contentType = actParam.substring(0,charPos).trim().toLowerCase();
                        output.contentSubtype = actParam.substring(charPos+1).trim().toLowerCase();
                    }

                    while( paramTokenizer.hasMoreTokens() ) {
                        actParam = paramTokenizer.nextToken().trim();
                        charPos = actParam.indexOf('=');
                        if( charPos != -1 ) {
                            output.putContentParameter(
                                actParam.substring(0,charPos).trim().toLowerCase(), decoder.decode( actParam.substring(charPos+1).trim() )
                                 );
                        }
                    }

                }
            }
            else if( actLine.startsWith("Content-Transfer-Encoding") ) {
                charPos = actLine.indexOf(':');
                if( charPos != -1 ) {
                    output.contentTransferEncoding = actLine.substring(charPos+1).trim().toLowerCase();
                }
            }
            else if( actLine.startsWith("Content-ID") ) {
                charPos = actLine.indexOf(':');
                if( charPos != -1 ) {
                    output.contentID = actLine.substring(charPos+1).trim();
                }
            }
            else if( actLine.startsWith("Content-Description") ) {
                charPos = actLine.indexOf(':');
                if( charPos != -1 ) {
                    output.contentDescription = actLine.substring(charPos+1).trim();
                }
            }
            else if( actLine.startsWith("Content-Disposition") ) {
                charPos = actLine.indexOf(':');
                if( charPos != -1 ) {
                    paramTokenizer = new StringTokenizer(actLine.substring(charPos+1),";");
                    actParam = paramTokenizer.nextToken().trim();
                    output.contentDisposition = actParam.trim().toLowerCase();

                    while( paramTokenizer.hasMoreTokens() ) {
                        actParam = paramTokenizer.nextToken().trim();
                        charPos = actParam.indexOf('=');
                        if( charPos != -1 ) {
                            output.putDispositionParameter(
                                actParam.substring(0,charPos).trim(),
                                decoder.decode( actParam.substring(charPos+1).trim() ));
                        }
                    }
                }
            }

              // End of Parsing
            actLine = lineTokenizer.nextLine();
        }

        return output;
    }


}










