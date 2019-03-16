package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

/*
 * 1. First, initialize Scanner with new source String
 * 2. Begin scanning the source code char by char for distinct tokens
 * 3. Every time you correctly scan a distinct token, add it to the list tokens
 * 4. Stop when you have reached the end of the source string
 */

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	
	private int start = 0;
	private int current = 0;
	private int line = 1;
	
	Scanner(String source) {
		this.source = source;
	}
	
	// Scans tokens from source file one by one until all tokens are scanned. Adds an EOF token at the end
	List<Token> scanTokens() {
		while (!isAtEnd()) {
			// We are at the beginning of the next lexeme
			start = current;
			scanToken();
		}
		
		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}
	
	// Are we at the end of the source file?
	private boolean isAtEnd() {
		return current >= source.length();
	}
	
	// Scan and identify the next token
	private void scanToken() {
		char c = advance();
		switch(c) {
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
	        	
	        case '"': string(); break;
	        
	        default:
	        	if (isDigit(c)) {
	        		number();
	        	} else if (isAlpha(c)) {
	        		identifier();
	        	} else {
		        	Lox.error(line, "Unexpected character.");
		        	break;
	        	}
		}
	}
	
	// Return the current char and advance to the next one
	private char advance() {
		return source.charAt(current++);
	}
	
	// Add a token of type type
	private void addToken(TokenType type) {
		addToken(type, null);
	}
	
	// Add a token of type type with actual value literal
	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}
	
	// Check if the current char in the sourcefile matches the char expected
	private boolean match(char expected) {
		if (isAtEnd() || source.charAt(current) != expected) return false;
		
		current++;
		return true;
	}
	
	// Return the current char without consuming it stream
	private char peek() {
		if (isAtEnd()) return '\0';
		return source.charAt(current);
	}
	
	// Return the next char after the current char without consuming it from stream
	private char peekNext() {
		if (current + 1 >= source.length()) return '\0';
		return source.charAt(current + 1);
	}
	
	// Add a String token. Triggered after consuming a quotation char
	private void string() {
		// As long as the next character is not the closing quote, or we aren't at the end of the file, keep consuming for string
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') line++;
			advance();
		}
		
		// Untermined string
		if (isAtEnd()) {
			Lox.error(line, "Unterminated String");
			return;
		}
		
		// The closing "
		advance();
		
		// Trim the surrounding quotes
		String value = source.substring(start + 1, current - 1);
		
		addToken(STRING, value);
	}
	
	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	
	private void number() {
		while (isDigit(peek())) advance();
		
		// Look for a fractional part
		if (peek() == '.' && isDigit(peekNext())) {
			// Consume the '.'
			advance();
			
			while (isDigit(peek())) advance();
		}
		
		addToken(NUMBER,
				Double.parseDouble(source.substring(start, current)));
	}
	
	private void identifier() {
		while (isAlphaNumeric(peek())) advance();
		
		// See if the identifier is a reserved word.   
	    String text = source.substring(start, current);

	    TokenType type = keywords.get(text);           
	    if (type == null) type = IDENTIFIER;           
	    addToken(type);
	}
	
	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') ||
			   (c >= 'A' && c <= 'Z') ||
			   c == '_';
	}
	
	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}
	
	private static final Map<String, TokenType> keywords;
	
	static {
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
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
}
