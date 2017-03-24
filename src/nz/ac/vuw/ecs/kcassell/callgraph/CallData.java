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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.EclipseSearchUtils;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

/**
 * This class maintains information about the members accessed by methods within
 * a class, for example, the attributes that are directly accessed by a method.
 * @author Keith Cassell
 */
public class CallData implements ParameterConstants {
	/** The set of known attributes (fields). */
	protected HashSet<IField> attributes = new HashSet<IField>();

	/** The set of known methods. */
	protected HashSet<IMethod> methods = new HashSet<IMethod>();

	/**
	 * The methods directly called by a method. The key is the calling method.
	 * The value is the set of methods that it calls.
	 */
	protected HashMap<IMethod, HashSet<IMethod>> methodsCalledMap =
		new HashMap<IMethod, HashSet<IMethod>>();

	/**
	 * The attributes directly accessed by a method. The key is the calling
	 * method. The value is the set of attributes that it accesses.
	 */
	protected HashMap<IMethod, HashSet<IField>> attributesAccessedMap =
		new HashMap<IMethod, HashSet<IField>>();

	/**
	 * The direct callers of a method. The key is a method that gets called by
	 * some method. The value is the set of methods that directly call it.
	 */
	protected HashMap<IMethod, HashSet<IMethod>> methodCalledByMap =
		new HashMap<IMethod, HashSet<IMethod>>();

	/**
	 * The direct accessors of an attribute (field). The key is an attribute
	 * that gets accessed by some method. The value is the set of methods that
	 * directly access it.
	 */
	protected HashMap<IField, HashSet<IMethod>> attributeAccessedByMap =
		new HashMap<IField, HashSet<IMethod>>();

	/** Should inherited members be included in the call data? */
	protected boolean includeInherited = false;

	/** Should inner class members be included in the call data? */
	protected boolean includeInners = false;

	/** Should static members be included in the call data? */
	protected boolean includeStatics = true;

	protected static UtilLogger logger = new UtilLogger("CallData");
	
	/**
	 * The constructor sets some fields based on property values.
	 */
	public CallData() {
		ApplicationParameters params = ApplicationParameters.getSingleton();
		includeInherited =
			params.getBooleanParameter(INCLUDE_INHERITED_KEY, false);
		includeInners =
			params.getBooleanParameter(INCLUDE_INNERS_KEY, false);
		includeStatics =
			params.getBooleanParameter(INCLUDE_STATICS_KEY, true);
	}

	/**
	 * Gathers call information about the given class and stores the information
	 * about the members, attributes (fields), and call relationships between
	 * them. This information is obtainable via the various get* methods.
	 * 
	 * @param type
	 *            the class to analyze
	 * @throws JavaModelException generally when the project can not be found
     */
	public void collectCallData(IType type) throws JavaModelException {
		try {
			IJavaSearchScope scope = null;
			if (includeInherited) {
				// scope = EclipseSearchUtils.createProjectSearchScope(type);
				IPackageFragment packageFragment = type.getPackageFragment();
				scope = SearchEngine
						.createJavaSearchScope(new IJavaElement[] { packageFragment });
			} else {
				scope = SearchEngine
						.createJavaSearchScope(new IJavaElement[] { type });
			}
			
			//TODO figure out where to store local vs. foreign
//			List<IMember> allMembers = EclipseSearchUtils.getMembers(type, true);
			
			// methods
			//Set<IMethod> calledMethods =
				calculateCalledMethods(type, scope);
//			Set<IMethod> allCalledMethods = EclipseSearchUtils.calculateCalledMethods(type);
//			Set<IMethod> foreignCalledMethods = new HashSet<IMethod>(allCalledMethods);
//			foreignCalledMethods.removeAll(locallyCalledMethods);
			
			// fields
			collectFieldCallData(type, scope);
		} catch (CoreException e) {
			logger.severe("CallData.calculateCalledMethods: " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Calculates method calls internal to a class and stores them in
	 * the methodsCalledMap (also the methodCalledByMap).
	 * @param type the class of interest
	 * @return all methods within this class that are called by methods
	 *  within this class.
	 * @throws CoreException 
	 */
	private Set<IMethod> calculateCalledMethods(IType type, IJavaSearchScope scope)
			throws CoreException {
		//TODO make consistent - this is now including inherited methods
		// populates the calls and called-by maps
		collectMethodCallData(type, scope);
		
		Collection<HashSet<IMethod>> values = methodsCalledMap.values();
		HashSet<IMethod> calledMethods = new HashSet<IMethod>();
		for (HashSet<IMethod> value : values) {
			calledMethods.addAll(value);
		}
		return calledMethods;
	}

	/**
	 * Gathers call information about methods calling methods within the given
	 * class. This information is stored in the methodCalledByMap and
	 * methodsCalledMap.
	 * 
	 * @param type the class to analyze
	 * @param searchEngine
	 * @param participants
	 * @param scope the elements being examined, e.g. this class or this package
	 * @param methodCollector gathers the search results
	 * @return the collection of methods that access the indicated member
	 * @throws CoreException 
	 */
	private void collectMethodCallData(IType type,
			IJavaSearchScope scope)
			throws CoreException {
		List<IMethod> typeMethods =
			EclipseSearchUtils.getMethods(type, includeInherited);
		methods.addAll(typeMethods);

		// Optionally include information about methods belonging to
		// inner classes.  NOTE: this is not recursive currently.
		if (includeInners) {
//			Set<IType> innerTypes = EclipseSearchUtils.getEmbeddedTypes(type);
			IType[] innerTypes = type.getTypes();
			for (IType innerType : innerTypes) {
				int flags = innerType.getFlags();
				if (!Flags.isStatic(flags)) {
					List<IMethod> innerTypeMethods =
						EclipseSearchUtils.getMethods(innerType, false);
					typeMethods.addAll(innerTypeMethods);
				}
			}
		}
		methods.addAll(typeMethods);

		// Update the stored information about the methods of the class
		for (IMethod method : methods) {
			// Update the methodCalledByMap for typeMethods[i]
			try {
				Set<IMethod> callers = EclipseSearchUtils.calculateCallingMethods(method, scope);
				methodCalledByMap.put(method, new HashSet<IMethod>(callers));

				// Update the methodsCalledMap for method
				for (IMethod caller : callers) {
					HashSet<IMethod> calleesL = methodsCalledMap.get(caller);
					if (calleesL == null) {
						calleesL = new HashSet<IMethod>();
					}
					calleesL.add(method);
					methodsCalledMap.put(caller, calleesL);
				}
			} catch (CoreException e) {
				logger.warning("CallData.collectMethodCallData: " + e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gathers call information about methods accessing fields within the given
	 * class. This information is stored in the attributeAccessedByMap and
	 * attributesAccessedMap.
	 * 
	 * @param type the class to analyze
	 * @param scope the elements being examined, e.g. this class or this package
	 * @param methodCollector gathers the search results
	 * @throws CoreException 
	 */
	private void collectFieldCallData(IType type, IJavaSearchScope scope)
	throws CoreException {
		List<IField> typeFields =
			EclipseSearchUtils.getFields(type, includeInherited);
		
		// Optionally include information about fields belonging to
		// inner classes.  NOTE: this is not recursive currently.
		if (includeInners) {
//			Set<IType> innerTypes = EclipseSearchUtils.getEmbeddedTypes(type);
			IType[] innerTypes = type.getTypes();
			for (IType innerType : innerTypes) {
				int flags = innerType.getFlags();
				if (!Flags.isStatic(flags)) {
					List<IField> innerTypeFields =
						EclipseSearchUtils.getFields(innerType, false);
					typeFields.addAll(innerTypeFields);
				}
			}
		}
		attributes.addAll(typeFields);

		// Update the stored information about the fields of the class
		for (IField attribute : attributes) {
			// Update the fieldCalledByMap for attribute
			try {
				Set<IMethod> callers = EclipseSearchUtils.calculateCallingMethods(
						attribute, scope);
				attributeAccessedByMap.put(attribute, new HashSet<IMethod>(
						callers));

				// Update the fieldsCalledMap for attribute
				for (IMethod caller : callers) {
					HashSet<IField> calleesL = attributesAccessedMap
							.get(caller);
					if (calleesL == null) {
						calleesL = new HashSet<IField>();
					}
					calleesL.add(attribute);
					attributesAccessedMap.put(caller, calleesL);
				}
			} catch (CoreException e) {
				logger.warning("CallData.collectFieldCallData: " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Gets a human readable version of the method signature.
	 */
	protected String getSignature(IMethod method) throws JavaModelException {
		String methodName = method.getElementName();
		String sig = Signature.toString(method.getSignature(), methodName,
				null, // parameterNames,
				true, // fullyQualifyTypeNames,
				false); // includeReturnType
		return sig;
	}

	/**
	 * @return the attributes
	 */
	public Set<IField> getAttributes() {
		return attributes;
	}

	/**
	 * @return the methods
	 */
	public Set<IMethod> getMethods() {
		return methods;
	}

	/**
	 * @return the methodsCalledMap
	 */
	public Map<IMethod, HashSet<IMethod>> getMethodsCalledMap() {
		return methodsCalledMap;
	}

	/**
	 * @return the attributesAccessedMap
	 */
	public Map<IMethod, HashSet<IField>> getAttributesAccessedMap() {
		return attributesAccessedMap;
	}

	/**
	 * @return the methodCalledByMap
	 */
	public Map<IMethod, HashSet<IMethod>> getMethodCalledByMap() {
		return methodCalledByMap;
	}

	/**
	 * @return the attributeAccessedByMap
	 */
	public Map<IField, HashSet<IMethod>> getAttributeAccessedByMap() {
		return attributeAccessedByMap;
	}

	/**
	 * This provides a mechanism for generating a human-readable version of the
	 * methodsCalledMap
	 * 
	 * @return a string representation of the methodsCalledMap
	 */
	private String methodsCalledMapToString() {
		StringBuffer buf = new StringBuffer("methodsCalledMap:\n");
		Set<IMethod> keySet = methodsCalledMap.keySet();

		for (IMethod caller : keySet) {
			String sCaller;
			try {
				sCaller = getSignature(caller);
			} catch (JavaModelException e) {
				sCaller = caller.toString();
			}
			HashSet<IMethod> callees = methodsCalledMap.get(caller);
			buf.append(sCaller);
			buf.append(": ");
			buf.append(callees.toString());
			buf.append("\n");
		}
		return buf.toString();
	}

	/**
	 * This provides a mechanism for generating a human-readable version of the
	 * attributesAccessedMap
	 * 
	 * @return a string representation of the attributesAccessedMap
	 */
	private String attributesAccessedMapToString() {
		StringBuffer buf = new StringBuffer("attributesAccessedMap:\n");
		Set<IMethod> keySet = attributesAccessedMap.keySet();

		for (IMethod caller : keySet) {
			String sCaller;
			try {
				sCaller = getSignature(caller);
			} catch (JavaModelException e) {
				sCaller = caller.toString();
			}
			HashSet<IField> callees = attributesAccessedMap.get(caller);
			buf.append(sCaller);
			buf.append(": ");
			buf.append(callees.toString());
			buf.append("\n");
		}
		return buf.toString();
	}

	/**
	 * @return a human-readable form of the call data
	 */
	@Override
	public String toString() {
		String result = "CallData@" + hashCode() + "\n"
				+ attributesAccessedMapToString() + methodsCalledMapToString();
		return result;
	}

}
