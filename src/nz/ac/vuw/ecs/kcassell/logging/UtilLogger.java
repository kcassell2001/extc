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

package nz.ac.vuw.ecs.kcassell.logging;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.ac.vuw.ecs.kcassell.ClassRefactoringPlugin;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

/**
 * A class to route logging appropriately, depending on the
 * application context (e.g. within Eclipse or standalone).
 * @see http://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_logging_facility%3F
 * @author kcassell
 *
 */
public class UtilLogger {
	
	/** The java logger, used when not executing in a plug-in. */
	private Logger javaLogger = null;
	
	/** The logger to be used when executing in an Eclipse plug-in. */
	private ILog pluginLogger = null;
	
	private static boolean changedRootLogger = false;

	public UtilLogger(String loggerName) {
		//TODO figure out identifiers: Help->About...->Installation Details->Plugins
		// lists the plugin name as "ClassRefactoring",
		// but the plugin ID as "classRefactoringPlugin"

		if (pluginLogger == null) {
			Bundle bundle = null;
			bundle = Platform.getBundle(ClassRefactoringPlugin.PLUGIN_ID);
//			bundle = Platform.getBundle("ClassRefactoring");
			
			if (bundle != null) {
				pluginLogger = Platform.getLog(bundle);
			}
		}
		if (pluginLogger == null) {
			javaLogger = Logger.getLogger(loggerName);
			
			if (!changedRootLogger) {
				resetFileHandler(javaLogger, new ConciseLogFormatter());
			}
		}
	}
	
	/**
	 * Reset the logger's FileHandler's formatter.  (An Eclipse bug prevents
	 *  this being done via the logging properties.
	 * @param logger
	 * @param formatter
	 */
	protected void resetFileHandler(Logger logger,
			ConciseLogFormatter formatter) {
		// TODO set other FileHandler properties?
//		java.util.logging.FileHandler.pattern=%h/workspace/ClassRefactoringPlugin/extc%g.log
//		java.util.logging.FileHandler.limit = 100000
//		java.util.logging.FileHandler.count = 10
//		#java.util.logging.FileHandler.formatter = java.util.logging.XMLFormatter
//		java.util.logging.FileHandler.formatter = nz.ac.vuw.ecs.kcassell.logging.ConciseLogFormatter
//		java.util.logging.FileHandler.level = FINE
		if (logger != null) {
			Handler[] handlers = logger.getHandlers();
			if (handlers == null || handlers.length == 0) {
				Logger parent = logger.getParent();
				if (parent != null) {
					handlers = parent.getHandlers();
				}
			}
			for (Handler handler : handlers) {
				if (handler instanceof FileHandler) {
					handler.setFormatter(formatter);
					changedRootLogger = true;
				}
			}
		}
	}

	public void log(String msg) {
		log(msg, null);
	}

	public void log(String msg, Exception e) {
		pluginLogger.log(
				new Status(Status.INFO, ClassRefactoringPlugin.PLUGIN_ID,
						Status.OK, msg, e));
	}
	
	public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
		javaLogger.logp(level, sourceClass, sourceMethod, msg); 
	}

	public void finer(String string) {
		if (pluginLogger != null) {
			pluginLogger.log(new Status(Status.INFO,
					ClassRefactoringPlugin.PLUGIN_ID, string));
		} else {
			javaLogger.finer(string);
		}
	}

	public void fine(String string) {
		if (pluginLogger != null) {
			pluginLogger.log(new Status(Status.INFO,
					ClassRefactoringPlugin.PLUGIN_ID, string));
		} else {
			javaLogger.fine(string);
		}
	}

	public void info(String string) {
		if (pluginLogger != null) {
			pluginLogger.log(new Status(Status.INFO,
					ClassRefactoringPlugin.PLUGIN_ID, string));
		} else {
			javaLogger.info(string);
		}
	}

	public void warning(String string) {
		if (pluginLogger != null) {
			pluginLogger.log(new Status(Status.WARNING,
					ClassRefactoringPlugin.PLUGIN_ID, string));
		} else {
			javaLogger.warning(string);
		}
	}

	public void severe(String string) {
		if (pluginLogger != null) {
			pluginLogger.log(new Status(Status.ERROR,
					ClassRefactoringPlugin.PLUGIN_ID, string));
		} else {
			javaLogger.severe(string);
		}
	}
}