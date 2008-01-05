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
package org.codehaus.swizzle.depreport;

import org.apache.maven.artifact.Artifact;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
 * @version $Revision$ $Date$
 */
public class XmlFormatter extends Formatter {
    private static final String indent = "  ";

    public XmlFormatter(PrintWriter out) {
        super(out);
    }

    public void format(Dependency root) throws IOException {
        out.println("<dependencies>");
        print(root.getChildern(), indent);
        out.println("</dependencies>");
    }

    private void print(List childern, String s) {
        Collections.sort(childern, new Comparator(){
            public int compare(Object o1, Object o2) {
                Artifact a = ((Dependency)o1).getArtifact();
                Artifact b = ((Dependency)o2).getArtifact();

                if (a.getGroupId().equals(b.getGroupId())){
                    // secondary sort by artifact id
                    return a.getArtifactId().compareTo(b.getArtifactId());
                }

                // primary sort by group id
                return a.getGroupId().compareTo(b.getGroupId());
            }
        });

        for (int i = 0; i < childern.size(); i++) {
            Dependency dep = (Dependency) childern.get(i);
            print(dep, s);
        }
    }

    private void print(Dependency dep, String s) {
        out.print(s);
        out.print("<dependency");

        print(s, "groupId", dep.getArtifact().getGroupId());
        print(s, "artifactId", dep.getArtifact().getArtifactId());
        print(s, "version", dep.getArtifact().getVersion());
        print(s, "file-name", dep.getArtifact().getFile().getName());

        if (dep.getArtifact().getDownloadUrl() != null){
            print(s, "download-url", dep.getArtifact().getDownloadUrl());
        }

        if (dep.getArtifact().isOptional()){
            print(s, "optional", dep.getArtifact().isOptional()+"");
        }

        if (dep.getChildern().size() > 0){
            out.println(">");
            print(dep.getChildern(), s + indent);
            out.print(s);
            out.println("</dependency>");
        } else {
            out.println("/>");
        }

    }

    private void print(String s, String name, String value) {
        out.print(" ");
        out.print(name);
        out.print("=\"");
        out.print(value);
        out.print("\"");
    }
}
