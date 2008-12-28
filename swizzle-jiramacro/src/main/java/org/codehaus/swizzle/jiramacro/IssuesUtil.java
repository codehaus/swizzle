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
package org.codehaus.swizzle.jiramacro;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @version $Revision$ $Date$
 */
public class IssuesUtil {

    private static final ThreadLocal context = new ThreadLocal();

    public String param(List issues) {
        if (issues == null) {
            //throw new NullPointerException("as.param: issues list is null");
            return "null";
        }
        Random random = new Random();
        String id = issues.toString() + random.nextInt();
        id = id.replace('|', '.');
        id = "ref::" + id;
        Map map = issuesMap();
        map.put(id, issues);
        return id;
    }

    public static Map issuesMap() {
        Map map = (Map) context.get();
        if (map == null) {
            map = new HashMap();
            context.set(map);
        }

        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
        }
        return map;
    }

    public static List getIssues(String id){
        if (id.equals("null")){
            return Collections.EMPTY_LIST;
        }
        return (List) issuesMap().remove(id);
    }

    public static void clear() {
        issuesMap().clear();
        context.set(null);
    }

}
