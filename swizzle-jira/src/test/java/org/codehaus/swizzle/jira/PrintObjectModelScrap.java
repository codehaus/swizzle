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
package org.codehaus.swizzle.jira;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class PrintObjectModelScrap extends TestCase {

    public static void main(String[] args) throws Exception{
        new PrintObjectModelScrap().testGo();
    }
    public void testGo() throws Exception {
        skip.add("toHashtable");
        skip.add("toString");
        skip.add("equals");
        skip.add("compareTo");
        skip.add("hashCode");
        print(Jira.class);
        print(Comment.class);
        print(JiraRss.class);
        // fail("");
    }

    public void print(Class clazz) throws Exception {
        if (seen.contains(clazz)) return;

        seen.add(clazz);

        String shortName = name(clazz);
//        System.out.println(" * ["+shortName+"|Jira Object Model#"+shortName+"]");
        System.out.println("\nh2. " + shortName);
        System.out.println("");
        System.out.println("[Source|http://fisheye.codehaus.org/browse/swizzle/trunk/swizzle-jira/src/main/java/org/codehaus/swizzle/jira/"+shortName+".java?r=trunk]");
        System.out.println("{anchor:"+shortName+"}");
        Method[] methods = clazz.getMethods();
        Arrays.sort(methods, new Comparator(){
            public int compare(Object object, Object object1) {
                Method a = (Method) object;
                Method b = (Method) object1;

                return name(a).compareTo(name(b));
            }

            private String name(Method method){
                String name = method.getName();
                return name.replaceFirst("^(get|set|add|remove|delete|create|update)","");
            }
        });
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];

            print(method);
        }

        for (int i = 0; i < next.size(); i++) {
            Class type = (Class) next.get(i);
            print(type);
        }
    }

    private List next = new ArrayList();
    private List seen = new ArrayList();
    private List skip = new ArrayList();

    private void print(Method method) throws Exception {
        if (skip.contains(method.getName())) return;
        boolean isPublic = Modifier.isPublic(method.getModifiers());
        boolean isNotStatic = !Modifier.isStatic(method.getModifiers());
        if (!isPublic || !isNotStatic) {
            return;
        }
        if (!method.getDeclaringClass().getName().startsWith("org.codehaus.swizzle")) return;

        List types = new ArrayList();
        types.addAll(Arrays.asList(method.getParameterTypes()));
        types.add(method.getReturnType());

        for (int i = 0; i < types.size(); i++) {
            Class type = (Class) types.get(i);
            if (type.getName().startsWith("org.codehaus.swizzle.jira")) {
                next.add(type);
            }

        }

        Class type = method.getReturnType();
        System.out.print("| " + name(type));
        System.out.print(" | " + method.getName());
        Class[] params = method.getParameterTypes();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < params.length; i++) {
            Class param = params[i];
            sb.append(" _"+name(param)+"_,");
        }
        if (sb.length() > 0){
            sb.deleteCharAt(sb.length()-1);
        }
        System.out.print(sb.toString()+" |");
        System.out.println(" |");

    }

    private String name(Class type) {
        return type.getName().replaceFirst("^.*\\.","");
    }
}
