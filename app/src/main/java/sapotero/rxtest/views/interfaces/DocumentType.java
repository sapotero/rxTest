package sapotero.rxtest.views.interfaces;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

import sapotero.rxtest.R;

public class DocumentType {

  private final ArrayList<String> names;
  private final ArrayList<String> values;
  private Integer index;

  private DocumentType instance;
  private final Context context;

  DocumentType(Context ctx) {
    this.context = ctx;

    this.names  = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.JOURNAL_TYPES)));
    this.values = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.JOURNAL_TYPES_VALUE)));
  }
  public String getByUID(String uid) {
    String result = null;

    if ( values != null && values.size() > 0 && uid != null){

      for (String value : values) {
        if (value.startsWith(uid.substring(0, 1))) {
          result = value;
          break;
        }
      }

    }

    return result;
  }

}
