/**
 *
 * Copyright 2006 David Blevins
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.codehaus.swizzle.jirareport;

/**
 * @version $Revision$ $Date$
 */
public class StringsUtil {

    public String lc(String string){
        if (string == null) return null;

        StringBuffer sb = new StringBuffer(string);
        for (int i = 0; i < sb.length(); i++) {
              sb.setCharAt(i, Character.toLowerCase(sb.charAt(i)));
        }
        return sb.toString();
    }

    public String uc(String string){
        if (string == null) return null;

        StringBuffer sb = new StringBuffer(string);
        for (int i = 0; i < sb.length(); i++) {
              sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
        }
        return sb.toString();
    }

    public String ucfirst(String string){
        if (string == null) return null;

        StringBuffer sb = new StringBuffer(string);
        if (sb.length() > 0){
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    public String lcfirst(String string){
        if (string == null) return null;

        StringBuffer sb = new StringBuffer(string);
        if (sb.length() > 0){
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        }
        return sb.toString();
    }

}
