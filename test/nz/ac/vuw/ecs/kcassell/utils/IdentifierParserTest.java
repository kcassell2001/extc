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

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class IdentifierParserTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testParseCamelCaseIdentifier() {
		ArrayList<String> ids = IdentifierParser.parseCamelCaseIdentifier("test");
		assertEquals(1, ids.size());
		assertEquals("test", ids.get(0));
		ids = IdentifierParser.parseCamelCaseIdentifier("Test");
		assertEquals(1, ids.size());
		assertEquals("Test", ids.get(0));
		ids = IdentifierParser.parseCamelCaseIdentifier("R2D2");
		assertEquals(1, ids.size());
		assertEquals("R2D2", ids.get(0));
		ids = IdentifierParser.parseCamelCaseIdentifier("_Test");
		assertEquals(1, ids.size());
		assertEquals("Test", ids.get(0));
		ids = IdentifierParser.parseCamelCaseIdentifier(".Test.");
		assertEquals(1, ids.size());
		assertEquals("Test", ids.get(0));
		ids = IdentifierParser.parseCamelCaseIdentifier("Test__");
		assertEquals(1, ids.size());
		assertEquals("Test", ids.get(0));
		ids = IdentifierParser.parseCamelCaseIdentifier("TESS");
		assertEquals(1, ids.size());
		assertEquals("TESS", ids.get(0));
		ids = IdentifierParser.parseCamelCaseIdentifier("TestCase");
		assertEquals(2, ids.size());
		assertEquals("Test", ids.get(0));
		assertEquals("Case", ids.get(1));
		ids = IdentifierParser.parseCamelCaseIdentifier("TestCase");
		assertEquals(2, ids.size());
		assertEquals("Test", ids.get(0));
		assertEquals("Case", ids.get(1));
		ids = IdentifierParser.parseCamelCaseIdentifier("TESS_Case");
		assertEquals(2, ids.size());
		assertEquals("TESS", ids.get(0));
		assertEquals("Case", ids.get(1));
		ids = IdentifierParser.parseCamelCaseIdentifier("TESSCase");
		assertEquals(2, ids.size());
		assertEquals("TESS", ids.get(0));
		assertEquals("Case", ids.get(1));
		ids = IdentifierParser.parseCamelCaseIdentifier("3DMonsterTruck");
		assertEquals(3, ids.size());
		assertEquals("3D", ids.get(0));
		assertEquals("Monster", ids.get(1));
	}

}
