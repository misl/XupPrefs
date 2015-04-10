package nl.xup.prefs.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import nl.xup.prefs.memory.MemoryPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Minto van der Sluis
 */
public class PreferencesTestHelper {
  // ----------------------------------------------------------------------
  // Class fields
  // ----------------------------------------------------------------------

  private static final Logger logger = LoggerFactory.getLogger( PreferencesTestHelper.class );
	  
  // ----------------------------------------------------------------------
  // Private methods
  // ----------------------------------------------------------------------

  /**
   * TODO
   */
  public static void clearPreferences( Object caller ) {
    Preferences node = Preferences.systemRoot();
    
    // Alleen preferences verwijderen indien we gebruik maken van 
    // MemoryPrefs. Dit om te voorkomen dat per ongeluk andere
    // preferences worden verwijderd.
    if ( node instanceof MemoryPreferences ) {
      try {
        node.clear();
        
        // Verwijder alle childnodes.
        String[] nodeNamen = node.childrenNames();
        for(int i=0; i < nodeNamen.length; i++ ) {
          Preferences childNode = node.node( nodeNamen[ i ] );
          childNode.removeNode();
          childNode.flush();
        }
        node.flush();
        
        // Om zeker te zijn dat alle listeners zijn uitgevoerd (aparte 
        // thread) eventjes wachten.
        synchronized (caller) {
          caller.wait( 100 );
        }
      } catch(InterruptedException e) {
        // ignore
      } catch(Exception e) {
        logger.error( "Clearing memory preferences failed!", e );
      }
    } else {
      logger.error( "Verkeerde preferences factory ({}), geen clear uitgevoerd.", 
          node.getClass() );
    }
  }
  
  /**
   * TODO
   * @param resource
   */
  public static void laadPreferences( Object caller, String resource ) {
    // Eerst eventuele oude preferences verwijderen.
    clearPreferences( caller );
    
    // Het resource bestand met de preferences laden.
    InputStream stream = caller.getClass().getClassLoader().getResourceAsStream( resource );

    // Nu de preferences netjes inlezen.
    try {
      // De preferences in de stream inlezen.
      Preferences.importPreferences( stream );
      logger.info( "Preferences ingelezen uit: " + resource );
    } catch ( IOException ioex ) {
      logger.error( "FlowTest: Inlezen preferences uit '" + resource + "' mislukt!", ioex );
    } catch ( InvalidPreferencesFormatException ipex ) {
      logger.error( "FlowTest: Foutief preferences formaat in '" + resource + "'!", ipex );
    }
  }

  public static void laadPreferences( Object caller, String resource, boolean clear ) {
    if ( clear ) {
      // Eerst eventuele oude preferences verwijderen.
      clearPreferences( caller );
    }

    // Het resource bestand met de preferences laden.
    InputStream stream = caller.getClass().getClassLoader().getResourceAsStream( resource );

    // Nu de preferences netjes inlezen.
    try {
      // De preferences in de stream inlezen.
      Preferences.importPreferences( stream );
      logger.info( "Preferences ingelezen uit: " + resource );
    } catch ( IOException ioex ) {
      logger.error( "FlowTest: Inlezen preferences uit '" + resource + "' mislukt!", ioex );
    } catch ( InvalidPreferencesFormatException ipex ) {
      logger.error( "FlowTest: Foutief preferences formaat in '" + resource + "'!", ipex );
    }
  }
}
