package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

// RUN: Java -cp .. tool.GenerateAst ../jlox 

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
          System.err.println("Usage: generate_ast <output directory>");
          System.exit(64);
        }
        String outputDir = args[0];

        // Let's define the abstract expression class and its subclasses
        defineAst(outputDir, "Expr", Arrays.asList(
            "Binary : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal : Object value",
            "Unary : Token operator, Expr right"
            // we just add more classes here when we want to define them. 
        ));
    }

    // this writes a very nice, simple class definition
    /* 
    it takes the output directory (where we want the classes to be stored)
    Then writes out a new file to "path" that is named "Directory/[className].java"
    Then writes a simple class definition in that file, which is:
----------

package jlox;
import java.util.List
abstract class [Expr] {

}
    */
    // write the abstract class c
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        System.out.println(path);
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.println("package jlox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);


        // write the subclass definition (and instance variables)
        for(String type: types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim(); 
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");
    
        writer.println("}");
        writer.close();
    }

    // 
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("  static class " + className + " extends " + baseName + " {");

        // Constructor.
        writer.println("    " + className + "(" + fieldList + ") {");

        // instance variables
        String[] variables = fieldList.split(", ");
        for (String var : variables) {
            String name = var.split(" ")[1].trim();
            writer.println("      this." + name + " = " + name + ";");
        }
        writer.println("    }");

        // Visitor method
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
            className + baseName + "(this);");
        writer.println("    }");
        writer.println();

        for (String var : variables) {
            writer.println("    final " + var + ";");
        }

        writer.println("  }");
        writer.println();
    }
    
    private static void defineVisitor( PrintWriter writer, String basename, List <String> types) {
        writer.println("  interface Visitor<R> {");

        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + basename + "(" + typeName + " " + basename.toLowerCase() + ");");
        }

        writer.println("  }");
        writer.println();
    }

}
