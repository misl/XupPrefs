/**
 * 
 */
package nl.xup.prefs.composite;

import java.util.prefs.PreferencesFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 */
public class CompositeRule {
    // ----------------------------------------------------------------------
    // Class fields
    // ----------------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(CompositeRule.class);

    // ----------------------------------------------------------------------
    // Attributes
    // ----------------------------------------------------------------------

    private Pattern _pattern = null;
    private PreferencesFactory _factory = null;
    private String _replacement = null;
    
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------
    
    public CompositeRule( Class<? extends PreferencesFactory> factoryClass ) {
        this( factoryClass, null );
    }

    public CompositeRule( Class<? extends PreferencesFactory> factoryClass, String namePattern ) {
        this( factoryClass, namePattern, null );
    }
    
    public CompositeRule( Class<? extends PreferencesFactory> factoryClass, String namePattern, String replacement ) {
        String errorMessage = null;
        Exception exception = null;
        try {
            _factory = (PreferencesFactory) factoryClass.newInstance();
        } catch ( IllegalAccessException iae ) {
            errorMessage = "Can't access " + factoryClass.getName();
            exception = iae;
        } catch ( InstantiationException ie ) {
            errorMessage = "Can't instantiate " + factoryClass.getName();
            exception = ie;
        }
        
        // Check if an error occured.
        if ( errorMessage != null ) {
            logger.error( errorMessage, exception );
            throw new RuntimeException( exception );
        }

        // Pass other arguments on to the method that knows best 
        // how to handle it.
        setPattern( namePattern );
        setReplacement(replacement);
    }

    public CompositeRule( PreferencesFactory factory ) {
        this( factory, null );
    }

    public CompositeRule( PreferencesFactory factory, String namePattern ) {
        this( factory, namePattern, null );
    }
    
    public CompositeRule( PreferencesFactory factory, String namePattern, String replacement ) {
        _factory = factory;

        // Pass other arguments on to the method that knows best 
        // how to handle it.
        setPattern( namePattern );
        setReplacement(replacement);
    }
    
    // ----------------------------------------------------------------------
    // Public Interface
    // ----------------------------------------------------------------------

    /**
     * @return the namePattern
     */
    public String getPattern() {
        String pattern = null;
        
        // Only if we have a pattern we can return it.
        if ( _pattern != null ){
            pattern = _pattern.pattern();
        }
            
        return pattern;
    }

    /**
     * @param namePattern the namePattern to set
     */
    public void setPattern(String namePattern) {
        // If a pattern was specified we can use it for regex.
        if ( namePattern != null ) {
            _pattern = Pattern.compile( namePattern );
        }
    }

    /**
     * @return the replacement
     */
    public String getReplacement() {
        return _replacement;
    }

    /**
     * @param replacement the replacement to set
     */
    public void setReplacement(String replacement) {
        _replacement = replacement;
    }

    /**
     * @return the preference factory
     */
    public PreferencesFactory getFactory() {
        return _factory;
    }
    
    /**
     * Checks whether the given name matches the rule pattern. 
     * @param absoluteName the name to match
     * @return true if the given name matches this rule, false otherwise
     */
    public boolean matches( String absoluteName ) {
        // If this rule has no pattern it will match by default.
        if ( _pattern == null ) {
            return true;
        }
        
        // check whether the given name matches the pattern.
        return _pattern.matcher( absoluteName ).matches();
    }
    
    /**
     * Uses both the pattern and the replacement to modify the
     * given absolute name. The name is only modified if and only
     * if there is both a pattern and a replacement, and the pattern
     * matches the given absolute name. 
     * @param absoluteName the name subject to the replacement
     * @return the result of the replacement or the given name
     *         unchanged. 
     */
    public String replace( String absoluteName ) {
        String result = absoluteName;
        
        // Only replace if we have a replacement string and a
        // pattern to replace.
        if ( _replacement == null || _pattern == null ) {
            // No replacement string or pattern, no replacement. 
            // So return the original name.
            return absoluteName;
        }
        
        // Replacement will only be done on a match.
        Matcher matcher = _pattern.matcher( absoluteName );
        if ( matcher.matches() ) {
            // We have a match now do the replacement.
            result = matcher.replaceFirst( _replacement );
        }
        
        return result;
    }
}
