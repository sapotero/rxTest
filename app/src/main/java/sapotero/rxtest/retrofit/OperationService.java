package sapotero.rxtest.retrofit;

import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.documents.Documents;

public interface OperationService{

  @PUT("operations/check_control_label.json")
  Observable<Documents> addControlLabel(
    @Query("uids[]") String uid,
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("operation_data[comment]") String comment
  );
}
