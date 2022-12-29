package jlox;

import java.io.IOException;

public class Token { 

    final TokenType type; // left paren... bang + bangequal, identifier / string / number, and / or / protected keyword
    final String lexeme; // actual text
    final Object literal; // what is this 
    final int line; // line location in file

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }

}