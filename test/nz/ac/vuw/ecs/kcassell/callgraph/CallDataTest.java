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

package nz.ac.vuw.ecs.kcassell.callgraph;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Before;
import org.junit.Test;

public class CallDataTest extends TestCase {
	
	static {
//		EclipseUtils.activateWorkbench();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject("=ClassRefactoringPlugin");
        try {
			//project.create(null);
	        project.open(null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
        //JavaCore.create(project);
	}
	
	protected IType iType = EclipseUtils.getTypeFromHandle(
			"=ClassRefactoringPlugin/test<nz.ac.vuw.ecs.kcassell.callgraph{CallDataTest.java[CallDataTest");

	@Before
	public void setUp() throws Exception {
		iType = EclipseUtils.getTypeFromHandle(
		"=ClassRefactoringPlugin/test<nz.ac.vuw.ecs.kcassell.callgraph{CallDataTest.java[CallDataTest");
//		EclipseUtils.openInEditor(iType);
	}

	@Test
	public void testCollectCallData() {
		CallData callData = new CallData();
		try {
			callData.collectCallData(iType);
			Set<IField> attributes = callData.getAttributes();
			assertEquals(1, attributes.size());
			Iterator<IField> iterator = attributes.iterator();
			IField field = iterator.next();
			assertEquals("iType", field.getElementName());
			Set<IMethod> methods = callData.getMethods();
			assertEquals(5, methods.size());
		} catch (JavaModelException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetCallingMethodsIMemberIJavaSearchScope() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCallingMethodsIMemberSearchEngineSearchParticipantArrayIJavaSearchScopeMethodCollector() {
		fail("Not yet implemented");
	}
	
	protected void doNothing(String s) {
	}

	@Test
	public void testGetSignature() {
		CallData callData = new CallData();
		try {
			callData.collectCallData(iType);
			Set<IMethod> methods = callData.getMethods();
			for (IMethod method : methods) {
				String sig = callData.getSignature(method);
				String handle = method.getHandleIdentifier();
				if (handle.contains("testCollect")) {
					assertEquals("testCollectCallData()", sig);
				} else if (handle.contains("testGetSignature")) {
					assertEquals("testGetSignature()", sig);
				} else if (handle.contains("setUp")) {
					assertEquals("setUp()", sig);
				} else if (handle.contains("doNothing")) {
					assertEquals("doNothing()", sig);
				}
			}
		} catch (JavaModelException e) {
			fail(e.getMessage());
		}
	}

}
