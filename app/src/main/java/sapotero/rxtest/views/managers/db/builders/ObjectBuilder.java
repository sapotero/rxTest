package sapotero.rxtest.views.managers.db.builders;

import android.content.Context;

import sapotero.rxtest.retrofit.models.documents.Document;

/**
 * Created by sapotero on 28.11.16.
 */

public class ObjectBuilder {
  private Document document;

  public ObjectBuilder(Context context) {

  }

  public Document get(String uid) {
    return document;
  }
}
