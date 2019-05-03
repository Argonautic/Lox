package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
	final Environment enclosing;
	private final Map<String, Object> values = new HashMap<>();

	Environment() {
		enclosing = null;
	}

	Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}

	void define(String name, Object value) {
		values.put(name, value);
	}

	Object getAt(int distance, String name) {
		return ancestor(distance).values.get(name);
	}

	void assignAt(int distance, Token name, Object value) {
		ancestor(distance).values.put(name.lexeme, value);
	}

	// Get the environment "distance" steps away
	Environment ancestor(int distance) {
		Environment environment = this;
		for (int i = 0; i < distance; i++) {  // Have to hop every time in a loop because environments are stored as a linked list
			environment = environment.enclosing;
		}

		return environment;
	}

	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}

		if (enclosing != null) {
			return enclosing.get(name);
		}
		 
		// References to variables that don't exist are checked at runtime instead of
		// compile time because there are cases of valid code where references exist to 
		// variables that have not yet been declared, such as recursive functions, or
		// references to a global variable within a function. It's okay to refer to a
		// variable before it's defined as long as it's not evaluated
		
		// (I'd rather do it the java way where all names and variables are declared before
		// any function bodies are looked at)
		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}

	// compared to define, assign can look through enclosing environments to reassign a
	// previously declared variable
	void assign(Token name, Object value) {
		if (values.containsKey(name.lexeme)) {
			values.put(name.lexeme, value);
			return;
		}

		if (enclosing != null) {
			enclosing.assign(name, value);
			return;
		}
		
		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}
}
