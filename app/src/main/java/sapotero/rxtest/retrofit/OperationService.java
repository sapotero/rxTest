package sapotero.rxtest.retrofit;

import java.util.ArrayList;

import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.retrofit.models.documents.Documents;

public interface OperationService{

  @PUT("operations/check_control_label.json")
  Observable<Documents> addControlLabel(
    @Query("uids[]") String uid,
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("operation_data[comment]") String comment
  );

  @PUT("{operation_name}.json")
  Observable<OperationResult> execute(
    @Path("operation_name") String operation_name,
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("uids[]") ArrayList<String> uids,
    @Query("operation_data[comment]") String operation_data,
    @Query("status_code") String status_code
  );


}
