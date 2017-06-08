package sapotero.rxtest.utils.memory.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class Counter {
  private final String TAG = this.getClass().getSimpleName();
  private final HashMap<Status, Map<Document, Integer>> store;

  public enum Document{
    INCOMING_DOCUMENTS,
    OUTGOING_DOCUMENTS,
    ORDERS,
    INCOMING_ORDERS,
    CITIZEN_REQUESTS,
    SECRET_INCOMING_DOCUMENTS,
    SECRET_OUTGOING_DOCUMENTS,
    SECRET_ORDERS,
    SECRET_INCOMING_ORDERS,
    ORDERS_DDO,
    ATTORNEY_LETTERS,
    PROJECTS,
    PROCESSED;

    public static Document getType(String string){
      Document type = null;

      if (string != null) {
        try {
          type = Document.valueOf( string.toUpperCase() );
        } catch (IllegalArgumentException | NullPointerException error) {
          Timber.e(error);
        }
      }

      return type;
    }
  }

  public enum Status{
    APPROVAL,
    SIGNING,
    PRIMARY_CONSIDERATION,
    SENT_TO_THE_REPORT,
    UNDEFINED,
    PROCESSED;

    public static Status getStatus(String string){
      Status status = null;

      try {
        status = Status.valueOf( string.toUpperCase() );
      } catch (IllegalArgumentException | NullPointerException error) {
        Timber.e(error);
      }

      return status;
    }
  }

  public Counter() {
    this.store = new HashMap<>();
  }

  public HashMap<Status, Map<Document, Integer>> getData(){
    return store;
  }

  public String put(InMemoryDocument doc){
    Status status = Status.getStatus( doc.getFilter() );
//    if ( doc.getDocument().isProcessed() || doc.isProcessed() ){
//      status = Status.PROCESSED;
//    }

    if (status != null) {

      if ( store.containsKey(status) ){

        HashMap<Document, Integer> type = (HashMap<Document, Integer>) store.get(status);

        Document index = Document.getType(doc.getIndex());

        if ( doc.getIndex() == null ){
          index = Document.PROJECTS;
        }

        if ( doc.getDocument().isProcessed() || doc.isProcessed() ){
          index = Document.PROCESSED;
        }



        if (index != null) {
          if ( type.containsKey(index) ){
            type.put(index, type.get(index)+1);
          } else {
            type.put(index, 1);
          }
        }




      } else {
        // add new status
        Document type = Document.getType(doc.getIndex());
        if ( doc.getIndex() == null ){
          type = Document.PROJECTS;
        }

        if ( doc.getDocument().isProcessed() ){
          type = Document.PROCESSED;
        }

        if (type != null) {
          Map<Document, Integer> entry = new HashMap<>();
          entry.put(type, 1);
          store.put( status, entry );
        }

      }

    }

    return doc.getUid();
  }

  public void recreate(HashMap<String, InMemoryDocument> documents) {

    if (documents.values().size() > 0) {

      this.store.clear();
      for (InMemoryDocument doc : documents.values() ) {
        put(doc);
      }

      getInfo();
    }
  }

  private void getInfo(){
    Gson builder = new GsonBuilder().setPrettyPrinting().create();
    Timber.tag(TAG).w( builder.toJson(store) );
  }

}
