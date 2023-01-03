# Crafting_Interpreters
Interpreter for Robert Nystrom's hobby language Lox, written in Java, as described at https://craftinginterpreters.com/

In directory
```
jlox_v1/jlox
```

Compile with:
```
javac *.java
```

Execute with:
```
Java -cp .. jlox.Lox
```

To Generate AST subclasses, go to directory:
```
jlox_v1/tool
```
and compile. 

Execute with:
```
Java -cp .. tool.GenerateAst ../jlox
```
