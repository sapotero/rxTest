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
import sapotero.rxtest.views.menu.fields.Item;

public class DocumentTypeItem {
  @Inject SingleEntityStore<Persistable> dataStore;

  private final Item item;


  public DocumentTypeItem(Context context, Item item) {
    super();
    this.item = item;

    EsdApplication.getComponent( context ).inject(this);
  }

  public String getName() {

    if (item.getIndex() == 0){
      Integer total = dataStore
        .count(RDocumentEntity.class)
        .get()
        .value();

      Integer projects = dataStore
        .count(RDocumentEntity.class)
        .where( RDocumentEntity.FILTER.eq(Fields.Status.APPROVAL.getValue() )   )
        .or( RDocumentEntity.FILTER.eq(Fields.Status.SIGNING.getValue() )   )
        .get()
        .value();

      return String.format( item.getName(), total, projects);
    } else {
      int count = 0;

      WhereAndOr<Scalar<Integer>> query = dataStore.count(RDocument.class).where(RDocumentEntity.ID.ne(0));

      if ( item.getCountConditions().length > 0 ){

        for (ConditionBuilder condition : item.getCountConditions() ){
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

      return String.format( item.getName(), count);
    }

  }

  public Item getItem(){
    return item;
  }

}
