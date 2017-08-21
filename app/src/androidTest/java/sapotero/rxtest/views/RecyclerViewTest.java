package sapotero.rxtest.views;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.documents.Signer;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.activities.ActivityForTest;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.utils.TestRecyclerViewFragment;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isFocusable;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static sapotero.rxtest.views.utils.TestUtils.withTextColor;

@RunWith(AndroidJUnit4.class)
public class RecyclerViewTest {

  private ActivityForTest activity;
  private Instrumentation instrumentation;

  private TestRecyclerViewFragment fragment;
  private DocumentsAdapter adapter;
  private RecyclerView recyclerView;
  private DocumentsAdapter.DocumentViewHolder viewHolder;

  private InMemoryDocument dummyDoc;

  @Rule
  public ActivityTestRule<ActivityForTest> activityTestRule = new ActivityTestRule<>(ActivityForTest.class);

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

  private void addDoc() {
    activity.runOnUiThread(() -> {
      adapter.clear();
      adapter.addItem(dummyDoc);
    });
    instrumentation.waitForIdleSync();
  }

  private void getViewHolder(int position) {
    AtomicReference<DocumentsAdapter.DocumentViewHolder> reference = new AtomicReference<>();

    activity.runOnUiThread(() -> reference.set( (DocumentsAdapter.DocumentViewHolder) recyclerView.findViewHolderForAdapterPosition(position) ));
    instrumentation.waitForIdleSync();

    viewHolder = reference.get();
  }

  @Test
  public void recyclerViewIsPresent() {
    View viewById = activity.findViewById(R.id.testactivity_recycler_view);
    assertThat(viewById, notNullValue());
    assertThat(viewById, instanceOf(RecyclerView.class));
  }

  @Test
  public void docAdded() {
    dummyDoc = generateDocument();

    addDoc();

    onView(withId(R.id.swipe_layout_title)).check(matches(withText( dummyDoc.getDocument().getShortDescription() )));
    onView(withId(R.id.swipe_layout_subtitle)).check(matches(withText( dummyDoc.getDocument().getComment() )));
    onView(withId(R.id.swipe_layout_date)).check(matches(withText( dummyDoc.getDocument().getTitle() )));
    onView(withId(R.id.swipe_layout_from)).check(matches(withText( dummyDoc.getDocument().getOrganization() )));
    onView(withId(R.id.swipe_layout_urgency_badge)).check(matches(not(isDisplayed())));
    onView(withId(R.id.sync_label)).check(matches(not(isDisplayed())));
    onView(withId(R.id.favorite_label)).check(matches(not(isDisplayed())));
    onView(withId(R.id.control_label)).check(matches(not(isDisplayed())));
    onView(withId(R.id.lock_label)).check(matches(not(isDisplayed())));
    onView(withId(R.id.swipe_layout_date)).check(matches(withTextColor(ContextCompat.getColor( activity, R.color.md_grey_800 ))));
  }

  @Test
  public void from() {
    dummyDoc = generateDocument();
    dummyDoc.getDocument().setOrganization( "Без организации" );
    dummyDoc.getDocument().getSigner().setOrganisation("");

    addDoc();

    onView(withId(R.id.swipe_layout_from)).check(matches(withText( dummyDoc.getDocument().getOrganization() )));

    dummyDoc.setIndex( "incoming_orders" );
    addDoc();
    onView(withId(R.id.swipe_layout_from)).check(matches(withText( "" )));

    dummyDoc.setIndex( "citizen_requests" );
    addDoc();
    onView(withId(R.id.swipe_layout_from)).check(matches(withText( "" )));
  }

  @Test
  public void ready() {
    dummyDoc = generateDocument();
    dummyDoc.setAsReady();

    addDoc();

    // Check if document is in READY state
    onView(withId(R.id.sync_label)).check(matches(not(isDisplayed())));
    onView(withId(R.id.swipe_layout_cv)).check(matches(isClickable()));
    onView(withId(R.id.swipe_layout_cv)).check(matches(isFocusable()));
  }

  @Test
  public void loading() {
    dummyDoc = generateDocument();
    dummyDoc.setAsLoading();

    addDoc();

    // Check if document is in LOADING state
    onView(withId(R.id.sync_label)).check(matches(isDisplayed()));
    onView(withId(R.id.swipe_layout_cv)).check(matches(not(isClickable())));
    onView(withId(R.id.swipe_layout_cv)).check(matches(not(isFocusable())));
  }

  @Test
  public void urgency() {
    dummyDoc = generateDocument();
    dummyDoc.getDocument().setUrgency( "Срочно" );

    addDoc();

    onView(withText(dummyDoc.getDocument().getUrgency())).check(matches(isDisplayed()));
  }

  @Test
  public void sync() {
    dummyDoc = generateDocument();
    dummyDoc.getDocument().setChanged(true);

    addDoc();

    onView(withId(R.id.sync_label)).check(matches(isDisplayed()));
  }

  @Test
  public void favorite() {
    dummyDoc = generateDocument();
    dummyDoc.getDocument().setFavorites(true);

    addDoc();

    onView(withId(R.id.favorite_label)).check(matches(isDisplayed()));
  }

  @Test
  public void control() {
    dummyDoc = generateDocument();
    dummyDoc.getDocument().setControl(true);

    addDoc();

    onView(withId(R.id.control_label)).check(matches(isDisplayed()));
  }

  @Test
  public void lock() {
    dummyDoc = generateDocument();
    dummyDoc.getDocument().setFromFavoritesFolder(true);

    addDoc();

    onView(withId(R.id.lock_label)).check(matches(isDisplayed()));

    dummyDoc.getDocument().setFromFavoritesFolder(false);
    dummyDoc.getDocument().setFromProcessedFolder(true);

    addDoc();

    onView(withId(R.id.lock_label)).check(matches(isDisplayed()));
  }

  @Test
  public void red() {
    dummyDoc = generateDocument();
    dummyDoc.getDocument().setRed(true);

    addDoc();
    getViewHolder(0);

    onView(withId(R.id.swipe_layout_date)).check(matches(withTextColor(ContextCompat.getColor( activity, R.color.md_red_A700 ))));
    assertEquals( R.drawable.top_border, viewHolder.getBackgroundResourceId() );
  }

  @Test
  public void returned() {
    dummyDoc = generateDocument();
    dummyDoc.getDocument().setReturned(true);

    addDoc();

    onView(withId(R.id.returned_label)).check(matches(isDisplayed()));
  }

  @Test
  public void rejected() {
    dummyDoc = generateDocument();
    dummyDoc.getDocument().setRejected(true);

    addDoc();

    onView(withId(R.id.rejected_label)).check(matches(isDisplayed()));
  }

  @Test
  public void again() {
    dummyDoc = generateDocument();
    dummyDoc.getDocument().setAgain(true);

    addDoc();

    onView(withId(R.id.again_label)).check(matches(isDisplayed()));
  }

  @Test
  public void showAllLabels() {
    dummyDoc = generateDocument();
    dummyDoc.getDocument().setChanged(true);
    dummyDoc.getDocument().setUrgency( "Весьма срочно" );
    dummyDoc.getDocument().setControl(true);
    dummyDoc.getDocument().setFavorites(true);
    dummyDoc.getDocument().setFromFavoritesFolder(true);
    dummyDoc.getDocument().setReturned(true);

    addDoc();

    // These labels must be visible simultaneously
    onView(withId(R.id.sync_label)).check(matches(isDisplayed()));
    onView(withText(dummyDoc.getDocument().getUrgency())).check(matches(isDisplayed()));
    onView(withId(R.id.control_label)).check(matches(isDisplayed()));
    onView(withId(R.id.favorite_label)).check(matches(isDisplayed()));
    onView(withId(R.id.lock_label)).check(matches(isDisplayed()));
    onView(withId(R.id.returned_label)).check(matches(isDisplayed()));
  }
}
