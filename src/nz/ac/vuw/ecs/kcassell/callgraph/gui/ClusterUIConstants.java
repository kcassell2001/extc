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

package nz.ac.vuw.ecs.kcassell.callgraph.gui;

import java.awt.Color;

public interface ClusterUIConstants {

	public static final Color[] CLUSTER_COLORS = { new Color(215, 135, 135),
			new Color(135, 135, 210), new Color(135, 205, 190),
			new Color(205, 175, 135), new Color(195, 205, 135),
			new Color(145, 215, 135), new Color(135, 180, 210),
			new Color(100, 140, 255), new Color(60, 220, 220),
			new Color(30, 250, 100) };

	public static final String CONDENSE_CYCLES = "Cycles";
	public static final String CONDENSE_OBJECTS_METHODS = "Methods from Object";
	public static final String CONDENSE_REQUIRED_METHODS = "Required Methods";
	public static final String INCLUDE_CONSTRUCTORS = " Constructors";
	public static final String INCLUDE_INHERITED = " Inherited Members";
	public static final String INCLUDE_INNERS = " Inner Class Members";
	public static final String INCLUDE_LOGGERS = " Loggers";
	public static final String INCLUDE_OBJECT_METHODS = " Object Methods";
	public static final String INCLUDE_STATIC_MEMBERS = " Static Members";
	
	/////// RGB values for various colors
	public static final int BLACK_RGB = 0;
	public static final int BLUE_RGB = 0x0000FF;
	public static final int CYAN_RGB = 0x00FFFF;
	public static final int GREEN_RGB = 0x00FF00;
	public static final int GREY_RGB = 0x888888;
	public static final int LIGHT_GREY_RGB = 0xCCCCCC;
	public static final int LIGHT_BLUE_RGB = 0x3333CC;
	public static final int RED_RGB = 0xFF0000;
	public static final int WHITE_RGB = 0xfffff;
	public static final int YELLOW_RGB = 0xFFFF00;

}
