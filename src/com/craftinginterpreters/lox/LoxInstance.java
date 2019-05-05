package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class LoxInstance {
    private LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    /*
        Lox uses bound methods, which means that when methods are called, they refer to the instance they
        were *drawn* from. Normally, since methods are drawn (using '.') at the same time they're called
        (using '()'), the distinction is moot. However, it does matter for situations like:

        ```
            class Foo {
                myMethod() {
                    print this.bar;
                }
            }

            var instOne = Foo();
            instOne.bar = 10;

            var instTwo = Foo();
            instTwo.bar = 15

            instTwo.myMethod = instOne.myMethod
            instTwo.myMethod()  // This *should* print out 10, because this myMethod was drawn from instOne
        ```

        Within methods, the keyword "this" will always refer to the instance the method was drawn from as well
     */

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    // LoxInstance getting and setting can be done dynamically because the resolver ensures
    // you'll always get the intended instance every time you refer to an instance name
    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        LoxFunction method = klass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        // Piss off, javascript
        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
