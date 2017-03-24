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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApplicationParameters {
	
	private static ApplicationParameters singleton =
		new ApplicationParameters();

	/** Private constructor to provide singleton. */
	private ApplicationParameters() {
	}
	
	/**
	 * @return the singleton
	 */
	public static ApplicationParameters getSingleton() {
		return singleton;
	}

	protected Properties properties = new Properties();

	/** Read in the properties from the specified file. */
	public Properties loadProperties(String fileName) throws IOException {
		FileInputStream fileStream = new FileInputStream(fileName);
		properties.load(fileStream);
		fileStream.close();
		return properties;
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	/**
	 * Retrieve the stored value for the parameter if it exists.
	 * If not, use the provided default value
	 * @param key the parameter name
	 * @param value the default value to use if none is stored
	 * @return the parameter's value
	 */
	public String getParameter(String key, String value)
	{
		if (properties != null) {
			String propValue = properties.getProperty(key);
			if (propValue != null) {
				value = propValue;
			}
		}
		return value;
	}
	
	/**
	 * Retrieve the stored value for the parameter if it exists.
	 * If not, use the provided default value
	 * @param key the parameter name
	 * @param value the default value to use if none is stored
	 * @return the parameter's value
	 */
	public boolean getBooleanParameter(String key, boolean value)
	{
		if (properties != null) {
			String sValue = properties.getProperty(key);
			if (sValue != null) {
				value = sValue.equalsIgnoreCase("true");
			}
		}
		return value;
	}
	
	/**
	 * Retrieve the stored value for the parameter if it exists.
	 * If not, use the provided default value
	 * @param key the parameter name
	 * @param value the default value to use if none is stored
	 * @return 0 for false; 1 for true
	 */
	public int getBooleanParameterAsInt(String key, boolean value)
	{
		value = getBooleanParameter(key, value);
		return value?1:0;
	}
	
	/**
	 * Retrieve the stored value for the parameter if it exists.
	 * If not, use the provided default value
	 * @param key the parameter name
	 * @param value the default value to use if none is stored
	 * @return the parameter's value
	 */
	public int getIntParameter(String key, int value)
	{
		if (properties != null) {
			String sValue = properties.getProperty(key);
			if (sValue != null) {
				try {
				    value = Integer.decode(sValue); // new Integer(sValue);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return value;
	}
	
	/**
	 * Store the value for the parameter
	 * @param key the parameter name
	 * @param value the value to store
	 */
	public void setParameter(String key, String value)
	{
		if (properties == null) {
			properties = new Properties();
		}
		properties.setProperty(key, value);
	}
	
	public String toString() {
		String result = getClass().getSimpleName();
		if (properties != null) {
			result += ": " + properties.toString();
		}
		return result;
	}
}
