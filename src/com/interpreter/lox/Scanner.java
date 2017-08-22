package com.interpreter.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.interpreter.lox.TokenType.*; //TODO: Replace the static import with `TokenType.` syntax

class Scanner {
    private final String source; // Lox source code
    private final List<Token> tokens = new ArrayList<>(); // List to store the tokens in source code

    private int start = 0; // first char in the lexeme being scanned
    private int current = 0; // char currently scanning
    private int line = 1; // source line of current

    // Constructor
    Scanner(String source) {
        this.source = source;
    }

    /**
     * Generates the List of tokens present in the Lox source code
     * @return A <Token> List
     * */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line)); // Add an EOF token to signify the end of the source code
        return tokens;
    }

    /**
     * Scan a single token
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
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
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                // Check if it's a comment
                if (match('/')) {
                    // A comment goes until the end of the line
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break; // Handle strings
            default:
                if (isDigit(c)) {
                    number();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    /**
     * Handle scanning number literals
     * */
    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part
        if (peek() == '.') {
            if (isDigit(peekNext())) {
                advance(); // Consume the '.'
                while (isDigit(peek())) advance(); // Consume the rest of the fractional part
            } else {
                Lox.error(line, "Invalid number format.");
            }
        }

        if (!Lox.getErrorStatus()) {
            addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
        }
    }

    /**
     * Handle scanning string literals
     * */
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++; // Handles multi-line strings
            advance();
        }

        // Handles unterminated strings
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance(); // Consumes the closing '"'

        // Trim the surrounding quotes to get the value of the string
        // TODO: Add support for string escape sequences here
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * Checks whether the next unconsumed character is what we expect.
     * If it is then we consume it.
     * @param expected Expected character
     * @return boolean representing whether `expected` is the next unconsumed character
     * */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    /**
     * Returns the next unconsumed character WITHOUT consuming it.
     * @return Next unconsumed character
     * */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * Returns the char after the next unconsumed character without consuming it.
     * @return Character after the character returned by peek()
     * */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    /**
     * Checks whether the given character is a digit or not.
     * @param c character to test
     * @return whether the character is digit
     * */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Checks whether the scanner is at the end of the source code.
     * */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Consumes a character and advances the scanner to the next character.
     * */
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    // This overload is used to handle tokens with 1 character
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // This overload is used to handle tokens with literal values
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
