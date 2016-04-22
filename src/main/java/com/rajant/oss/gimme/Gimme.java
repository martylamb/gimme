package com.rajant.oss.gimme;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A simple service locator for dependency inversion
 */
public class Gimme {

    /** suppliers of every implementation we can provide, keyed by interface */
    private static final Map<Class<?>, Supplier> _suppliers = new java.util.HashMap<>();
    
    /**
     * Unregisters all registered singletons and suppliers
     */
    public static void clear() {
        synchronized(_suppliers) {
            _suppliers.clear();
        }
    }
    
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
     * Returns an implementation of the specified interface, or throws a NotFoundException if no implementation is available
     * @param iface the interface for which an implementation is desired
     * @return an implementation of the specified interface, or null if no implementation is available
     * @throws NotFoundException if no implementation is available
     */
    public static <T> T a(Class<? super T> iface) throws NotFoundException {
        Supplier<T> supplier;
        synchronized(_suppliers) {
            supplier = _suppliers.get(iface);
        }
        if (supplier == null) throw new NotFoundException(iface);
        return supplier.get();
    }
    
    /**
     * Returns an implementation of the specified interface, or throws a NotFoundException if no implementation is available
     * Exactly equivalent to `a(Class<? super T> iface)`
     * @see Gimme#a(java.lang.Class) 
     * @param iface the interface for which an implementation is desired
     * @return an implementation of the specified interface, or null if no implementation is available
     * @throws NotFoundException if no implementation is available
     */
    public static <T> T an(Class<? super T> iface) throws NotFoundException { return a(iface); }
    
    /**
     * Convenience method to return an Optional<T> wrapping around a call to {@link Gimme#a(java.lang.Class)}
     * No exception is thrown if an implementation is not available.
     * 
     * @param iface the interface for which an implementation is desired
     * @return an Optional wrapping an implementation of the specified interface, or Optional.empty if no implementation is available
     */
    public static <T> Optional<T> optional(Class<? super T> iface) {
        try {
            return Optional.of(an(iface));
        } catch (NotFoundException ohWell) {
            return Optional.empty();
        }        
    }
    
    /**
     * Given one or more interfaces classes, throws a NotFoundException if any
     * lack a registered implementation
     * 
     * @param ifaces the interfaces required by the client
     * @throws com.rajant.oss.gimme.Gimme.NotFoundException 
     */
    public static void require(Class<?>... ifaces) throws NotFoundException {
        synchronized(_suppliers) {
            for (Class<?> clazz : ifaces) {
                if (clazz == null) throw new NullPointerException("null interface specified as required");
                if (!_suppliers.containsKey(clazz)) throw new NotFoundException(clazz);
            }
        }
    }
    
    /**
     * Returns a boolean indicating whether any of the specified interface classes
     * lack a registered implementation
     * 
     * @param ifaces the interfaces required by the client
     * @return a boolean indicating whether any of the specified interface classes
     * lack a registered implementation
     */
    public static boolean has(Class<?>... ifaces) {
        try {
            require(ifaces);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    // type will be either "implementation" or "supplier", just used for message clarity
    private static <T> T requireNonNull(T t, String type) {
        if (t == null) throw new NullPointerException(type + " may not be null");
        return t;
    }
    
    private static <T> Class<? super T>[] requireAtLeastOneElementIn(Class<? super T>[] ifaces) {
        if (ifaces == null || ifaces.length == 0) throw new IllegalArgumentException("at least one interface must be specified");        
        return ifaces;
    }
    
    public static class NotFoundException extends Exception {
        private NotFoundException(Class iface) { super("implementation not found: " + iface); }
    }
}
