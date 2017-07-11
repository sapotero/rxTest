package sapotero.rxtest.retrofit.interactors;

import java.util.ArrayList;

import retrofit2.Retrofit;
import rx.Observable;
import sapotero.rxtest.retrofit.Api.AuthService;
import sapotero.rxtest.retrofit.models.AuthSignToken;
import sapotero.rxtest.retrofit.models.v2.v2UserOshs;


public class ApiInteractor {

  private AuthService service;

  public ApiInteractor(Retrofit retrofit) {
    this.service = retrofit.create(AuthService.class);
  }

  public Observable<AuthSignToken> getUser() {
    return service.getAuth( "androidr", "123456" );
  }

  public Observable<ArrayList<v2UserOshs>> getUserInfo(String token) {
    return service.getUserInfoV2( "androidr", token );
  }



}
