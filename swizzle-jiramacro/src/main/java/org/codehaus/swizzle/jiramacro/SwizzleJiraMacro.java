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

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.macro.basic.AbstractPanelMacro;
import org.apache.velocity.VelocityContext;
import org.codehaus.swizzle.jirareport.Main;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Iterator;


/**
 * @version $Revision$ $Date$
 */
public class SwizzleJiraMacro extends AbstractPanelMacro {

    protected String getPanelContentCSSClass() {
        return "jirareportContent";
    }

    protected String getPanelCSSClass() {
        return "jirareport";
    }

    protected String getPanelHeaderCSSClass() {
        return "jirareportHeader";
    }

    public RenderMode getBodyRenderMode() {
        return RenderMode.allow(RenderMode.F_NONE);
    }

    public boolean suppressMacroRenderingDuringWysiwyg() {
        return false;
    }

    protected SubRenderer getSubRenderer() {
        return super.getSubRenderer();
    }

    public String execute(Map params, String body, com.atlassian.renderer.RenderContext renderContext) throws MacroException {

        URL templateUrl = null;
        try {
            String url = (String) params.get("template");
            if (url != null) {
                templateUrl = new URL(url);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "Invalid template url: " + e.getMessage();
        }

        File tempFile = null;
        String content = body;
        String template;
        if (templateUrl != null) {
            template = templateUrl.toExternalForm();
        } else {
            try {
                tempFile = File.createTempFile("swizzlejira", ".vm");
                FileWriter fileWriter = new FileWriter(tempFile);
                fileWriter.write(content);
                fileWriter.flush();
                fileWriter.close();
                template = tempFile.getAbsolutePath();
            } catch (IOException e) {
                throw new MacroException("Unable to save template content to temp file.", e);
            }
        }
        PrintStream out = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            out = new PrintStream(baos);

            Map defaults = MacroUtils.defaultVelocityContext();
            for (Iterator iterator = params.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                defaults.put(entry.getKey(), entry.getValue());
            }

            VelocityContext context = new VelocityContext(defaults);
            context.put("as", new IssuesUtil());
            Main.generate(context, template, out);
        } catch (Exception e) {
            CharArrayWriter trace = new CharArrayWriter();
            PrintWriter writer = new PrintWriter(trace);
            writer.println("Template: " + template);
            e.printStackTrace(writer);
            writer.flush();
            writer.close();
            String error = new String(trace.toCharArray());
            return error.replaceAll("\n", "<br>");
        }

        try {
            out.flush();
            out.close();
            tempFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }


        SubRenderer subRenderer = getSubRenderer();
        String velocityOutput = new String(baos.toByteArray());
        return subRenderer.render(velocityOutput, renderContext);
    }

    public String getName() {
        return "swizzlejira";
    }
}
