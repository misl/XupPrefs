package nl.xup.prefs.composite;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 */
public class CompositePreferencesFactory implements PreferencesFactory {
	// ----------------------------------------------------------------------
    // Class fields
    // ----------------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(CompositePreferencesFactory.class);

    private static final LinkedHashSet< CompositeRule > _rules = new LinkedHashSet<CompositeRule>();
    
    // ----------------------------------------------------------------------
    // Attributes
    // ----------------------------------------------------------------------

    private CompositePreferences _systemRoot = null;
    private CompositePreferences _userRoot = null;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------
  
    public CompositePreferencesFactory()
    {
        super();
    }

    // ----------------------------------------------------------------------
    // Interface implementation - PreferencesFactory
    // ----------------------------------------------------------------------
  
    /* (non-Javadoc)
     * @see java.util.prefs.PreferencesFactory#systemRoot()
     */
    public Preferences systemRoot() {
        // Create system root if it has not yet been created.
        if ( _systemRoot == null ) {
            checkRules();
            
            _systemRoot = new CompositePreferences( this, false );
        }
        return _systemRoot;
    }

    /* (non-Javadoc)
     * @see java.util.prefs.PreferencesFactory#userRoot()
     */
    public Preferences userRoot() {
        // Create user root if it has not yet been created.
        if ( _userRoot == null ) {
            checkRules();
            
            _userRoot =  new CompositePreferences( this, true );
        }
        return _userRoot;
    }

    // ----------------------------------------------------------------------
    // Public interface
    // ----------------------------------------------------------------------

    /**
     * Enable cleaning things up completely.
     */
    void dispose() {
        // Get rid of all rules.
        _rules.clear();
        
        // get rid of root nodes.
        _systemRoot = null;
        _userRoot = null;
    }
    
    /**
     * Add a rule to the end of a set of composite rules. The order in 
     * which the rules get added determine the search order. So make sure
     * the last rule will give a positive match for any node name. This
     * can be accomplished by leaving the pattern blank (null).
     * @param rule the rule to add.
     * @throws IllegalArgumentException when the given rule is null.
     */
    public static void addRule( CompositeRule rule ) throws IllegalArgumentException {
        // Check for empty rules.
        if (rule == null) {
            throw new IllegalArgumentException( "Null rule not acceptable!" );
        }
        
        if ( logger.isDebugEnabled() ) {
            logger.info( "Adding CompositeRule: {} --> {}", 
                    rule.getPattern(),
                    rule.getFactory().getClass().getName() ); 
        }
        
        // Add it to our list of rules.
        _rules.add( rule );
    }

    // ----------------------------------------------------------------------
    // Package interface
    // ----------------------------------------------------------------------
    
    /**
     * Finds the composite rule that matches the given absolute node name.
     * The first rule whose pattern matches the absolute node name will be 
     * returned.
     * @param absoluteName    the absolute node name.
     * @return the composite rule to match the given node name.
     */
    CompositeRule findMatchingRule( String absoluteName ) {
        CompositeRule resultRule = null;
        
        // Loop though all rules and find the first match
        for( CompositeRule rule : _rules ) {
            // Does the node name match the pattern for this rule. If
            // no pattern available the rules matches by default.
            if ( rule.matches( absoluteName ) ) {
                // Houston, we have a match!
                resultRule = rule;

                if ( logger.isDebugEnabled() ) {
                    logger.debug( "Rule match: " + absoluteName + " == {} --> {}", 
                            rule.getPattern(),
                            rule.getFactory().getClass().getName() ); 
                }
                
                // We found a match, no need to keep on searching.
                break;
            }
        }
        
        if ( resultRule == null ) {
            // Hmm, none of the rules match.
            logger.error( "No matching rule found: {}", absoluteName );
            throw new RuntimeException( "CompositePreferencesFactory: No matching rule found for " + absoluteName );
        }
        
        return resultRule;
    }

    /**
     * TODO: doc it
     * @return
     */
    Set<PreferencesFactory> getLeafFactories() {
        Set<PreferencesFactory> factories = new HashSet<PreferencesFactory>( _rules.size() );
        
        // Loop over all rules and to gather the factories.
        for( CompositeRule rule : _rules ) {
            factories.add( rule.getFactory() );            
        }
        
        return factories;
    }

    // ----------------------------------------------------------------------
    // Internal/private methods.
    // ----------------------------------------------------------------------
    
    /**
     * To prevent a situation where a node matches no backing store at least
     * 1 rule is needed. Also the last rule should match everything, so it 
     * should not have a pattern. 
     */
    private void checkRules() {
        // Check number of rules
        if ( _rules.size() == 0 ) {
            throw new RuntimeException( "CompositePreferencesFactory: No rules specified." );
        }

        // Last rule needs to be without a pattern.
        CompositeRule lastRule = null;
        for( CompositeRule rule : _rules ) {
            lastRule = rule;
        }
        if ( lastRule.getPattern() != null ) {
            throw new RuntimeException( "CompositePreferencesFactory: Last rule should not have a pattern." );
        }
    }
}
