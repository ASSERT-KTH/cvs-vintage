package org.jboss.ejb.plugins.cmp.ejbql;

import java.io.PushbackReader;
import java.io.IOException;

public class WhitespaceState implements TokenizerState {

   public WhitespaceState() {
   }

   public Token nextToken(PushbackReader in, char character, Tokenizer tokenizer)
         throws IOException {
            
      for(int c = in.read(); c != -1; c = in.read()) {
         // get the state for the next character
         TokenizerState state = tokenizer.getCharacterState((char)c);
         
         // if we are NOT the state for the next character, call the 
         // next state, else loop
         if(state != this) {
            return state.nextToken(in, (char)c, tokenizer);
         }
      }
      // white space to the end of the stream
      return null;
   }
}
