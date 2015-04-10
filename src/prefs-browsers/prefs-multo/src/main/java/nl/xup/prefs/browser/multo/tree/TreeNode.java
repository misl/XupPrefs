package nl.xup.prefs.browser.multo.tree;

import java.util.prefs.Preferences;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class TreeNode extends Folder<Preferences> {

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  public TreeNode(String id, AbstractTree<Preferences> tree, IModel<Preferences> model)
  {
    super(id, tree, model);
  }

  // ----------------------------------------------------------------------
  // Overriding Folder
  // ----------------------------------------------------------------------

  @Override
  protected IModel<?> newLabelModel( IModel<Preferences> model ) {
    return new Model<String>(model.getObject().name());
  }
  
  @Override
  protected void onClick( AjaxRequestTarget target ) {
    System.out.println( this.getModelObject().absolutePath() ); 
    // TODO Auto-generated method stub
    super.onClick( target );
  }
}
