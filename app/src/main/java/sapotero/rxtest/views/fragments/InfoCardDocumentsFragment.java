package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.events.bus.FileDownloadedEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.views.activities.DocumentImageFullScreenActivity;
import sapotero.rxtest.views.adapters.DocumentLinkAdapter;
import sapotero.rxtest.views.custom.CircleLeftArrow;
import sapotero.rxtest.views.custom.CircleRightArrow;
import timber.log.Timber;

public class InfoCardDocumentsFragment extends Fragment implements AdapterView.OnItemClickListener, GestureDetector.OnDoubleTapListener {

  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private OnFragmentInteractionListener mListener;
  private Context mContext;
  private String TAG = this.getClass().getSimpleName();

  private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";

  @BindView(R.id.pdfView) PDFView pdfView;

  @BindView(R.id.info_card_pdf_fullscreen_prev_document) CircleLeftArrow prev_document;
  @BindView(R.id.info_card_pdf_fullscreen_next_document) CircleRightArrow next_document;
  @BindView(R.id.info_card_pdf_fullscreen_document_counter) TextView document_counter;
  @BindView(R.id.info_card_pdf_fullscreen_page_title)       TextView document_title;
  @BindView(R.id.info_card_pdf_fullscreen_page_counter)     TextView page_counter;
  @BindView(R.id.info_card_pdf_fullscreen_button) FrameLayout fullscreen;

  @BindView(R.id.info_card_pdf_no_files) TextView no_files;
  @BindView(R.id.info_card_pdf_wrapper)  FrameLayout pdf_wrapper;

  @BindView(R.id.fragment_info_card_urgency_title) TextView urgency;


  private int index;

  private Preference<String> UID;
  private DocumentLinkAdapter adapter;
  private String uid;
  private Boolean withOutZoom = false;


  public InfoCardDocumentsFragment() {
  }


  private void loadSettings() {
    UID  = settings.getString("activity_main_menu.uid");
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if ( !EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().register(this);
    }

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_documents, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getDataComponent().inject( this );

    loadSettings();

    if (null != savedInstanceState) {
      index = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, 0);
    }

    updateDocument();

    return view;
  }

  public void updateDocument(){

    ArrayList<Image> documents = new ArrayList<Image>();
    adapter = new DocumentLinkAdapter(mContext, documents);

    RDocumentEntity document = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid == null ? UID.get() : uid))
      .get()
      .firstOrNull();

    Timber.tag("IMAGESSS").e("%s", document.getUid() );

    //resolved https://tasks.n-core.ru/browse/MVDESD-12626 - срочность
    if ( document.getUrgency() != null ){
      urgency.setVisibility(View.VISIBLE);
    }

    if (document.getImages().size() > 0){
      adapter.clear();

      for (RImage image : document.getImages()) {
        RImageEntity img = (RImageEntity) image;
        Timber.tag(TAG).i("image " + img.getTitle() );
        adapter.add( img );
      }

    }


    index = 0;


    if (adapter.getCount() > 0) {

      try {
        setPdfPreview();
      } catch (Exception e) {
        e.printStackTrace();
      }

      no_files.setVisibility(View.GONE);
      pdf_wrapper.setVisibility(View.VISIBLE);
    } else {
      no_files.setVisibility(View.VISIBLE);
      pdf_wrapper.setVisibility(View.GONE);
    }
  }

  private void setPdfPreview() {
    Image image = adapter.getItem(index);

    document_title.setText( image.getTitle() );

    File file = new File(getContext().getFilesDir(), String.format( "%s_%s", image.getMd5(), image.getTitle() ));


    if (file.exists()){
      pdfView
        .fromFile( file )
        .enableSwipe(true)
        .enableDoubletap(true)
        .defaultPage(0)
        .swipeHorizontal(false)
        .enableAntialiasing(true)
        .onRender((nbPages, pageWidth, pageHeight) -> pdfView.fitToWidth())
        .onLoad(nbPages -> {
          Timber.tag(TAG).i(" onLoad");
        })
        .onError(t -> {
          Timber.tag(TAG).i(" onError");
        })
        .onPageChange((page, pageCount) -> {
          Timber.tag(TAG).i(" onPageChange");
          updatePageCount();
        })
        .enableAnnotationRendering(false)
        .password(null)
        .scrollHandle(null)
        .load();
      updateDocumentCount();
      updatePageCount();
      updateZoomVisibility();
    }
  }

  private void updateZoomVisibility() {
    if (withOutZoom){
      fullscreen.setVisibility(View.GONE);
    }
  }

  public void updateDocumentCount(){
    document_counter.setText( String.format("%s из %s", index + 1, adapter.getCount()) );
  }

  public void updatePageCount(){
    page_counter.setText( String.format("%s из %s страниц", pdfView.getCurrentPage() + 1, pdfView.getPageCount()) );
  }

  @OnClick(R.id.info_card_pdf_fullscreen_prev_document)
  public void setLeftArrowArrow() {
    Timber.tag(TAG).i( "BEFORE %s - %s", index, adapter.getCount() );
    if ( index <= 0 ){
      index = adapter.getCount()-1;
    } else {
      index--;
    }
    Timber.tag(TAG).i( "AFTER %s - %s", index, adapter.getCount() );

    setPdfPreview();
  }

  @OnClick(R.id.info_card_pdf_fullscreen_next_document)
  public void setRightArrow() {
    Timber.tag(TAG).i( "BEFORE %s - %s", index, adapter.getCount() );
    if ( index >= adapter.getCount()-1 ){
      index = 0;
    } else {
      index++;
    }
    Timber.tag(TAG).i( "AFTER %s - %s", index, adapter.getCount() );

    setPdfPreview();
  }

  @OnClick(R.id.info_card_pdf_fullscreen_button)
  public void fullscreen() {
    Type listType = new TypeToken<ArrayList<Image>>() {}.getType();

    Context context = getContext();

    Intent intent = new Intent( context, DocumentImageFullScreenActivity.class);
    intent.putExtra( "files", new Gson().toJson( adapter.getItems(), listType ) );
    intent.putExtra( "index", index );
    context.startActivity(intent);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
      mContext = context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    pdfView.recycle();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Toast.makeText(getContext(), "test", Toast.LENGTH_SHORT).show();
  }

  @Override
  public boolean onSingleTapConfirmed(MotionEvent e) {
    return false;
  }

  @Override
  public boolean onDoubleTap(MotionEvent motionEvent) {
    return false;
  }


  @Override
  public boolean onDoubleTapEvent(MotionEvent e) {
    return false;
  }

  public InfoCardDocumentsFragment withUid(String uid) {
    this.uid = uid;
    return this;
  }

  public InfoCardDocumentsFragment withOutZoom(Boolean withOutZoom) {
    this.withOutZoom = withOutZoom;
    return this;
  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(FileDownloadedEvent event) {
    Log.d("FileDownloadedEvent", event.path);

  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCurrentDocumentEvent event) throws Exception {
    Timber.tag(TAG).w("UpdateCurrentDocumentEvent %s", event.uid);
    if (Objects.equals(event.uid, UID.get())){
      updateDocument();
    }
  }


}
