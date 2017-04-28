package sapotero.rxtest.db.requery.utils.validation;

import android.content.Context;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import timber.log.Timber;

public class Validation{
  @Inject RxSharedPreferences settings;

  private Set<String> selected_journals;
  private String TAG = this.getClass().getSimpleName();

  Validation() {
    EsdApplication.getDataComponent().inject(this);

    settings
      .getStringSet("settings_view_journals")
      .asObservable()
      .subscribeOn( Schedulers.newThread() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        set -> {
          selected_journals = set;
        },
        Timber::e
      );
  }

  public Boolean filterDocumentInSelectedJournals(Boolean finalWithFavorites, String type, String status){

    Boolean result = false;

    if (selected_journals != null){
      for (String index: selected_journals) {
        ArrayList<String> journal = new ArrayList<>();

        switch (index){
          case "1":
            journal.add("incoming_documents");
            break;
          case "2":
            journal.add("citizen_requests");
            break;
          case "4":
            journal.add("incoming_orders");
            break;
          case "7":
            journal.add("outgoing_documents");
            break;
          case "5":
            journal.add("orders");
            break;
          case "6":
            journal.add("orders_ddo");
            break;
          case "3":
            journal.add("sign");
            break;
        }

        if (journal.contains(type)){
          result = true;
        }

        if (Arrays.asList("signing", "approval").contains(status) && journal.contains("sign")){
          result = true;
        }

      }
    }

    if (finalWithFavorites){
      result = true;
    }

    return result;
  }

  public List<String> getSelectedJournals() {
    ArrayList<String> journal = new ArrayList<>();

    if (selected_journals != null){
      for (String index: selected_journals) {
        switch (index){
          case "1":
            journal.add("incoming_documents");
            break;
          case "2":
            journal.add("citizen_requests");
            break;
          case "4":
            journal.add("incoming_orders");
            break;
          case "7":
            journal.add("outgoing_documents");
            break;
          case "5":
            journal.add("orders");
            break;
          case "6":
            journal.add("orders_ddo");
            break;
        }

      }
    }

    Timber.tag(TAG).e("selected status: %s", journal);

    return journal;
  }
  public Boolean hasSigningAndApproval(){
    Boolean result = false;

    if (selected_journals != null) {
      if (selected_journals.contains("3")){
        result = true;
      }
    }

    return result;
  }
}
