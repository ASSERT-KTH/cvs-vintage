package org.jboss.ejb.plugins.cmp.ejbql;

import java.io.Reader;
import java.io.PushbackReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class Tokenizer implements Iterator {
   private static final TokenizerState INPUT_PARAMETER_STATE = new InputParameterState();
   private static final TokenizerState NUMBER_STATE = new NumberState();
   private static final TokenizerState QUOTE_STATE = new QuoteState();
   private static final TokenizerState SYMBOL_STATE = new SymbolState();   
   private static final TokenizerState WHITESPACE_STATE = new WhitespaceState();
   private static final TokenizerState WORD_STATE = new WordState();

   private Map tokenizerStates = new HashMap(256);
   private TokenizerState defaultState;
   private PushbackReader reader;
   private Token peekedToken;

   public Tokenizer() {
      initDefaultStates();
   }
   
   public Tokenizer(String string) {
      this(new StringReader(string));
   }
   
   public Tokenizer(Reader reader) {
      this();
      setReader(reader);
   }

   public TokenizerState getCharacterState(char character) {
      TokenizerState s = (TokenizerState)tokenizerStates.get(new Character(character));
      if(s != null) {
         return s;
      }
      return SYMBOL_STATE;
   }
   
   public void setCharacterState(char character, TokenizerState state) {
      tokenizerStates.put(new Character(character), state);
   }
   
   public Reader getReader() {
      return reader;
   }
   
   public void setReader(Reader reader){
      this.reader = new PushbackReader(reader, 32);
   }
      
   public boolean hasNext() {
      return peekToken() != null;
   }

   public Object next() {
      return nextToken();
   }
   
   public Token nextToken() {
      Token n = peekToken();
      if(n == null) {
         throw new NoSuchElementException();
      }
      peekedToken = null;
      return n;
   }
   
   public Token peekToken() {
      if(peekedToken == null) {
         try{
            int c = reader.read();
            if(c == -1) {
               return null;
            }
            
            TokenizerState state = getCharacterState((char)c);
            peekedToken = state.nextToken(reader, (char)c, this);
         } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
         }
      }
      return peekedToken;
   }
   
   public void remove() {
      throw new UnsupportedOperationException("Tokenier does not support remove");
   }
   
   protected void initDefaultStates() {
      for(int i=0; i<(int)Character.MAX_VALUE; i++) {
         if(Character.isJavaIdentifierStart((char)i)) {
            tokenizerStates.put(new Character((char)i), WORD_STATE);
         }
         if(Character.isWhitespace((char)i)) {
            tokenizerStates.put(new Character((char)i), WHITESPACE_STATE);
         }
         if(Character.isDigit((char)i)) {
            tokenizerStates.put(new Character((char)i), NUMBER_STATE);
         }
      }
      tokenizerStates.put(new Character('\''), QUOTE_STATE);
      tokenizerStates.put(new Character('?'), INPUT_PARAMETER_STATE);
      tokenizerStates.put(new Character('.'), NUMBER_STATE);
   }
}
