package sapotero.rxtest.views.activities;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.documents.Signer;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.utils.TestRecyclerViewFragment;

import static android.support.test.espresso.Espresso.onView;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class RecyclerViewTest {

  private TestActivity activity;
  private Instrumentation instrumentation;

  private TestRecyclerViewFragment fragment;
  private RecyclerView recyclerView;
  private DocumentsAdapter adapter;

  private InMemoryDocument dummyDoc;

  @Rule
  public ActivityTestRule<TestActivity> activityTestRule = new ActivityTestRule<>(TestActivity.class);

  @Before
  public void init() {
    instrumentation = InstrumentationRegistry.getInstrumentation();

    fragment = new TestRecyclerViewFragment();

    activity = activityTestRule.getActivity();
    activity.runOnUiThread(() -> activity.addFragment(fragment));
    instrumentation.waitForIdleSync();

    recyclerView = fragment.getRecyclerView();
    adapter = fragment.getAdapter();
  }

  @Test
  public void recyclerViewIsPresent() {
    View viewById = activity.findViewById(R.id.testactivity_recycler_view);
    assertThat(viewById, notNullValue());
    assertThat(viewById, instanceOf(RecyclerView.class));
  }

  @Test
  public void loadingReady() {
    dummyDoc = generateDocument();

    activity.runOnUiThread(() -> {
      adapter.addItem(dummyDoc);
    });
    instrumentation.waitForIdleSync();

    waitUI();
  }

  private InMemoryDocument generateDocument() {
    InMemoryDocument doc = new InMemoryDocument();

    doc.setUid( "025e937f2dffce50fee6fcd69cfe7daf48b333ddf29fe634d89cae907d7c42c409" );
    doc.setMd5( "4ea3893b94f0fc1ad4935a760eee5928" );
    doc.setAsReady();

    Document document = new Document();
    document.setUid( "025e937f2dffce50fee6fcd69cfe7daf48b333ddf29fe634d89cae907d7c42c409" );
    document.setMd5( "4ea3893b94f0fc1ad4935a760eee5928" );
    document.setSortKey( 1500539843 );
    document.setTitle( "1640 от 20.07.2017" );
    document.setRegistrationNumber( "1640" );
    document.setRegistrationDate( "20.07.2017" );
    document.setShortDescription( "SDfkljh23ir dkjsfhkds hfs dfkjdsh" );
    document.setComment( "dskh oi23u fkjdhsf2893 ksdjhf sddsfds" );
    document.setExternalDocumentNumber( "42833" );
    document.setReceiptDate( "20.07.2017" );
    document.setViewed( true );
    document.setOrganization( "ЦВСНП МВД по Республике Башкортостан" );

    Signer dummySigner = new Signer();
    dummySigner.setId( "58f88dfc776b000026370001" );
    dummySigner.setName( "Иванов И.И. (Старший эксперт)" );
    dummySigner.setOrganisation( "ЦВСНП МВД по Республике Башкортостан" );
    dummySigner.setType( "mvd_person" );

    document.setSigner( dummySigner );
    document.setChanged( false );

    doc.setDocument( document );

    return doc;
  }

  private void waitUI() {
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
