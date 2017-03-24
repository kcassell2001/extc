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

package nz.ac.vuw.ecs.kcassell.callgraph.gui.transformers;

import java.awt.Color;
import java.awt.Paint;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.NodeType;
import nz.ac.vuw.ecs.kcassell.callgraph.gui.ClusterUIConstants;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;

import org.apache.commons.collections15.Transformer;
import org.eclipse.jdt.core.Flags;

public class NodeTypeColorer implements Transformer<CallGraphNode, Paint> {

	protected Color defaultColor = Color.GRAY;
	protected Color fieldColor = Color.BLUE;
	protected Color methodColor = Color.WHITE;
	protected Color publicColor = Color.GREEN;
	protected Color protectedColor = Color.YELLOW;
	protected Color privateColor = Color.RED;
	protected Color packageDefaultColor = Color.blue;
	
	public NodeTypeColorer() {
		ApplicationParameters params = ApplicationParameters.getSingleton();
		int iParam =
			params.getIntParameter(ParameterConstants.FIELD_COLOR_KEY,
					ClusterUIConstants.LIGHT_BLUE_RGB);
		fieldColor = new Color(iParam);
		iParam = params.getIntParameter(ParameterConstants.METHOD_COLOR_KEY,
					ClusterUIConstants.BLACK_RGB);
		methodColor = new Color(iParam);
		iParam = params.getIntParameter(ParameterConstants.PRIVATE_COLOR_KEY,
					ClusterUIConstants.RED_RGB);
		privateColor = new Color(iParam);
		iParam = params.getIntParameter(ParameterConstants.PROTECTED_COLOR_KEY,
				ClusterUIConstants.YELLOW_RGB);
		protectedColor = new Color(iParam);
		iParam = params.getIntParameter(ParameterConstants.PUBLIC_COLOR_KEY,
				ClusterUIConstants.GREEN_RGB);
		publicColor = new Color(iParam);
		iParam = params.getIntParameter(ParameterConstants.PACKAGE_DEFAULT_COLOR_KEY,
				ClusterUIConstants.BLUE_RGB);
		packageDefaultColor = new Color(iParam);
	}

	public Paint transform(CallGraphNode node) {
		Paint color = defaultColor;
		int flags = node.getMemberFlags();
		
		// No detail about the member is known (public, synchronized, etc.)
		if (Flags.AccDefault == flags) {
			if (NodeType.FIELD.equals(node.getNodeType())) {
				color = fieldColor;
			} else {
				color = methodColor;
			}
		} else {
			if (node.isInner()) {
				color = Color.pink;
			} if (Flags.isPackageDefault(flags)) {
				color = packageDefaultColor;
			} else if (Flags.isPrivate(flags)) {
				color = privateColor;
			} else if (Flags.isProtected(flags)) {
				color = protectedColor;
			} else if (Flags.isPublic(flags)) {
				color = publicColor;
			} else {
				color = defaultColor;
			}
		}
		return color;
	}
}
