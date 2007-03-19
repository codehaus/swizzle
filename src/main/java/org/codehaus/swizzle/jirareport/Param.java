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
public class Param {
    public static String MISSING = "MISSING";
    public static String INVALID = "INVALID";

    private final String status;
    private final String name;
    private final String regex;
    private final String description;

    public Param(String status, String name, String regex, String description) {
        this.status = status;
        this.description = description;
        this.name = name;
        this.regex = regex;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }

    public String getStatus() {
        return status;
    }
}
