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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ConciseLogFormatter extends Formatter
{
    protected static final String DELIM = ",";
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm:ss.SSS");

    @Override
    /**
     * Creates a log record like:
     * <code>
         9:51:32.784,FINE,XToolkit.setupModifierMap, modeSwitchMask = 128
       </code>
       The class name and method are given (without the package information).
     */
    public String format(LogRecord record)
    {
        StringBuffer buf = new StringBuffer();
        long millis = record.getMillis();
        buf.append(dateFormat.format(new Date(millis))).append(DELIM);
        buf.append(record.getLevel()).append(DELIM);
//        String className = record.getSourceClassName();
//        String[] classNameParts = className.split("\\.");
//        
//        if (classNameParts.length > 0)
//        {
//            buf.append(classNameParts[classNameParts.length - 1]).append(".");
//        }
//        else
//        {
//            buf.append(className).append(".");
//        }
//        buf.append(record.getSourceMethodName()).append(DELIM);
        buf.append(" ").append(record.getMessage()).append("\n");
        return buf.toString();
    }
    

}
