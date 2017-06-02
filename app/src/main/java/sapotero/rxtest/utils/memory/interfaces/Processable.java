package sapotero.rxtest.utils.memory.interfaces;

import java.util.List;

import rx.Observable;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.models.documents.Document;

public interface Processable {

  void process( Observable<List<String>> api, String filter, String index );
  void process( Observable<List<String>> api, String filter );

  void process( RDocumentEntity doc );
  void process( RDocumentEntity doc, String filter, String index );
  void process( Document doc );
  void process( Document doc, String filter, String index  );
}
