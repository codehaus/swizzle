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

import org.apache.velocity.VelocityContext;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class ParamsUtil {

    private final List missingArgs = new ArrayList();
    private final VelocityContext context;

    public ParamsUtil(VelocityContext context) {
        this.context = context;
    }

    public void required(String param, String regex, String description) {
        Object object = context.get(param);
        if ( object == null ){
            missingArgs.add(new Param(Param.MISSING, param, regex, description));
        } else if (!object.toString().matches(regex)) {
            missingArgs.add(new Param(Param.INVALID, param, regex, description));
        }
    }

    public void validate() throws IllegalArgumentException {
        if (missingArgs.size() == 0){
            return;
        }
        String lineSeparator = System.getProperty("line.separator");

        StringBuffer sb = new StringBuffer();
        sb.append("Invalid or missing arguments.");
        sb.append(lineSeparator);

        for (int i = 0; i < missingArgs.size(); i++) {
            Param param = (Param) missingArgs.get(i);
            sb.append("  [");
            sb.append(param.getStatus());
            sb.append("]    ");
            sb.append(param.getName());
            sb.append("    : ");
            sb.append(param.getDescription());
            sb.append(". Must Match Pattern '");
            sb.append(param.getRegex());
            sb.append("'");
            sb.append(lineSeparator);
        }

        throw new MissingParamsException(sb.toString(), (Param[]) missingArgs.toArray(new Param[]{}));
    }

}
