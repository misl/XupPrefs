package nl.xup.prefs.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import nl.xup.prefs.memory.MemoryPreferences;
import nl.xup.prefs.memory.MemoryPreferencesFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferencesStartup {

  private static final long serialVersionUID = 1L;

  // ----------------------------------------------------------------------
  // Constants
  // ----------------------------------------------------------------------

  private static final String PREFS_FACTORY_KEY = "java.util.prefs.PreferencesFactory";
  private static final String PREFS_FILE = "/prefs.xml";
  private static Logger logger = LoggerFactory.getLogger( PreferencesStartup.class );

  // ----------------------------------------------------------------------
  // Override HttpServlet
  // ----------------------------------------------------------------------

  /**
   * @see javax.servlet.GenericServlet#init()
   */
  public static void initPrefs() {
    try {
      // Zorg ervoor dat de juiste preferences implementatie gebruikt
      // wordt.
      if( !(Preferences.systemRoot() instanceof MemoryPreferences) ) {
        logger.error( "StartupServlet: Verkeerde preferences factory zet " + PREFS_FACTORY_KEY );
      }

      // In memory preferences, echter deze moeten we wel van de juiste
      // preferences voorzien. Hiertoe lezen we eerst alle preferences in.
      readPreferences();
    } catch( Exception e ) {
      logger.error( "Fout bij StartupServlet.init()", e );
      throw new RuntimeException( "Fout bij StartupServlet.init()", e );
    }
  }

  // ----------------------------------------------------------------------
  // Private methods
  // ----------------------------------------------------------------------

  private static void readPreferences() {
    // Haal het bestand waar de preferences in staan op als stream.
    InputStream prefsStream = PreferencesStartup.class.getResourceAsStream( PREFS_FILE );

    try {
      // De preferences in de stream inlezen.
      Preferences.importPreferences( prefsStream );
      logger.info( "Preferences ingelezen uit: " + PREFS_FILE );
    } catch( IOException ioex ) {
      logger.error( "StartupServlet: Inlezen preferences mislukt!", ioex );
    } catch( InvalidPreferencesFormatException ipex ) {
      logger.error( "StartupServlet: Foutief preferences formaat!", ipex );
    }
  }
}
