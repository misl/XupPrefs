/**
 * 
 */
package nl.xup.prefs.composite;

import junit.framework.TestCase;
import nl.xup.prefs.composite.helpers.DummyPreferencesFactory;
import nl.xup.prefs.memory.MemoryPreferencesFactory;

/**
 *
 */
public class CompositeRuleTest extends TestCase {

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

	public void testConstructors() {
		// Create rule with only the factory type.
		CompositeRule rule = new CompositeRule( MemoryPreferencesFactory.class );
		assertNotNull( rule );
		assertTrue( rule.getFactory() instanceof MemoryPreferencesFactory );
		assertNull( rule.getPattern() );
		assertNull( rule.getReplacement() );

		// Create rule with a factory instance.
		MemoryPreferencesFactory factory = new MemoryPreferencesFactory();
		CompositeRule rule2 = new CompositeRule( factory );
		assertNotNull( rule2 );
		assertEquals( factory, rule2.getFactory() );
		assertNull( rule2.getPattern() );
		assertNull( rule2.getReplacement() );
		
		// Try create a rule with a factory that can not be instantiated.
        try {
    		new CompositeRule( DummyPreferencesFactory.class );
        	assertFalse( true );
        } catch ( RuntimeException re ) {
        	assertTrue( true );
        }
	}
	
	// ----------------------------------------------------------------------
	// Setters and Getters
	// ----------------------------------------------------------------------

	public void testGettersSetters() {
		// Create an instance to test on.
		CompositeRule rule = new CompositeRule( MemoryPreferencesFactory.class );
		
		// factory is already tested in testConstructors()
		
		// pattern
		String pattern = "aap";
		rule.setPattern( pattern );
		assertEquals( pattern, rule.getPattern() );
		
		// replacement
		String replacement = "noot";
		rule.setReplacement( replacement );
		assertEquals( replacement, rule.getReplacement() );
	}

	// ----------------------------------------------------------------------
	// Public interface methods
	// ----------------------------------------------------------------------

	public void testMatches() {
		// Create an instance to test on.
		CompositeRule rule = new CompositeRule( MemoryPreferencesFactory.class, "aap" );
		
		// pattern
		assertTrue( rule.matches( "aap" ) );
		assertFalse( rule.matches( "noot" ) );
	}

	public void testReplace() {
		// Create an instance to test on.
		CompositeRule rule = new CompositeRule( MemoryPreferencesFactory.class, "(aap)", "noot" );
		
		// replace
		assertEquals( "noot", rule.replace( "aap" ) );
		assertEquals( "mies", rule.replace( "mies" ) );

		// Create an instance to test on.
		rule = new CompositeRule( MemoryPreferencesFactory.class, "aap" );
		assertEquals( "aap", rule.replace( "aap" ) );
	}
}
