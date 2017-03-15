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

package nz.ac.vuw.ecs.kcassell.similarity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.utils.EclipseSearchUtils;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;

public class ClientDistanceCalculator 
extends VectorSpaceModelCalculator {
	
	private static final long serialVersionUID = -8788415900559905880L;

	/** The list of method handles whose clients are to be considered
	 *  in the clustering. */
	protected List<String> memberHandles = new ArrayList<String>();

	/**
	 * Construct the calculator
	 * @param handle the handle of the class whose clients we're interested in
	 * @throws IOException
	 */
	public ClientDistanceCalculator(String handle) throws IOException {
		super(handle);
	}
	
	/**
	 * Create a file where each line is a "document".  The first token of
	 * the line is the member handle.  Subsequent tokens are the
	 * calling classes.
	 * @param fileName the name of the document file
	 * @return the documents
	 */
	public String buildDocumentsForPublicMethods(
			JavaCallGraph callGraph, String fileName) {
		String documents = "";
		StringBuffer buf = new StringBuffer();
		List<CallGraphNode> nodes = callGraph.getNodes();
		for (CallGraphNode node : nodes) {
			String memberHandle = node.getLabel();
			try {
				IJavaElement member = JavaCore.create(memberHandle);
				// TODO - node is not a member, e.g. a cluster
				
				// Create a line with the member handle followed by the client classes
				if (passesFilter(member)) {
					makeLine(buf, memberHandle, member);
					memberHandles.add(memberHandle);
				}
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		try {
			BufferedWriter writer
			   = new BufferedWriter(new FileWriter(fileName));
			documents = buf.toString();
			writer.write(documents);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return documents;
	}

	/**
	 * Create a line with the member handle followed by the client classes
	 * @param buf
	 * @param memberHandle
	 * @param member
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	private static void makeLine(StringBuffer buf, String memberHandle,
			IJavaElement member) throws JavaModelException, CoreException {
		buf.append(memberHandle).append(' ');
		IJavaSearchScope scope =
			EclipseSearchUtils.createProjectSearchScope(member);
		Set<String> callers =
			EclipseSearchUtils.calculateCallingClasses(member, scope);
		for (String caller : callers) {
			buf.append(caller).append(' ');
		}
		buf.deleteCharAt(buf.length() - 1); // remove last space
		buf.append("\n");
	}
	
	/**
	 * Methods that are public and non-static pass.
	 * @param element
	 * @return true if the element is a public, non-static method;
	 *  false otherwise
	 */
	private static boolean passesFilter(IJavaElement element) {
		boolean isPublic = false;
		
		if (element instanceof IMethod) {
			IMethod member = (IMethod)element;
			try {
				int flags = member.getFlags();
				isPublic = Flags.isPublic(flags) && !Flags.isStatic(flags);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return isPublic;
	}

	/**
	 * Based on an Eclipse handle, retrieve the file name for the
	 * text file containing the corpus of documents.
	 * @param handle an Eclipse handle
	 * @return the file name of the corpus
	 */
	public String getDataFileNameFromHandle(String handle) {
		String memberClientsFile = getClientDataFileNameFromHandle(handle);
	    return memberClientsFile;
	}

	/**
	 * Based on an Eclipse handle, retrieve the file name for the
	 * text file containing the corpus of documents.
	 * @param handle an Eclipse handle
	 * @return the file name of the corpus
	 */
	public static String getClientDataFileNameFromHandle(String handle) {
		String className = EclipseUtils.getNameFromHandle(handle);
		String projectName = EclipseUtils.getProjectNameFromHandle(handle);
	    String memberClientsFile = RefactoringConstants.DATA_DIR +
			"MemberDocuments/" + projectName + "/" +
			className + "Clients.txt";
	    return memberClientsFile;
	}

	public DistanceCalculatorEnum getType() {
		return DistanceCalculatorEnum.ClientDistance;
	}

	public List<String> getMemberHandles() {
		return memberHandles;
	}

}
