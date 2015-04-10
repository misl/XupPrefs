/**
 * 
 */
package nl.xup.prefs.composite;

import java.util.Set;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import junit.framework.TestCase;
import nl.xup.prefs.composite.CompositePreferencesFactory;
import nl.xup.prefs.composite.CompositeRule;
import nl.xup.prefs.memory.MemoryPreferencesFactory;

/**
 * @author Minto van der Sluis
 */
public class CompositePreferencesFactoryTest extends TestCase {

    // ----------------------------------------------------------------------
    // PreferencesFactory interface
    // ----------------------------------------------------------------------

    public void testMisconfiguredFactory() {
        // First get us a factory instance.
        CompositePreferencesFactory factory = new CompositePreferencesFactory();

    	// Expect an exception since we defined no rules at all.
        try {
            factory.systemRoot();
        	assertFalse( true );
        } catch ( RuntimeException re ) {
        	assertTrue( true );
        }
        try {
            factory.userRoot();
        	assertFalse( true );
        } catch ( RuntimeException re ) {
        	assertTrue( true );
        }

        // We need at least 1 rule.
        CompositePreferencesFactory .addRule( 
        			new CompositeRule( MemoryPreferencesFactory.class, 
        			"aap" ) );
        
    	// Expect an exception since the last rule does have a pattern.
        try {
            factory.systemRoot();
        	assertFalse( true );
        } catch ( RuntimeException re ) {
        	assertTrue( true );
        }
        try {
            factory.userRoot();
        	assertFalse( true );
        } catch ( RuntimeException re ) {
        	assertTrue( true );
        }
        
        // Clean up the factory.
        factory.dispose();
    }
	
    public void testHappyFlowFactory() {
        // We need at least 1 rule.
        CompositePreferencesFactory.addRule( new CompositeRule( MemoryPreferencesFactory.class ) );
        
        // Now get us a factory instance.
        CompositePreferencesFactory factory = new CompositePreferencesFactory();
        
        // Get the root node from that factory.
        Preferences systemRoot = factory.systemRoot();
        Preferences userRoot = factory.userRoot(); 
        
        assertNotNull( systemRoot );
        assertFalse( systemRoot.isUserNode() );

        assertNotNull( userRoot );
        assertTrue( userRoot.isUserNode() );
        
        // Clean up the factory.
        factory.dispose();
    }

    public void testNoMatchingRule() {
    	// Normally we should not be able to get this error since
    	// the last rule should not have a pattern so it functions
    	// as a safetynet to match all nodes. To be able to test
    	// we change the rule after constructing the factory.
    	CompositeRule rule = new CompositeRule( MemoryPreferencesFactory.class );
        CompositePreferencesFactory.addRule( rule );
        
        // Now get us a factory instance.
        CompositePreferencesFactory factory = new CompositePreferencesFactory();
        
        // Get the root node from that factory.
        Preferences systemRoot = factory.systemRoot();
        Preferences userRoot = factory.userRoot(); 

        // Change the rule to enforce the exception.
        rule.setPattern( "test" );
        
        // Get the root node from that factory.
        try {
            systemRoot.node( "newNode" );
            assertFalse( true );
        } catch ( RuntimeException re ) {
        	assertTrue( true );
        }
	    try {
	        userRoot.node( "newNode" );
	        assertFalse( true );
	    } catch ( RuntimeException re ) {
	    	assertTrue( true );
	    }

        // Clean up the factory.
        factory.dispose();
    }

    // ----------------------------------------------------------------------
    // Public interface
    // ----------------------------------------------------------------------

	public void testNullRule() {
        try {
            // Null rule is not allowed.
            CompositePreferencesFactory.addRule( null );
        	assertFalse( true );
        } catch ( IllegalArgumentException iae ) {
        	assertTrue( true );
        }
	}

    // ----------------------------------------------------------------------
    // Package interface
    // ----------------------------------------------------------------------


	public void testLeafFactories() {
		// Add 2 rules with different factory instances.
		PreferencesFactory factory1 = new MemoryPreferencesFactory();
		PreferencesFactory factory2 = new MemoryPreferencesFactory();
        CompositePreferencesFactory.addRule( new CompositeRule( factory1 ) );
        CompositePreferencesFactory.addRule( new CompositeRule( factory2 ) );
        
        // Now get us a composite factory instance.
        CompositePreferencesFactory factory = new CompositePreferencesFactory();

        // Getting the leaf factories should return 2 factories
        Set<PreferencesFactory> factories = factory.getLeafFactories();
        assertEquals( 2, factories.size() );
        assertTrue( factories.contains( factory1 ) );
        assertTrue( factories.contains( factory2 ) );
        
        factory.dispose();
        
        // Now the same test but adding the same factory twice.
        CompositePreferencesFactory.addRule( new CompositeRule( factory1 ) );
        CompositePreferencesFactory.addRule( new CompositeRule( factory1 ) );

        // Now get us a new composite factory instance.
        factory = new CompositePreferencesFactory();

        // Getting the leaf factories should return 1 factory
        factories = factory.getLeafFactories();
        assertEquals( 1, factories.size() );
        assertTrue( factories.contains( factory1 ) );
        assertFalse( factories.contains( factory2 ) );
        
        factory.dispose();
	}
}
