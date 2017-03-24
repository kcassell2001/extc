/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
Copyright (c) 2010, Keith Cassell
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following 
      disclaimer in the documentation and/or other materials
      provided with the distribution.
    * Neither the name of the Victoria University of Wellington
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package nz.ac.vuw.ecs.kcassell.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class IdentifierParser {

	private static final String INPUT_FILE = "C:/Temp/java6-classes-1.txt";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(INPUT_FILE));
			String str;
			ArrayList<ArrayList<String>> parsedIds = new ArrayList<ArrayList<String>>();

			while ((str = in.readLine()) != null) {
				ArrayList<String> tokens = parseCamelCaseIdentifier(str);
				parsedIds.add(tokens);
			}

			in.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Breaks an identifier into its component parts based on case, e.g.
	 * NamingContextHolder => [Naming, Context, Holder]
	 * 
	 * @param id
	 *            the identifier to parse
	 * @return the components of the ID
	 */
	public static ArrayList<String> parseCamelCaseIdentifier(String id) {
		ArrayList<String> tokens = new ArrayList<String>();
		String token = "";
		Character prevChar = '_';
		// the count of consecutive upper case chars (for acronym determination)
		int upperCount = 0;
		// TODO handle acronyms properly. Currently, SSLSession => [SSLSession]
		// instead of SSLSession => [SSL, Session]

		for (int i = 0; i < id.length(); i++) {
			Character curChar = id.charAt(i);

			// Punctuation separates tokens
			if (isPunctuation(curChar) && token.length() > 0) {
				upperCount = 0;
				addToken(tokens, token);
				token = "";
			}
			// Upper case
			else if (Character.isUpperCase(curChar)) {
				upperCount++;
				// Within acronym
				if (Character.isUpperCase(prevChar)) {
					token += curChar;
				}
				// Transition from lower case to upper starts new token
				else if (Character.isLowerCase(prevChar)) {
					addToken(tokens, token);
					token = "" + curChar;
				}
				// Digits are considered part of the previous token
				else if (Character.isDigit(prevChar)) {
					token += curChar;
				}
				else {
					token += curChar;
				}
			}
			// lower case chars
			else if (Character.isLowerCase(curChar)) {
				// Done with acronym. We'll consider the last upper case character
				// to be part of a new token, e.g. UIConstants get broken into "UI" and
				// "Constants".
				if (upperCount > 1) {
					upperCount = 0;
					token = token.substring(0, token.length() - 1);
					addToken(tokens, token);
					token = "" + prevChar + curChar;
				} else { // continuing lower case
					upperCount = 0;
					token += curChar;
				}
			}
			// digits just get appended to current token
			// and do not change the acronym status, e.g. R2D2 is considered
			// a single acronym/token
			else if (Character.isDigit(curChar)) {
				token += curChar;
			}
			prevChar = curChar;
		} // for
		addToken(tokens, token);
//		System.out.println("Parsed " + id + " => " + tokens);
		return tokens;
	}

	protected static void addToken(ArrayList<String> tokens, String token) {
		if ((token != null) && !"".equals(token)) {
			tokens.add(token);
		}
	}

	private static boolean isPunctuation(Character c) {
		return (c.equals('_') || c.equals('.') || c.equals('$'));
	}

}
