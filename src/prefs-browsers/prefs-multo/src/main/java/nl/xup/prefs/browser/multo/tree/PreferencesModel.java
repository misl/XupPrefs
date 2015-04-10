package nl.xup.prefs.browser.multo.tree;

import java.util.prefs.Preferences;

import org.apache.wicket.model.LoadableDetachableModel;

public class PreferencesModel extends LoadableDetachableModel<Preferences>{

  // ----------------------------------------------------------------------
  // Class attributes
  // ----------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ----------------------------------------------------------------------
  // Object attributes
  // ----------------------------------------------------------------------

  private final String id;
  private final boolean userRoot;

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  public PreferencesModel( Preferences node ) {
    super(node);
    
    id = node.absolutePath();
    userRoot = node.isUserNode();
  }
  
  // ----------------------------------------------------------------------
  // Overriding LoadableDetachableModel
  // ----------------------------------------------------------------------

  @Override
  protected Preferences load() {
    Preferences rootNode;
    if ( userRoot ) {
      rootNode = Preferences.systemRoot();
    } else {
      rootNode = Preferences.userRoot();
    }
    return rootNode.node( id );
  }

  // ----------------------------------------------------------------------
  // Overriding Object
  // ----------------------------------------------------------------------
  
  @Override
  public boolean equals( Object obj ) {
    if ( obj instanceof PreferencesModel ) {
      PreferencesModel node = PreferencesModel.class.cast(obj);
      return node.userRoot == userRoot && node.id.equals( id );
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return (Boolean.toString( userRoot ) + id).hashCode();
  }
}
