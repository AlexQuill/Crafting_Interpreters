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
    private int start;
    private int current;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
      keywords = new HashMap<>();
      keywords.put("and",    AND);
      keywords.put("class",  CLASS);
      keywords.put("else",   ELSE);
      keywords.put("false",  FALSE);
      keywords.put("for",    FOR);
      keywords.put("fun",    FUN);
      keywords.put("if",     IF);
      keywords.put("nil",    NIL);
      keywords.put("or",     OR);
      keywords.put("print",  PRINT);
      keywords.put("return", RETURN);
      keywords.put("super",  SUPER);
      keywords.put("this",   THIS);
      keywords.put("true",   TRUE);
      keywords.put("var",    VAR);
      keywords.put("while",  WHILE);
    }
  

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens(String source) {

        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance(); // keep consuming characters
        switch (c) { // all single character tokens
          case '(': addToken(LEFT_PAREN); break;
          case ')': addToken(RIGHT_PAREN); break;
          case '{': addToken(LEFT_BRACE); break;
          case '}': addToken(RIGHT_BRACE); break;
          case ',': addToken(COMMA); break;
          case '.': addToken(DOT); break;
          case '-': addToken(MINUS); break;
          case '+': addToken(PLUS); break;
          case ';': addToken(SEMICOLON); break;
          case '*': addToken(STAR); break; 
          case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
          case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
          case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
          case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
          case '"': string(); break;
          case '/':
            if (match('/')) {
                while(peek() != '\n' && !isAtEnd()) advance();
            } else if (match('*')){
                block_comment();
            }
            else {
                addToken(SLASH);
            } break;
          case '\r':
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

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    } 

    private boolean match(char c) {
        if (isAtEnd()) return false;
        if (c != source.charAt(current)) return false;

        current++; // increment counter if we have a 2-char token
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean isAlpha(char c) {
        return ((c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                 c == '_');
    }

    private boolean isAlphaNumeric(char c) {
        return (isAlpha(c) || isDigit(c));
    }

    private void identifier() {
        while(isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        if (type == null) {
            addToken(IDENTIFIER);
        } else {
            addToken(type);
        }
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
          // Consume the "."
          advance();
    
          while (isDigit(peek())) advance();
        }
    
        addToken(NUMBER,
            Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while(peek() != '"' && !(isAtEnd())) { // while not at end of file
            if (peek() == '\n') line++; // continue to advance until we hit a wall
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string");
            return;
        }
        advance();
        String val  = source.substring(start + 1, current - 1);
        addToken(STRING, val);
    }



    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd(){
        return current >= source.length();
    } 
    
}
