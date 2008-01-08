package org.codehaus.swizzle.depreport;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.swizzle.depreport.ReportMojo;

/**
 * @author Edwin Punzalan
 */
public class ReportMojoTest
        extends AbstractMojoTestCase {
    private String basedir = PlexusTestCase.getBasedir();

    public void testMinConfiguration()
            throws Exception {
        executeMojo("min-plugin-config.xml");
    }

    private ReportMojo getMojo(String pluginXml)
            throws Exception {
        return (ReportMojo) lookupMojo("report", basedir + "/src/test/plugin-configs/report/" + pluginXml);
    }

    private ReportMojo executeMojo(String pluginXml)
            throws Exception {
        ReportMojo mojo = getMojo(pluginXml);

        mojo.execute();

        return mojo;
    }

}
