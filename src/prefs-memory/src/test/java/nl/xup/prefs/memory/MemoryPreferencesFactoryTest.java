package nl.xup.prefs.memory;

import java.util.prefs.Preferences;

import junit.framework.TestCase;

/**
 * Unittests for MemoryPreferencesTest class
 * 
 * @author Minto van der Sluis
 */
public class MemoryPreferencesFactoryTest extends TestCase {

    // ----------------------------------------------------------------------
    // PreferencesFactory interface
    // ----------------------------------------------------------------------

    public void testImplementedInterface() {
        // First get us a factory instance.
        MemoryPreferencesFactory factory = new MemoryPreferencesFactory();
        
        // Get the root node from that factory.
        Preferences systemRoot = factory.systemRoot();
        Preferences userRoot = factory.userRoot(); 
        
        assertNotNull( systemRoot );
        assertFalse( systemRoot.isUserNode() );

        assertNotNull( userRoot );
        assertTrue( userRoot.isUserNode() );
    }
}
