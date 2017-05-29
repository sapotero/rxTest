package sapotero.rxtest.utils.memory.utils;

import java.util.ArrayList;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class IMDFilter {

  private final String TAG = this.getClass().getSimpleName();
  private final ArrayList<ConditionBuilder> conditions;

  private final ArrayList<String> statuses = new ArrayList<>();
  private final ArrayList<String> types    = new ArrayList<>();

  private Boolean isProcessed = false;
  private Boolean isFavorites = false;
  private Boolean isControl   = false;

  public IMDFilter(ArrayList<ConditionBuilder> conditions) {
    this.conditions = conditions;
    processConditions();
  }

  private void processConditions() {
    if (conditions != null) {
      for (ConditionBuilder condition : conditions ){
        if (condition.getField().getLeftOperand() == RDocumentEntity.FILTER){

          try {
            statuses.addAll(((ArrayList<String>) condition.getField().getRightOperand()));
          } catch (Exception e) {
            statuses.add( String.valueOf(condition.getField().getRightOperand()) );
          }

        }

        if (condition.getField().getLeftOperand() == RDocumentEntity.DOCUMENT_TYPE){
          types.add( String.valueOf(condition.getField().getRightOperand()) );
          Timber.tag(TAG).w("new index: %s", String.valueOf(condition.getField().getRightOperand()));
        }

        if (condition.getField().getLeftOperand() == RDocumentEntity.CONTROL){
          if ( condition.getField().getRightOperand().equals(true)  ){
            isControl = true;
          }
        }

        if ( condition.getField().getLeftOperand() == RDocumentEntity.PROCESSED ){
          if ( condition.getField().getRightOperand().equals(true)  ){
            isProcessed = true;
          }
        }

        if (condition.getField().getLeftOperand() == RDocumentEntity.FAVORITES){
          if ( condition.getField().getRightOperand().equals(true)  ){
            isFavorites = true;
          }
        }

      }
    }
  }

  public ArrayList<String> getStatuses() {
    return statuses;
  }
  public Boolean hasStatuses() {
    return statuses.size() > 0;
  }

  public ArrayList<String> getTypes() {
    return types;
  }
  public Boolean hasTypes() {
    return types.size() > 0;
  }


  public Boolean isProcessed(InMemoryDocument doc) {
    return isProcessed == doc.isProcessed();
  }

  public Boolean isFavorites(InMemoryDocument doc) {
    Boolean result = true;

    if ( isFavorites ){
      result = doc.getDocument().getFavorites() || doc.getDocument().isFromFavoritesFolder();
    }

    return result;
  }

  public Boolean isControl(InMemoryDocument doc) {
    Boolean result = true;

    if ( isControl ){
      result = doc.getDocument().getControl();
    }

    return result;
  }

  public Boolean byType(InMemoryDocument document) {
    return types.size() <= 0 || types.contains( document.getIndex() );
  }

  public Boolean byStatus(InMemoryDocument document) {
    return statuses.size() == 0 || statuses.contains( document.getFilter() );
  }

  public static Boolean isMd5Changed(String m1, String m2){
    return !m1.equals( m2 );
  }

  public static Integer bySortKey(InMemoryDocument imd1, InMemoryDocument imd2) {
    int result = -1;

    if (imd1.getDocument().getSortKey() != null && imd2.getDocument().getSortKey() != null) {
      result = imd1.getDocument().getSortKey().compareTo( imd2.getDocument().getSortKey() );
    }

    return result;
  }
}