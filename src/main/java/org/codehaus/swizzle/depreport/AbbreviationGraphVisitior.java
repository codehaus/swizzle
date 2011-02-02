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

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class AbbreviationGraphVisitior implements Dependency.Visitor {

    public boolean visit(Dependency node) {
        List<Dependency> childern = node.getChildren();
        for (Dependency child : childern) {
            Set<Dependency> descendants = child.getDescendants();
            for (Dependency decendant : descendants) {

                if (childern.contains(decendant)){ // referenced higher up
                    List<Trail> trails = new ArrayList(decendant.getTrails());
                    for (Trail trail : trails) {
                        if (trail.contains(child)) decendant.unlink(trail);
                    }
                }
            }
        }
        return true;
    }

    public boolean endVisit(Dependency node) {
        return true;
    }
}
