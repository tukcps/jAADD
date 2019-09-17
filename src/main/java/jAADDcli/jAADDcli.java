/**
 * A simple command line calculator based on jAADD.
 */
// package jAAADcli;
package jAADDcli;

import  ExprParser.ExprParser;
import  ExprParser.ExprError;
import  ExprParser.ParseError;
import  java.io.*;
import java.util.Scanner;


public class jAADDcli {

    public static void main(String[] args) throws IOException {
        System.out.println("-------------------------------");
        System.out.println("-    jAADD command line tool  -");
        System.out.println("-------------------------------");

        // BufferedReader cons = new BufferedReader(new InputStreamReader(System.in));
        // assert(cons != null);
        Scanner    scan = new Scanner( System.in );

        ExprParser p;
        try { p = new ExprParser(); } catch (ExprError e) {
            System.out.println("    Internal error. "); return;
        }
        do {
            try {
                System.out.print(" > ");
                System.out.flush();

                String input = scan.nextLine(); // cons.readLine();

                if (input.equals("symbols")) {
                    System.out.print(p.SymbolTableInfo());
                } else if (input.equals("help") || input.isEmpty()) {
                    System.out.println("    jAADD command line tool commands are: ");
                    System.out.println("      let id = expression    evaluates expression and assigns value to id.");
                    System.out.println("      func id = expression   assigns expression as function to id.");
                    System.out.println("      expression             evaluates expression and prints result.");
                    System.out.println("      var_name               any valid identifier, initially unconstrained +/- infinity.");
                    System.out.println("      exit                   terminates the tool.");
                } else if (input.equals("exit")) {
                    return;
                }
                else {
                    p.setExpr(input);
                    System.out.println("   Result: " + p.evalDD());
                }
            }
            catch(ExprError e) {
                System.out.println("    "+e.getMessage());
            }
            catch(ParseError e) {
                System.out.println("    "+e.getMessage());
            }
        } while (true);
    }
}
