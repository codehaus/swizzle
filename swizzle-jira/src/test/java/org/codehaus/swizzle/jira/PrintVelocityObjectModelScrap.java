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

import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class PrintVelocityObjectModelScrap extends TestCase {

    public static void main(String[] args) throws Exception{
        new PrintVelocityObjectModelScrap().testGo();
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
        fail("");
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
        System.out.println("|| Type || Name || Description || xml-rpc || rss || requires filler ||");
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().startsWith("get")){
                print(method);
            }
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
        StringBuffer property = new StringBuffer(method.getName());
        property.replace(0,3,"");
        char c = property.charAt(0);
        property.setCharAt(0, Character.toLowerCase(c));

        System.out.print(" | " + property.toString());
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
        System.out.print(" |");
        System.out.print(" (/) |");
        System.out.print(" (/) |");
        System.out.println(" |");

    }

    private String name(Class type) {
        return type.getName().replaceFirst("^.*\\.","");
    }
}
