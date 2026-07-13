package com.trickshotmlg.friendnet.core.permissions;

import junit.framework.TestCase;

import java.util.List;

public class PermissionTest extends TestCase {

    Permission p = new Permission("p", null);
    Permission p1_1 = new Permission("p1_1", p);
    Permission p1_2 = new Permission("p1_2", p);

    Permission p2_1 = new Permission("p2_1", p1_1);
    Permission p2_2 = new Permission("p2_2", p1_2);

    Permission p3_1 = new Permission("p3_1", p2_1);
    Permission p3_2 = new Permission("p3_2", p2_2);

    String prefix = "GLOBAL_PREFIX";

    @Override
    protected void setUp() throws Exception {
        p.setGlobalPrefix(prefix);
    }

    public void testSetGlobalPrefix() {
        assertEquals(prefix + "." + p.getPermission(), p.getPermissionPrefixed());
        assertEquals(prefix + "." + p3_1.getPermission(), p3_1.getPermissionPrefixed());
    }

    public void testGetPermission() {
        assertEquals("p", p.getPermission());
        assertEquals("p.p1_1.p2_1", p2_1.getPermission());
        assertEquals("p.p1_2.p2_2.p3_2", p3_2.getPermission());
    }

    public void testGetPermissionPrefixed() {
        assertEquals(prefix + ".p", p.getPermissionPrefixed());
        assertEquals(prefix + ".p.p1_1.p2_1", p2_1.getPermissionPrefixed());
        assertEquals(prefix + ".p.p1_2.p2_2.p3_2", p3_2.getPermissionPrefixed());
    }

    public void testGetParent() {
        assertEquals(p2_2, p3_2.getParent());
        assertEquals(null, p.getParent());
    }

    public void testGetChildren() {
        assertEquals(List.of(p1_1, p1_2), p.getChildren());
        assertEquals(List.of(p2_1), p1_1.getChildren());
        assertEquals(List.of(), p3_1.getChildren());
    }

    public void testAddChild() {
        assertEquals(List.of(), p3_1.getChildren());
        Permission tmp = new Permission("tmp", p3_1);
        assertEquals(List.of(tmp), p3_1.getChildren());
    }

    public void testToStringContainsPermissionDetails() {
        String value = p1_1.toString();

        assertTrue(value.contains("permission='p.p1_1'"));
        assertTrue(value.contains("prefixed='" + prefix + ".p.p1_1'"));
        assertTrue(value.contains("children=1"));
    }

    public void testHas() {
        //assertEquals("", PermissionHolder.FRIEND_REQUESTS_DENY.getPermissionPrefixed());
    }
}
