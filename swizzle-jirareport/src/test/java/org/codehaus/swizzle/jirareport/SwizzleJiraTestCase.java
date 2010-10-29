package org.codehaus.swizzle.jirareport;

import org.codehaus.swizzle.jira.Jira;

import junit.framework.TestCase;

public abstract class SwizzleJiraTestCase
    extends TestCase
{
    protected Jira getJira()
        throws Exception
    {
        Jira jira = new Jira( "http://jira.codehaus.org/rpc/xmlrpc" );
        jira.login( "swizzle", "swizzle" );
        return jira;
    }
}
