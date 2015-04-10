package ${groupId}.${artifactId};

import gnu.util.ReloadableClassLoader;

import java.util.prefs.PreferencesFactory;

import junit.framework.TestCase;

import ${groupId}.${artifactId}.MyPreferencesFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: 
 */
public class PreferencesTestBase extends TestCase {

	// ----------------------------------------------------------------------
	// Class fields
	// ----------------------------------------------------------------------
	private static final Logger logger = LoggerFactory.getLogger( PreferencesTestBase.class );
	private static final String PREFS_FACTORY_KEY = "java.util.prefs.PreferencesFactory";

	// ----------------------------------------------------------------------
	// Object fields
	// ----------------------------------------------------------------------

	// Remember the previous preferences factory, so it can be restored.
    private String originalPreferencesFactory;

    // Remeber the original class loader, so it can be restored.
    private ClassLoader originalClassLoader;
	
	// ----------------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------------

	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    protected PreferencesTestBase( String testName )
    {
        super( testName );
    }

    // ----------------------------------------------------------------------
    // TestCase overrides
    // ----------------------------------------------------------------------

    protected void setUp() throws Exception {
    	// We need a special class loader, otherwise it is not
    	// possible to unload the preference factory, re initialize
    	// it and start a fresh instance for the next test.
    	originalClassLoader = Thread.currentThread().getContextClassLoader();
    	Thread.currentThread().setContextClassLoader( ReloadableClassLoader.getClassLoader() );
    	
    	// Store the existing preference factory, so it can be
    	// restored later.
    	originalPreferencesFactory = System.getProperty( PREFS_FACTORY_KEY );

		// Make sure the correct preferences factory is used
		// for all tests
		System.setProperty( PREFS_FACTORY_KEY,  
				MyPreferencesFactory.class.getCanonicalName() );
   }

    protected void tearDown() throws Exception {
    	// Restore the original class loader and make sure the one
    	// we used for testing is unloaded (garbage collected).
    	Thread.currentThread().setContextClassLoader( originalClassLoader );
    	originalClassLoader = null;

    	// Reset the class loader so the classes will be reloaded next time.
    	ReloadableClassLoader.reset();

    	// Restore the old preference factory if there is one.
        if (originalPreferencesFactory != null)
        {
            System.setProperty( PREFS_FACTORY_KEY, originalPreferencesFactory);
        }
    }

    protected void reloadPreferencesFactory() {
    	// Get the name of the preferences factory to be reloaded.
		String preferencesFactoryName = System.getProperty( PREFS_FACTORY_KEY );
		
    	try {
			Class factoryClass = ReloadableClassLoader.getClassLoader().loadClass( preferencesFactoryName );
			PreferencesFactory factory = (PreferencesFactory) factoryClass.newInstance();
			
			// To be really sure access a method.
			factory.systemRoot();
    	} catch (Exception e) {
			logger.error( "Faild reloading " + preferencesFactoryName, e );
		}
    }
}
