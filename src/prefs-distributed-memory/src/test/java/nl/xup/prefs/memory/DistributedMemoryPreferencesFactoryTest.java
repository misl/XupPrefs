package nl.xup.prefs.memory;

import java.util.prefs.Preferences;

import junit.framework.TestCase;

/**
 * Unittests for DistributedMemoryPreferencesTest class
 * 
 * @author Minto van der Sluis
 */
public class DistributedMemoryPreferencesFactoryTest extends TestCase {

    // ----------------------------------------------------------------------
    // PreferencesFactory interface
    // ----------------------------------------------------------------------

    public void testImplementedInterface() {
        // First get us a factory instance.
        DistributedMemoryPreferencesFactory factory = new DistributedMemoryPreferencesFactory();
        
        // Get the root node from that factory.
        Preferences systemRoot = factory.systemRoot();
        Preferences userRoot = factory.userRoot(); 
        
        assertNotNull( systemRoot );
        assertFalse( systemRoot.isUserNode() );

        assertNotNull( userRoot );
        assertTrue( userRoot.isUserNode() );
    }
}
