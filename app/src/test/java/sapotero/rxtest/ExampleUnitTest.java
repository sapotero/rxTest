package sapotero.rxtest;

import com.google.gson.Gson;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.observers.TestSubscriber;
import sapotero.rxtest.retrofit.interactors.ApiInteractor;
import sapotero.rxtest.retrofit.models.AuthSignToken;
import sapotero.rxtest.retrofit.models.v2.v2UserOshs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.verify;

public class ExampleUnitTest {

  @Mock
  List<String> mockedList;

  @Mock
  private AuthSignToken token;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
//    when(mCache.get(anyString())).thenReturn(null);

  }
  @Test
  public void listTest() throws Exception {
    mockedList.add("true");
    mockedList.clear();

    verify(mockedList).add("true");
    verify(mockedList).clear();
  }

  @Test
  public void shouldLoadTwoUsers() throws Exception {

    List<String> letters = Arrays.asList("A", "B", "C", "D", "E");
    TestSubscriber<String> subscriber = new TestSubscriber<>();

    Observable<String> observable = Observable
      .from(letters)
      .zipWith(
        Observable.range(1, Integer.MAX_VALUE),
        ((string, index) -> index + "-" + string));

    observable.subscribe(subscriber);

    subscriber.assertCompleted();
    subscriber.assertNoErrors();
    subscriber.assertValueCount(5);
    assertThat(
      subscriber.getOnNextEvents(),
      hasItems("1-A", "2-B", "3-C", "4-D", "5-E")
    );

  }



  @Test
  public void mockService() {

    AuthSignToken authSignToken = new AuthSignToken();

    MockWebServer mockService = new MockWebServer();
    mockService.enqueue(new MockResponse().setBody(new Gson().toJson(authSignToken)));

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl( "http://google.com/" )
      .build();

    TestSubscriber<AuthSignToken> subscriber = new TestSubscriber<>();
    ApiInteractor apiInteractor = new ApiInteractor(retrofit);
    apiInteractor.getUser().subscribe(subscriber);


    subscriber.assertError( HttpException.class );
//    subscriber.assertCompleted();
  }

  @Test
  public void callApiTest() {

    AuthSignToken token = new AuthSignToken();
    token.setAuthToken("fakeToken");

    MockWebServer mockWebServer = new MockWebServer();
    mockWebServer.enqueue(new MockResponse().setBody(new Gson().toJson(token)));

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl( "http://mobile.sed.a-soft.org/" )
      .build();

    TestSubscriber<AuthSignToken> subscriber = new TestSubscriber<>();
    ApiInteractor apiInteractor = new ApiInteractor(retrofit);
    apiInteractor.getUser().subscribe(subscriber);

    List<AuthSignToken> tokens = subscriber.getOnNextEvents();


    String real_token = "";

    // получили 1 токен
    assertEquals(1, tokens.size());
    assertEquals(tokens.get(0).getLogin(), null);

    real_token = tokens.get(0).getAuthToken();

    subscriber.assertNoErrors();
    subscriber.assertCompleted();
    subscriber.unsubscribe();

    TestSubscriber<ArrayList<v2UserOshs>> infoSubscriber = new TestSubscriber<>();
    apiInteractor.getUserInfo(real_token).subscribe(infoSubscriber);

    List<ArrayList<v2UserOshs>> user_info = infoSubscriber.getOnNextEvents();

    infoSubscriber.assertNoErrors();
    infoSubscriber.assertCompleted();
    infoSubscriber.unsubscribe();

    // получили 1 токен
    assertEquals(1, user_info.size());

    v2UserOshs api_user  = user_info.get(0).get(0);
    v2UserOshs mock_user = new v2UserOshs();
    mock_user.setId("596490f596dd00003b000001");
    mock_user.setIsOrganization(false);
    mock_user.setIsGroup(false);
    mock_user.setName("Android_руководитель A.А.");
    mock_user.setOrganization("ГУ Android Test");
    mock_user.setPosition("Министр");
    mock_user.setLastName("Android_руководитель");
    mock_user.setFirstName("Autotest");
    mock_user.setMiddleName("А");
    mock_user.setGender("Мужской");
    mock_user.setImage(null);

    assertEquals(
      new Gson().toJson(api_user),
      new Gson().toJson(mock_user)
    );


  }


}