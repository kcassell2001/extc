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
import java.util.List;

import javax.swing.JOptionPane;

import nz.ac.vuw.ecs.kcassell.callgraph.CallData;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class EclipseUtils {

	public static IJavaModel prepareWorkspace() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IJavaModel javaModel = JavaCore.create(workspaceRoot);
		return javaModel;
	}

	public static void openInEditor(IType iType) {
		activateWorkbench();
		try {
			JavaUI.openInEditor(iType);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void activateWorkbench() {
		// possible for PlatformUI.getWorkbench to throw an IllegalStateException
		// if the workbench is not yet started e.g createAndRunWorkbench() has not yet been called
//        WorkbenchAdvisor workbenchAdvisor = new WorkbenchAdvisor();
//        Display display = PlatformUI.createDisplay();
//        int returnCode = PlatformUI.createAndRunWorkbench(display, workbenchAdvisor);
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow workbenchWindow =
			workbench.getActiveWorkbenchWindow();
		workbenchWindow.getActivePage();
	}

	public static IType getTypeFromHandle(String handle) {
    	IType type = null;
        IJavaElement element = JavaCore.create(handle);
        if (element == null) {
            System.err.println("  No element created from " + handle);
        } else {
			IOpenable openable = element.getOpenable();
			if (openable == null) {
				System.err.println(handle + " has no openable ancestors");
			} else if (element instanceof IType) {
			    type = (IType) element;
			}
		}
		return type;
	}
	
	/**
	 * @param handle The Eclipse handle for the Java element
	 * @return the name corresponding to the handle
	 */
	public static String getNameFromHandle(String handle) {
    	String name = null;
        IJavaElement element = JavaCore.create(handle);
        if (element == null) {
            System.err.println("EclipseUtils.getNameFromHandle:" + 
            		" No element created from " + handle);
            name = handle;
        } else {
            name = element.getElementName();
        }
		return name;
	}
	
	/**
	 * @param handle The Eclipse handle for a Java element
	 * @return the name of the project containing the handle
	 */
	public static String getProjectNameFromHandle(String handle) {
    	String name = null;
        IJavaElement element = JavaCore.create(handle);
        if (element == null) {
            System.err.println("EclipseUtils.getProjectNameFromHandle:" + 
            		" No element created from " + handle);
            name = handle;
        } else {
            name = element.getJavaProject().getElementName();
        }
		return name;
	}
	
	public static IType getTypeInEditor() {
		IType iType = null;
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		IEditorInput editorInput = editor.getEditorInput();
		
		if (editorInput instanceof FileEditorInput) {
			FileEditorInput fileInput = (FileEditorInput)editorInput;
			IFile file = fileInput.getFile();
			IJavaElement element = JavaCore.create(file);

			if (element instanceof IType) {
	        	// CompilationUnit
	            iType = (IType) element;
	        } else if (element instanceof ICompilationUnit) {
	        	ICompilationUnit iComp = (ICompilationUnit)element;
	        	iType = iComp.findPrimaryType();
	        } else {
	        	System.err.println("  No element created from " + file);
	        }
		}
		return iType;
	}

	public static CallData createCallData(String handle) throws JavaModelException {
		CallData callData = null;
		IJavaElement element = JavaCore.create(handle);
		if (element == null) {
			System.err.println("  No element created from " + handle);
		} else if (element instanceof IType) {
			try {
				callData = createCallData(element);
			} catch (Exception e) {
				// TODO proper error handling
				System.err.println("EclipseUtils.createCallData caught " + e
						+ ": " + e.getMessage());
				e.printStackTrace();
			}
		}
		return callData;
	}

	/**
	 * Gathers call information about the given class and stores the information
	 * about the members, attributes (fields), and call relationships between
	 * them. This information is obtainable via the various get* methods.
	 * @param element the type/class being examined
	 * @return info about the interrelationships of the class's members
	 * @throws JavaModelException
	 */
	public static CallData createCallData(IJavaElement element)
			throws JavaModelException {
		IType type = (IType) element;
		CallData callData = new CallData();
		callData.collectCallData(type);
		return callData;
	}

	/**
	 * Get the Eclipse handles for the members (methods and fields) of the
	 * specified class
	 * @param classHandle the Eclipse handle for the class
	 * @return a collection of handles
	 * @throws JavaModelException
	 */
	public static List<String> getFilteredMemberHandles(String classHandle)
			throws JavaModelException {
		//TODO initializers and inner classes too?
		List<String> memberHandles = new ArrayList<String>();
		IJavaElement element = JavaCore.create(classHandle);

		if (element == null) {
			System.err.println("  No element created from " + classHandle);
		} else if (element instanceof IType) {
			IType type = (IType) element;
			List<String> fields = getFilteredFieldHandles(type);
			memberHandles.addAll(fields);
			List<String> methods = getFilteredMethodHandles(type);
			memberHandles.addAll(methods);
		}
		return memberHandles;
	}

	public static List<String> getFilteredFieldHandles(IType type)
	throws JavaModelException {
		List<String> memberHandles = new ArrayList<String>();
		ApplicationParameters params =
			ApplicationParameters.getSingleton();
		boolean includeStatic = params.getBooleanParameter(
				ParameterConstants.INCLUDE_STATICS_KEY, true);
		boolean includeLoggers = params.getBooleanParameter(
				ParameterConstants.INCLUDE_LOGGERS_KEY, true);
		IField[] fields = type.getFields();
		for (IField field : fields) {
			if (field != null) {
				String className = field.getDeclaringType().getFullyQualifiedName();
				int flags = field.getFlags();
				if ((includeStatic || !Flags.isStatic(flags))
						&& (includeLoggers
								|| !RefactoringConstants.LOGGER_CLASS
								     .equals(className))) {
					memberHandles.add(field.getHandleIdentifier());
				}
			}
		}
		return memberHandles;
	}

	public static List<String> getFilteredMethodHandles(IType type)
	throws JavaModelException {
		List<String> memberHandles = new ArrayList<String>();
		ApplicationParameters params =
			ApplicationParameters.getSingleton();
		boolean includeConstructors = params.getBooleanParameter(
				ParameterConstants.INCLUDE_CONSTRUCTORS_KEY, false);
		boolean includeObjectMethods = params.getBooleanParameter(
				ParameterConstants.INCLUDE_OBJECT_METHODS_KEY, true);
		IMethod[] methods = type.getMethods();
		
		// Get all method handles except maybe constructors, Object methods
		for (IMethod method : methods) {
			if (method != null) {
				String methodHandle = method.getHandleIdentifier();
				if ((includeObjectMethods || !isRedefinedObjectMethod(methodHandle))
//						&& (methodHandle.indexOf("TipText") < 0)
					 && (includeConstructors || !method.isConstructor())) {
					memberHandles.add(methodHandle);
				}
			}
		}	// for
		return memberHandles;
	}

	/**
	 * Get the simple names for the members (methods and fields) of the
	 * specified class.  Note that there may be duplicates as methods may
	 * have the same name but different signatures.
	 * @param classHandle the Eclipse handle for the class
	 * @return a collection of simple names
	 * @throws JavaModelException
	 */
	public static List<String> getFilteredMemberNames(String classHandle)
			throws JavaModelException {
		List<String> memberNames = new ArrayList<String>();
		IJavaElement element = JavaCore.create(classHandle);
		ApplicationParameters params =
			ApplicationParameters.getSingleton();

		if (element == null) {
			System.err.println("  No element created from " + classHandle);
		} else if (element instanceof IType) {
			IType type = (IType) element;
			IField[] fields = type.getFields();
			boolean includeLoggers = params.getBooleanParameter(
					ParameterConstants.INCLUDE_LOGGERS_KEY, true);
			boolean includeStatic = params.getBooleanParameter(
					ParameterConstants.INCLUDE_STATICS_KEY, true);

			for (IField field : fields) {
				if (field != null) {
					String className = field.getDeclaringType().getFullyQualifiedName();
					int flags = field.getFlags();
					if ((includeStatic || !Flags.isStatic(flags))
							&& (includeLoggers
									|| !RefactoringConstants.LOGGER_CLASS
									     .equals(className))) {
						memberNames.add(field.getElementName());
					}
				}
			}

			boolean includeConstructors = params.getBooleanParameter(
					ParameterConstants.INCLUDE_CONSTRUCTORS_KEY, false);
			boolean includeObjectMethods = params.getBooleanParameter(
					ParameterConstants.INCLUDE_OBJECT_METHODS_KEY, true);
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {
				if (method != null) {
					String methodHandle = method.getHandleIdentifier();
					if ((includeObjectMethods || !isRedefinedObjectMethod(methodHandle))
						 && (includeConstructors || !method.isConstructor())) {
						memberNames.add(method.getElementName());
					}
				}
			}	// for
		}
		return memberNames;
	}

	public static void showClass(final IType iType) {
		try {
			final Display display = Display.getDefault();

			Thread uiThread =
			new Thread() {

				public void run() {

					display.syncExec(new Runnable() {

						public void run() {
							EclipseUtils.openInEditor(iType);
						}
					});
				}
			};
			uiThread.start();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Unable to show " + iType.getElementName() + ": " + e.getMessage(),
					"Error showing class ", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Finds the IType corresponding to the supplied class name
	 * @param the project from which to search
	 * @return the corresponding IType
	 */
	public static IType findType(IJavaElement projectElement, String typeName) {
//		IJavaElement projectElement = element.getAncestor(PROJECT);
		IType resultType = null;
		if (projectElement != null && projectElement instanceof IJavaProject) {
			IJavaProject project = (IJavaProject)projectElement;
			try {
				resultType = project.findType(typeName);
			} catch (JavaModelException e) {
				// TODO proper error handling
				e.printStackTrace();
			}
		}
		return resultType;
	}

	/**
	 * Determines whether the supplied handle matches one of the methods
	 * defined on Object that can be overridden (clone, equals, hashCode,
	 * toString).
	 * @param handle the Eclipse handle
	 * @return true if an Object method; false otherwise
	 */
	public static boolean isRedefinedObjectMethod(String handle) {
		boolean result =
//			handle.indexOf("java.lang(Object.") > -1;
			handle.endsWith("~hashCode")
			|| handle.endsWith("~equals~QObject;")
			|| handle.endsWith("~clone")
			|| handle.endsWith("~toString");
		return result;
	}

}
