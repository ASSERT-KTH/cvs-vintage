package org.jboss.ejb.plugins.cmp.ejbql;

import java.io.PushbackReader;
import java.io.IOException;
import java.io.CharArrayWriter;

public class InputParameterState implements TokenizerState {

   public InputParameterState() {
   }

   public Token nextToken(PushbackReader in, char character, Tokenizer tokenizer)
         throws IOException {
      if(character != '?') {
         throw new IllegalArgumentException("InputParameterState must begin with a '?': c="+character);
      }
      if(in == null) {
         throw new IllegalArgumentException("in is null");
      }
      
      CharArrayWriter out = new CharArrayWriter(4);

      // read all chars in identifier
      int c = in.read();
      
      while(Character.isDigit((char)c)) {
         out.write(c);
         c = in.read();
      }
      
      // unread last char as it is not part of the identifier
      if(c != -1) {
         in.unread(c);
      }
      
      String paramNumString = out.toString();
      
      // assure that we got atleast one character
      if(paramNumString.length()==0) {
         throw new IllegalStateException("In an input parameter a digit must immedately follow the '?': c="+c);
      }
      int paramNum = Integer.parseInt(paramNumString);
      
      return new InputParameterToken(paramNum);
   }
}
