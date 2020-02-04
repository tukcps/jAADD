/**
 * A simple command line calculator based on jAADD.
 */
// package jAAADcli;
package jAADDcli;

import exprParser.ExprParser;
import  exprParser.ExprError;
import  exprParser.ParseError;
import  java.io.*;
import java.util.Scanner;


public class jAADDcli {

    public static void main(String[] args) throws IOException {
        System.out.println("-------------------------------");
        System.out.println("-    jAADD command line tool  -");
        System.out.println("-------------------------------");

        // BufferedReader cons = new BufferedReader(new InputStreamReader(System.in));
        // assert(cons != null);
        Scanner scan = new Scanner( System.in );
        ExprParser p = new ExprParser();
        do {
            try {
                System.out.print(" > ");
                System.out.flush();
                String input = scan.nextLine(); // cons.readLine();
                if (input.equals("symbols")) {
                    System.out.print(p.symbolTableInfo());
                } else if (input.equals("help") || input.isEmpty()) {
                    System.out.println("   jAADD command line tool commands are: ");
                    System.out.println("      var  id := expression  evaluates expression and assigns value to id.");
                    System.out.println("      fun  id := expression  assigns expression as function to id.");
                    System.out.println("      expression             evaluates expression and prints result.");
                    System.out.println("      var_name               any valid identifier, initially unconstrained +/- infinity.");
                    System.out.println("      exit                   terminates the tool.");
                } else if (input.equals("exit")) {
                    return;
                }
                else {
                    System.out.println("   Result: " + p.eval(input));
                }
            }
            catch(ExprError ee) {
                System.out.println("    " + ee.getMessage());
            }
            catch(ParseError pe) {
                System.out.println("    " + pe.getMessage());
            }
        } while (true);
    }
}
