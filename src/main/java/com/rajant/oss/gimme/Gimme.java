package com.rajant.oss.gimme;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A simple service locator for dependency inversion
 */
public class Gimme {
    
    private static final Map<Class<?>, Supplier> _suppliers = new java.util.HashMap<>();
    
    /**
     * Registers a singleton object as the service implementation for one or more interfaces
     * @param singleton the singleton being registered
     * @param ifaces the interfaces for which the singleton will be provided
     * @return the registered singleton
     */
    public static <T> T set(T singleton, Class<? super T>... ifaces) {        
        requireNonNull(singleton, "implementation");
        setSupplier(() -> singleton, ifaces);
        return singleton;
    }

    /**
     * Registers a Supplier to provide service implementations for one or more interfaces
     * @param supplier the Supplier being registered
     * @param ifaces the interfaces for which the Supplier will provide implementations
     * @return the registered Supplier
     */
    public static <T> Supplier<T> setSupplier(Supplier<T> supplier, Class<? super T>... ifaces) {
        requireAtLeastOneElementIn(ifaces);
        requireNonNull(supplier, "supplier");
        synchronized(_suppliers) {
            for (Class<? super T> iface : ifaces) {
                if (iface == null) throw new NullPointerException("null interface specified");
                if (!iface.isInterface()) throw new IllegalArgumentException("services can only be registered against interfaces:  impl[" + iface + "]");
                _suppliers.put(iface, supplier);
            }
        }
        return supplier;
    }
    
    /**
     * Returns an implementation of the specified interface, or null if no implementation is available
     * @param iface the interface for which an implementation is desired
     * @return an implementation of the specified interface, or null if no implementation is available
     */
    public static <T> T a(Class<? super T> iface) {
        synchronized(_suppliers) {
            Supplier<T> supplier = _suppliers.get(iface);
            return (supplier == null) ? null : supplier.get();
        }
    }
    
    /**
     * Exactly equivalent to `a(Class<? super T> iface)`
     * @see Gimme#a(java.lang.Class) 
     */
    public static <T> T an(Class<? super T> iface) { return a(iface); }
    
    /**
     * Convenience method to return an Optional<T> wrapping around a call to {@link Gimme#a(java.lang.Class)}
     */
    public static <T> Optional<T> optional(Class<? super T> iface) {
        return Optional.ofNullable(a(iface));
    }
    
    /**
     * Returns an implementation of the specified interface, or throws a NotFoundException if no implementation is available
     * @param iface the interface for which an implementation is desired
     * @return an implementation of the specified interface
     * @throws NotFoundException if no implementation is available
     */
    public static <T> T orThrow(Class<? super T> iface) throws NotFoundException {
        T result = a(iface);
        if (result == null) throw new NotFoundException("implementation not available: " + iface);
        return result;
    }
            
    private static <T> T requireNonNull(T t, String type) {
        if (t == null) throw new NullPointerException("failed to register null service " + type);
        return t;
    }
    
    private static <T> Class<? super T>[] requireAtLeastOneElementIn(Class<? super T>[] ifaces) {
        if (ifaces == null || ifaces.length == 0) throw new IllegalArgumentException("at least one interface must be specified");        
        return ifaces;
    }
    
    public static class NotFoundException extends Exception {
        private NotFoundException(String msg) { super(msg); }
    }
}
