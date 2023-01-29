package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlox.TokenType.*;

class Scanner {

    // these isntance variables are delcared but nothing is assigned to them yet. 
    private final String source; // text input from file or user
    private final List<Token> tokens = new ArrayList<>(); // where we put the tokens once split
    private int start; // pointers for character-by-character hops
    private int current;
    private int line = 1;

    private static final Map<String, TokenType> protectedWords;

    // dictionary of protected words
    static {
      protectedWords = new HashMap<>();
      protectedWords.put("and",    AND);
      protectedWords.put("class",  CLASS);
      protectedWords.put("else",   ELSE);
      protectedWords.put("false",  FALSE);
      protectedWords.put("for",    FOR);
      protectedWords.put("fun",    FUN);
      protectedWords.put("if",     IF);
      protectedWords.put("nil",    NIL);
      protectedWords.put("or",     OR);
      protectedWords.put("print",  PRINT);
      protectedWords.put("return", RETURN);
      protectedWords.put("super",  SUPER);
      protectedWords.put("this",   THIS);
      protectedWords.put("true",   TRUE);
      protectedWords.put("var",    VAR);
      protectedWords.put("while",  WHILE);
    }
  

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens(String source) {

        while (!isAtEnd()) { // while we are not at the end of the file
            start = current; // move start up
            scanToken(); // scan for one token
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance(); // get next char
        switch (c) { // what is that char?
          case '(': addToken(LEFT_PAREN); break; // if paren, that's a token...
          case ')': addToken(RIGHT_PAREN); break;
          case '{': addToken(LEFT_BRACE); break;
          case '}': addToken(RIGHT_BRACE); break;
          case ',': addToken(COMMA); break;
          case '.': addToken(DOT); break;
          case '-': addToken(MINUS); break;
          case '+': addToken(PLUS); break;
          case ';': addToken(SEMICOLON); break;
          case '*': addToken(STAR); break; 
          case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break; // if = and next char is =...
          case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
          case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
          case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
          case '"': string(); break; // we have a string
          case '/':
            if (match('/')) {
                while(peek() != '\n' && !isAtEnd()) advance();
            } else if (match('*')){
                block_comment();
            }
            else {
                addToken(SLASH);
            } break;
          case '\r': // don't care about returns, tabs, spaces
          case '\t':
          case ' ': break;
          case '\n': line++; break; // when we get to a newline, skip to next line
          default: 
            if (isDigit(c)) {
                number();
            } else if (isAlpha(c)) {
                identifier();
            } else {
                Lox.error(line, "unexpected character"); break;
            }
        }

    }

    private void block_comment() {
        while (!(isAtEnd()) && !(peek() == '*' && peekNext() == '/')) advance();
        System.out.printf("blocked comment: %s\n", source.substring(start, current));
        current++; // step past *
        current++; // step past /
    }

    // return next char
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // return two chars ahead
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    } 

    // check if next char matches parameter
    private boolean match(char c) {
        if (isAtEnd()) return false; // if we're at end of file it's obviously not a two-char token
        if (c != source.charAt(current)) return false; // if the next char doesn't match what we have as input, nope

        current++; // increment counter only if we have a 2-char token
        return true;
    }

    // clever way to return the current char and move the current pointer up
    private char advance() {
        return source.charAt(current++);
    }

    // check if it's an alphabetical character, for identifier case
    private boolean isAlpha(char c) {
        return ((c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                 c == '_');
    }
    
    // number or alphabetical
    private boolean isAlphaNumeric(char c) {
        return (isAlpha(c) || isDigit(c));
    }

    // handle identifier
    private void identifier() {
        // variable names can have letters so consumer the whole identifier
        // no underscores or spaces though.
        while(isAlphaNumeric(peek())) advance(); // move counter up until we're at end of identifier

        // capture the identifier 
        String text = source.substring(start, current);
        // check if it's a protected word
        TokenType type = protectedWords.get(text);

        // if not found in protected words dict, it's a variable name
        if (type == null) {
            addToken(IDENTIFIER);
        } else { // if a protected word, that's our new token type
            addToken(type);
        }
    }

    // check if character is a digit
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // handle a number
    private void number() {
        // consumer the full number
        while (isDigit(peek())) advance();

        // Look for a decimal part.
        if (peek() == '.' && isDigit(peekNext())) {
          // Consume the "."
          advance();
    
          // step through the . and keep it moving again
          while (isDigit(peek())) advance();
        }
        
        // add token of the full num, including the decimal
        addToken(NUMBER,
            Double.parseDouble(source.substring(start, current)));
    }

    // handle string
    private void string() {
        // while next char isn't the end of the string and we're not at the end of the file
        while(peek() != '"' && !(isAtEnd())) {
            if (peek() == '\n') line++; // if next char is newline, we're at a new line so increment line
            advance(); // advance increments counter - it also returns the char, but we don't care since we know it's not the end of the string yet 
        }
        // either end of string or end of file
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string");
            return;
        }
        // if end of string, then increment current and add a new token of the full string
        advance();
        String val  = source.substring(start + 1, current - 1);
        addToken(STRING, val);
    }

    // addToken variant 1 - all single-characters, two-characters, and protected identifiers
    // immediately pushes to type 2
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // addToken variant 2 - all variable names, numbers, and strings
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        // now we have a list of all the tokens we need
        tokens.add(new Token(type, text, literal, line));
    }

    // check if at end of file
    private boolean isAtEnd(){
        return current >= source.length();
    } 
    
}
