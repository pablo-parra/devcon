package com.devonfw.devcon;

import com.devonfw.devcon.input.InputConsole;

/**
 * TODO pparrado This type ...
 *
 * @author pparrado
 * @since 0.0.1
 */
public class Devcon {

  /**
   * @param args
   */
  public static void main(String[] args) {

    System.out.println("Hello this is Devcon!");

    new InputConsole(args).parse();
  }

}