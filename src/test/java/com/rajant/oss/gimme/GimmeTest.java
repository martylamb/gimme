package com.rajant.oss.gimme;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class GimmeTest {
    

    @Test public void testRequiresInterface() {
        try {
            Gimme.set(5, Integer.class);
            fail("able to register a class");
        } catch (IllegalArgumentException expected) {}
        
        Gimme.set(5, Serializable.class);
    }
    
    @Test public void noNullInterfaces() {
        try {
            Gimme.set("hello", Serializable.class, CharSequence.class, null);
            fail("able to register a null interface");
        } catch (NullPointerException expected) {}
    }
    
    @Test public void noNullVarargs() {
        try {
            Gimme.set("hello", null);
            fail("able to register a null vararg");
        } catch (IllegalArgumentException expected) {}
    }
    
    @Test public void noNullImplementation() {
        try {
            Gimme.set(null, CharSequence.class);
            fail("able to register a null implementation");
        } catch (NullPointerException expected) {}        
    }
    
    @Test public void noNullSupplier() {        
        try {
            Gimme.setSupplier(null, CharSequence.class);
            fail("able to register a null supplier");
        } catch (NullPointerException expected) {}        
    }
    
    @Test public void testThrow() {
        try {
            Gimme.a(Runnable.class);
            fail("did not throw");
        } catch (Gimme.NotFoundException expected) {}        
    }
    
    @Test public void testOptional() {
        String s = "Test String";        
        Gimme.set(s, CharSequence.class);

        assertEquals(s, Gimme.optional(CharSequence.class).get());
        assertFalse(Gimme.optional(Comparable.class).isPresent());
    }
    
    @Test public void testMultipleGets() throws Gimme.NotFoundException {
        String s = "Test String";        
        Gimme.set(s, CharSequence.class);
        
        assertEquals(s, Gimme.a(CharSequence.class));
        assertEquals(s, Gimme.an(CharSequence.class));
        assertEquals(s, Gimme.optional(CharSequence.class).get());
        assertEquals(s, Gimme.a(CharSequence.class));
    }
}
