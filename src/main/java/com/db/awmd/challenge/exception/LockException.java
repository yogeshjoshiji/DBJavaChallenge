/***
 * Excerpted from "Programming Concurrency on the JVM",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/vspcon for more book information.
***/
package com.db.awmd.challenge.exception;

public class LockException extends Exception {
  /**
	 * 
	 */
	private static final long serialVersionUID = -4928284032964776178L;

public LockException(final String message) {
    super(message);
  }
}
