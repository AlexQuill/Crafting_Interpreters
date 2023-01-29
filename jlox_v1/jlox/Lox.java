package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    public static boolean hadError = false;
    
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Correct usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]); // run a file
        } else {
            runPrompt(); // open shell
        }
    }
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path)); // read full file into byte array
        run(new String(bytes, Charset.defaultCharset())); // convert bytes to string and run that file

        // this is not async. We scan the whole thing, then check for hadError, and exit 
        if (hadError) System.exit(65);
    }
    
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in); // reads input
        BufferedReader buff = new BufferedReader(input); // converts bytes to text

        // REPL
        for (;;) {
            System.out.println("> ");
            String line = buff.readLine(); // read
            if (line == null) { break; } // Command-D
            run(line); // evaluate
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source); // class converts files to lexemes
        List<Token> tokens = scanner.scanTokens(source); // return list of tokens


        /*
         * Token class
         * Type: "PAREN"
         * Lexeme: text
         * literal: If it's a variable, string, or num, store what that is here
         * line: line in file that the token is on
         */

        for (Token token : tokens) {
            System.out.printf("Type: %s Lexeme: %s\n", token.type.toString(), token.lexeme.toString());
        }
    }

    // NOT private or public - private: only available within class. 
    // public: available to all classes
    // neither: package-private - available to all other classes within this package. 
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line: " + line + "] Error: " + where + ": " + message);
        hadError = true;
    }

}
