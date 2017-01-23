package sapotero.rxtest.views.adapters.models;

import android.content.Context;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.Scalar;
import io.requery.query.WhereAndOr;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocument;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.fields.MainMenuItem;

public class DocumentTypeItem {
  @Inject SingleEntityStore<Persistable> dataStore;

  private final MainMenuItem mainMenuItem;
  private final String user;


  public DocumentTypeItem(Context context, MainMenuItem mainMenuItem, String user) {
    super();
    this.mainMenuItem = mainMenuItem;
    this.user = user;

    EsdApplication.getComponent( context ).inject(this);
  }

  public String getName() {

    if (mainMenuItem.getIndex() == 0){
      Integer total = dataStore
        .count(RDocumentEntity.class)
        .where( RDocumentEntity.FAVORITES.ne( true ) )
        .and( RDocumentEntity.PROCESSED.ne( true ) )
//        .and( RDocumentEntity.USER.eq( getUserName() ) )
        .get()
        .value();

      Integer projects = dataStore
        .count(RDocumentEntity.class)
        .where( RDocumentEntity.FILTER.eq(Fields.Status.APPROVAL.getValue() )   )
//        .and( RDocumentEntity.USER.eq( getUserName() ) )
        .or( RDocumentEntity.FILTER.eq(Fields.Status.SIGNING.getValue() )   )
        .get()
        .value();

      return String.format( mainMenuItem.getName(), total, projects);
    } else {
      int count = 0;

      WhereAndOr<Scalar<Integer>> query =
        dataStore
          .count(RDocument.class)
          .where(RDocumentEntity.ID.ne(0));
//          .and( RDocumentEntity.USER.eq( user ) );

      if ( mainMenuItem.getCountConditions().length > 0 ){

        for (ConditionBuilder condition : mainMenuItem.getCountConditions() ){
          switch ( condition.getCondition() ){
            case AND:
              query = query.and( condition.getField() );
              break;
            case OR:
              query = query.or( condition.getField() );
              break;
            default:
              break;
          }
        }
      }

      count = query.get().value();

      return String.format( mainMenuItem.getName(), count);
    }

  }

  public MainMenuItem getMainMenuItem(){
    return mainMenuItem;
  }

}
