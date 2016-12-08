package sapotero.rxtest.db.requery.query;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.query.WhereAndOr;
import io.requery.rx.SingleEntityStore;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class DBQueryBuilder {

  @Inject SingleEntityStore<Persistable> dataStore;

  private final String TAG = this.getClass().getSimpleName();

  private final Context    context;
  private DocumentsAdapter adapter;
  private ArrayList<ConditionBuilder> conditions;
  private ProgressBar progressBar;
  private TextView documents_empty_list;
  private Subscription loadFromDbQuery;

  public DBQueryBuilder(Context context) {
    this.context = context;
    EsdApplication.getComponent(context).inject(this);
  }

  public DBQueryBuilder withAdapter(DocumentsAdapter rAdapter) {
    this.adapter = rAdapter;
    return this;
  }

  public DBQueryBuilder withEmptyView(TextView documents_empty_list) {
    this.documents_empty_list = documents_empty_list;
    return this;
  }

  public DBQueryBuilder withProgressBar(ProgressBar progress) {
    this.progressBar = progress;
    return this;
  }

  public void execute(){

    documents_empty_list.setVisibility(View.GONE);
    progressBar.setVisibility(ProgressBar.VISIBLE);


    if (loadFromDbQuery != null) {
      loadFromDbQuery.unsubscribe();
    }

    WhereAndOr<Result<RDocumentEntity>> query = dataStore.select(RDocumentEntity.class).where(RDocumentEntity.ID.ne(0));

    if ( conditions.size() > 0 ){

      for (ConditionBuilder condition : conditions ){
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

    loadFromDbQuery = query.get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .toList()
      .subscribe(docs -> {
        Timber.tag("loadFromDbQuery").e("docs: %s", docs.size() );
        addToAdapterList(docs);
      });
  }

  public void executeWithConditions(ArrayList<ConditionBuilder> conditions) {
    this.conditions = conditions;
    execute();
  }

  private void addToAdapterList(List<RDocumentEntity> docs) {
    if (docs.size() > 0) {

      ArrayList<Document> list_dosc = new ArrayList<Document>();
      for (int i = 0; i < docs.size(); i++) {
        RDocumentEntity doc = docs.get(i);
        Timber.tag(TAG).v("addToAdapter ++ " + doc.getTitle());

        Document document = new Document();
        document.setChanged( doc.isChanged() );
        document.setStatusCode( doc.getFilter() );
        document.setUid(doc.getUid());
        document.setMd5(doc.getMd5());
        document.setControl(doc.isControl());
        document.setFavorites(doc.isFavorites());
        document.setSortKey(doc.getSortKey());
        document.setTitle(doc.getTitle());
        document.setRegistrationNumber(doc.getRegistrationNumber());
        document.setRegistrationDate(doc.getRegistrationDate());
        document.setUrgency(doc.getUrgency());
        document.setShortDescription(doc.getShortDescription());
        document.setComment(doc.getComment());
        document.setExternalDocumentNumber(doc.getExternalDocumentNumber());
        document.setReceiptDate(doc.getReceiptDate());
        document.setOrganization(doc.getOrganization());

        list_dosc.add(document);

      }
      adapter.setDocuments(list_dosc);
      progressBar.setVisibility(ProgressBar.GONE);
    }


  }
}
