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

package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.ItemSupportList;
import nz.ac.vuw.ecs.kcassell.utils.EclipseSearchUtils;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

public class FrequentMethodsMiner {

	/**
	 * Associate all of the calling methods with their classes using a hash map
	 * @param callingMethods all of the methods that call the client class
	 * @param clientCalls a map whose keys are the client class identifiers
	 *  and whose values are the called server methods
	 */
	public Collection<ItemSupportList> getFrequentFrequentlyUsedMethods(
			String handle) throws CoreException {
		IType server = EclipseUtils.getTypeFromHandle(handle);
		IJavaProject project = server.getJavaProject();
		IJavaSearchScope scope =
			SearchEngine.createJavaSearchScope(new IJavaElement[] { project });
		HashMap<String, Set<String>> clientCallers =
			new HashMap<String, Set<String>>();
		
		IMethod[] serverMethods = server.getMethods();
		for (IMethod serverMethod : serverMethods) {
			int flags = serverMethod.getFlags();
			if (Flags.isPublic(flags) && !serverMethod.isConstructor()
					&& !EclipseUtils.isRedefinedObjectMethod(serverMethod.getHandleIdentifier())) {
				Set<IMethod> callingMethods = EclipseSearchUtils
						.calculateCallingMethods(serverMethod, scope);
				associateServerMethodsWithClient(serverMethod,
						callingMethods, clientCallers);
			}
		}
		
		// a collection of item support lists for each client class
		// where the items in the support lists are the calling methods from
		// the class
		ArrayList<ItemSupportList> itemSupportLists =
			createItemSupportLists(clientCallers);
		FPGrowthMiner fpMiner = new FPGrowthMiner();
		Collection<ItemSupportList> frequentMethods =
			fpMiner.mine(itemSupportLists, 4);
		
		return frequentMethods;
	}

	/**
	 * Creates item support lists for each server class
	 * @param clientCallers a map whose keys are the class identifier and whose values
	 * are the methods in that class that call client methods
	 * @return a collection of item support lists for each client class
	 * where the items in the support lists are the calling methods from
	 * the class.
	 */
	protected ArrayList<ItemSupportList> createItemSupportLists(
			HashMap<String, Set<String>> clientCallers) {
		ArrayList<ItemSupportList> itemSupportLists =
			new ArrayList<ItemSupportList>();
		Set<Entry<String,Set<String>>> entrySet = clientCallers.entrySet();
		for (Entry<String,Set<String>> entry : entrySet) {
			String className = entry.getKey();
			Set<String> classMethods = entry.getValue();
			ItemSupportList itemSupportList =
				new ItemSupportList(className, classMethods, null);
			itemSupportLists.add(itemSupportList);
		}
		return itemSupportLists;
	}

	/**
	 * Associate all of the calling methods with their classes using a hash map
	 * @param callingMethods all of the methods that call the client class
	 * @param clientCalls a map whose keys are the client class identifiers
	 *  and whose values are the called server methods
	 */
	protected void associateServerMethodsWithClient(
			IMethod serverMethod,
			Set<IMethod> callingMethods,
			HashMap<String, Set<String>> clientCalls) {
		String serverMethodName = serverMethod.getElementName();
		// TODO use handle instead

		for (IMethod callingMethod : callingMethods) {
			IType clientType = callingMethod.getDeclaringType();
			String clientTypeName = clientType.getElementName();
			// TODO String typeHandle = declaringType.getHandleIdentifier();
			Set<String> serverMethodsCalled = clientCalls.get(clientTypeName);
			if (serverMethodsCalled == null) {
				serverMethodsCalled = new HashSet<String>();
				clientCalls.put(clientTypeName, serverMethodsCalled);
			}
			serverMethodsCalled.add(serverMethodName);
		}
	}


}
