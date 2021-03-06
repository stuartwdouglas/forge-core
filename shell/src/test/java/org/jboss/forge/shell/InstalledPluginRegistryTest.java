/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.forge.shell;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.forge.shell.InstalledPluginRegistry.PluginEntry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class InstalledPluginRegistryTest
{
    private List<PluginEntry> installed;

    @Before
    public void before()
    {
        this.installed = InstalledPluginRegistry.list();
        for (PluginEntry p : installed)
        {
            InstalledPluginRegistry.remove(p);
        }
        Assert.assertTrue(InstalledPluginRegistry.list().isEmpty());
    }

    @After
    public void after()
    {
        List<PluginEntry> current = InstalledPluginRegistry.list();
        for (PluginEntry p : current)
        {
            InstalledPluginRegistry.remove(p);
        }
        for (PluginEntry p : installed)
        {
            InstalledPluginRegistry.install(p.getName(), p.getApiVersion(), p.getSlot());
        }
    }

    /**
     * Test method for {@link org.jboss.forge.shell.InstalledPluginRegistry#list()}.
     */
    @Test
    public void testGetInstalledPlugins()
    {
        List<PluginEntry> originals = InstalledPluginRegistry.list();
        assertFalse(InstalledPluginRegistry.has(new PluginEntry("test.test", "1.0.0-SNAPSHOT", "moo")));
        PluginEntry installed = InstalledPluginRegistry.install("test.test", "1.0.0-SNAPSHOT", "moo");
        assertTrue(InstalledPluginRegistry.has(installed));
        List<PluginEntry> plugins = InstalledPluginRegistry.list();

        boolean found = false;
        for (PluginEntry plugin : plugins)
        {
            if ("test.test:1.0.0-SNAPSHOT:moo".equals(plugin.toCoordinates()))
            {
                InstalledPluginRegistry.remove(plugin);
                assertFalse(InstalledPluginRegistry.has(plugin));
                found = true;
            }
        }
        assertTrue(found);

        assertSame(originals.size(), InstalledPluginRegistry.list().size());
        for (PluginEntry plugin : originals)
        {
            assertTrue(InstalledPluginRegistry.has(plugin));
            InstalledPluginRegistry.remove(plugin);
            assertFalse(InstalledPluginRegistry.has(plugin));
        }
    }

    /**
     * Test method for {@link org.jboss.forge.shell.InstalledPluginRegistry#list()}.
     */
    @Test
    public void testAddNewVersion()
    {
        List<PluginEntry> originals = InstalledPluginRegistry.list();
        InstalledPluginRegistry.install("test.test", "1.0.0-SNAPSHOT", "moo");
        InstalledPluginRegistry.install("test.test", "1.0.0-SNAPSHOT", "foo");
        List<PluginEntry> plugins = InstalledPluginRegistry.list();

        boolean found = false;
        for (PluginEntry plugin : plugins)
        {
            if (plugin.getName().equals("test.test"))
            {
                InstalledPluginRegistry.remove(plugin);
                assertFalse(InstalledPluginRegistry.has(plugin));
                found = true;
            }
        }
        assertTrue(found);

        assertSame(originals.size(), InstalledPluginRegistry.list().size());
        for (PluginEntry plugin : originals)
        {
            assertTrue(InstalledPluginRegistry.has(plugin));
            InstalledPluginRegistry.remove(plugin);
            assertFalse(InstalledPluginRegistry.has(plugin));
        }
    }

    @Test
    public void testMultipleVersions() throws Exception
    {
        PluginEntry one = InstalledPluginRegistry.install("foo", "1", "s1");
        PluginEntry two = InstalledPluginRegistry.install("foo", "2", "s2");

        assertFalse(InstalledPluginRegistry.has(one));
        assertTrue(InstalledPluginRegistry.has(two));

        InstalledPluginRegistry.remove(one);
        InstalledPluginRegistry.remove(two);

        assertFalse(InstalledPluginRegistry.has(one));
        assertFalse(InstalledPluginRegistry.has(two));
    }

    @Test(expected = IllegalStateException.class)
    public void testMinorVersionException() throws Exception
    {
        PluginEntry entry = PluginEntry.fromCoordinates("com.example.plugin:1.0.0-SNAPSHOT:main");
        Assert.assertFalse(InstalledPluginRegistry.isApiCompatible(null, entry));
    }

    @Test(expected = IllegalStateException.class)
    public void testMinorVersionException2() throws Exception
    {
        Assert.assertFalse(InstalledPluginRegistry.isApiCompatible("", (PluginEntry)null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinorVersionException3() throws Exception
    {
        PluginEntry entry = PluginEntry.fromCoordinates("com.example.plugin::main");
        Assert.assertFalse(InstalledPluginRegistry.isApiCompatible("", entry));
    }

    @Test
    public void testMinorVersionCompatible() throws Exception
    {
        PluginEntry entry = PluginEntry.fromCoordinates("com.example.plugin:1.0.0-SNAPSHOT:main");
        Assert.assertTrue(InstalledPluginRegistry.isApiCompatible("1.0.1.Final", entry));
        Assert.assertTrue(InstalledPluginRegistry.isApiCompatible("1.0.2.Final", entry));
        Assert.assertTrue(InstalledPluginRegistry.isApiCompatible("1.0.2000.Final", entry));
        Assert.assertTrue(InstalledPluginRegistry.isApiCompatible("1.0.2-SNAPSHOT", entry));
        Assert.assertTrue(InstalledPluginRegistry.isApiCompatible("1.0.1000-SNAPSHOT", entry));
        Assert.assertTrue(InstalledPluginRegistry.isApiCompatible("1.0.1000-adsfasfsd", entry));
        Assert.assertFalse(InstalledPluginRegistry.isApiCompatible("1.1.0.Final", entry));
        Assert.assertFalse(InstalledPluginRegistry.isApiCompatible("2.0.0.Final", entry));
        Assert.assertFalse(InstalledPluginRegistry.isApiCompatible("s1.0.0.Final", entry));
        Assert.assertFalse(InstalledPluginRegistry.isApiCompatible("", entry));
    }

}
