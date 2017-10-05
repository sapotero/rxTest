package sapotero.rxtest.views.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.jobs.bus.ReloadProcessedImageJob;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Transaction;
import sapotero.rxtest.views.activities.DocumentImageFullScreenActivity;
import sapotero.rxtest.views.adapters.DocumentLinkAdapter;
import sapotero.rxtest.views.custom.CircleLeftArrow;
import sapotero.rxtest.views.custom.CircleRightArrow;
import sapotero.rxtest.views.fragments.interfaces.PreviewFragment;
import timber.log.Timber;

public class InfoCardDocumentsFragment extends PreviewFragment implements AdapterView.OnItemClickListener, GestureDetector.OnDoubleTapListener {

  public static final int REQUEST_CODE_INDEX = 1;

  @Inject ISettings settings;
  @Inject MemoryStore store;
  @Inject JobManager jobManager;

  @BindView(R.id.pdfView) PDFView pdfView;

  @BindView(R.id.info_card_pdf_fullscreen_prev_document) CircleLeftArrow prev_document;

  @BindView(R.id.info_card_pdf_fullscreen_next_document) CircleRightArrow next_document;
  @BindView(R.id.info_card_pdf_fullscreen_document_counter) TextView document_counter;
  @BindView(R.id.info_card_pdf_fullscreen_page_title)       TextView document_title;
  @BindView(R.id.info_card_pdf_fullscreen_page_counter)     TextView page_counter;
  @BindView(R.id.info_card_pdf_fullscreen_button) FrameLayout fullscreen;
  @BindView(R.id.deleted_image) FrameLayout deletedImage;
  @BindView(R.id.broken_image) FrameLayout broken_image;
  @BindView(R.id.loading_image) FrameLayout loading_image;
  @BindView(R.id.info_card_pdf_reload) Button reloadImageButton;
  @BindView(R.id.info_card_pdf_no_files) TextView no_files;

  @BindView(R.id.info_card_pdf_wrapper)  FrameLayout pdf_wrapper;
  @BindView(R.id.fragment_info_card_urgency_title) TextView urgency;

  @BindView(R.id.open_in_another_app_wrapper) LinearLayout open_in_another_app_wrapper;
  @BindView(R.id.pdf_linear_wrapper) RelativeLayout pdf_linear_wrapper;

  private Context mContext;
  private String TAG = this.getClass().getSimpleName();

  private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";

  private int index = 0;

  private DocumentLinkAdapter adapter;
  private String uid;
  private Boolean withOutZoom = false;

  private File file;
  private String contentType;
  private PublishSubject<Float> directionSub = PublishSubject.create();


  private final SwipeUtil swipeUtil;
  private boolean toastShown = false;
  private Subscription sub;
  private Subscription reload;
  private boolean canScroll = true;

  public InfoCardDocumentsFragment() {
    swipeUtil = new SwipeUtil();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_documents, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getManagerComponent().inject( this );

    initEvents();

    if (null != savedInstanceState) {
      index = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, 0);
    }

    new Handler().postDelayed(this::updateDocument, 200);
//    updateDocument();
//    initSubscription();

    return view;
  }

  @Override
  public void update() {
    updateDocument();
  }


  private void initSubscription() {
    // TODO: создавать подписку только если образов больше 1

    directionSub = PublishSubject.create();

    sub = directionSub
      .buffer( 600, TimeUnit.MILLISECONDS )
      .onBackpressureBuffer(32)
      .onBackpressureDrop()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        positions -> {
          Timber.i("SIZE: %s", positions.size());

          if ( positions.size() >= 16 ){
            Boolean changed = false;

            for (int i = 0; i < positions.size(); i++) {
              if (i > 1){
                 if (!Objects.equals(positions.get(i - 1), positions.get(i))){
                   changed = true;
                   break;
                 }
              }
            }

            if (!changed){
              Timber.d("NOT CHANGED: %s", positions.get(0) );

              if ( positions.get(0) == 0.0f ){
                if (canScroll){
                  canScroll = false;
                  getPrevImage();
                }
              }

              if ( positions.get(0) == 1.0f ){
                if (canScroll){
                  canScroll = false;
                  getNextImage();
                }
              }
            }


          }
        },
        Timber::e
      );
  }

  public void updateDocument(){
    ArrayList<Image> images = new ArrayList<>();
    adapter = new DocumentLinkAdapter(mContext, images);

    InMemoryDocument document = store.getDocuments().get( uid == null ? settings.getUid() : uid );

    if ( document != null ) {
      //resolved https://tasks.n-core.ru/browse/MVDESD-12626 - срочность
      if ( document.getDocument() != null && document.getDocument().getUrgency() != null ) {
        urgency.setVisibility(View.VISIBLE);
      }

      index = settings.getImageIndex();

      if (document.getImages().size() > 0){
        adapter.clear();

        List<Image> tmp = new ArrayList<>();
        tmp.addAll( document.getImages() );

        try {
          Collections.sort(tmp, (o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
        } catch (Exception e) {
          e.printStackTrace();
        }

        try {
          Collections.sort(tmp, (o1, o2) -> o1.getNumber().compareTo( o2.getNumber() ));
        } catch (Exception e) {
          e.printStackTrace();
        }

        adapter.addAll( tmp );

        showPdf();

        no_files.setVisibility(View.GONE);
        pdf_wrapper.setVisibility(View.VISIBLE);

      } else {
        disablePdfView();
        no_files.setVisibility(View.VISIBLE);
        pdf_wrapper.setVisibility(View.GONE);
        open_in_another_app_wrapper.setVisibility(View.GONE);
      }
    }
  }

  private void setPdfPreview() throws FileNotFoundException {
    if (getContext() != null && getContext().getFilesDir() != null ){
      Image image = adapter.getItem(index);
      file = new File(getContext().getFilesDir(), image.getFileName() );

      Timber.tag(TAG).e("image: %s", new Gson().toJson(image) );
      Timber.tag(TAG).e("file: %s", file.toString() );

      // Проверяем, существует ли ЭО
      // ЭО автоматически удаляются через период времени
      // заданный в настройках
      if ( image.isDeleted() ) {
        showDownloadButton();

      } else if ( image.isNoFreeSpace() ) {
        // TODO: change to special message
        showDownloadButton();

      } else {
        // Проверяем что файл загружен полность,
        // иначе рисуем крутилку с окошком
        if (image.getSize() != null && file.length() == image.getSize()) {
          Timber.tag(TAG).e("image size: %s | %s", file.length(), image.getSize());
          showFileLoading(false);

          contentType = image.getContentType();
          document_title.setText( image.getTitle() );

          if ( Objects.equals(contentType, "application/pdf") ) {
            InputStream targetStream = new FileInputStream(file);

            if (file.exists()) {
              com.github.barteksc.pdfviewer.util.Constants.THUMBNAIL_RATIO = 1.0f;
              com.github.barteksc.pdfviewer.util.Constants.PART_SIZE = 256;

              pdfView
                .fromStream(targetStream)
                .enableSwipe(true)
                .enableDoubletap(true)
                .defaultPage(0)
                .swipeHorizontal(false)
                .onRender((nbPages, pageWidth, pageHeight) -> pdfView.fitToWidth())
                .onPageChange((page, pageCount) -> updatePageCount())
                .onPageScroll(this::setDirection)
                .enableAnnotationRendering(true)
                .scrollHandle(null)
                .load();

              pdfView.useBestQuality(false);
              pdfView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
              pdfView.setWillNotCacheDrawing(false);
              pdfView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
              pdfView.setDrawingCacheEnabled(true);
              pdfView.enableRenderDuringScale(false);
            }

            pdfView.setVisibility(View.VISIBLE);
            open_in_another_app_wrapper.setVisibility(View.GONE);
            page_counter.setVisibility(View.VISIBLE);

          } else {
            pdfView.setVisibility(View.GONE);
            open_in_another_app_wrapper.setVisibility(View.VISIBLE);
            page_counter.setVisibility(View.INVISIBLE);
          }

          updateDocumentCount();
          updatePageCount();
          updateVisibility();

        } else {
          showFileLoading(true);
        }
      }

      hideZoom();
    }
  }

  private void showFileLoading(boolean show) {
    loading_image.setVisibility( show ? View.VISIBLE : View.GONE );
    pdfView.setEnabled(!show);

    if (show){
      startReloadSubscription();
    } else {
      stopReloadSubscription();
    }
  }

  private void startReloadSubscription() {
    stopReloadSubscription();

    reload = PublishSubject.create().buffer( 5000, TimeUnit.MILLISECONDS )
      .onBackpressureBuffer(1)
      .onBackpressureDrop()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe( data->{
        try {
          Timber.d("startReloadSubscription");
          setPdfPreview();
        } catch (FileNotFoundException e) {
          Timber.e(e);
        }
      }, Timber::e );
  }
  private void stopReloadSubscription() {
    if (reload != null) {
      reload.unsubscribe();
    }
  }

  private void setDirection(int page, float positionOffset) {

    if ( adapter.getItems().size() > 1 ){

      SwipeUtil.DIRECTION direction = SwipeUtil.DIRECTION.OTHER;

      if (positionOffset == 0.0f){
        direction = SwipeUtil.DIRECTION.UP;
      }

      if (positionOffset == 1.0f){
        direction = SwipeUtil.DIRECTION.DOWN;
      }

      if ( swipeUtil.isSameDirection(direction) ) {
        if (direction != SwipeUtil.DIRECTION.OTHER) {

          if ( !toastShown ) {
            Toast toast = Toast.makeText(getContext(), direction.getMessage(), Toast.LENGTH_SHORT);

            if ( index == adapter.getCount()-1 && direction != SwipeUtil.DIRECTION.DOWN
              || index == 0 && direction != SwipeUtil.DIRECTION.UP){
              toast.show();
            }

            toastShown = true;

          }

        }
      } else {
        toastShown = false;
        canScroll = true;
      }


      swipeUtil.setDirection( direction );

      if (directionSub != null) {
        directionSub.onNext(positionOffset);
      }

    }

  }

  private void showDownloadButton() {
    deletedImage.setVisibility(View.VISIBLE);
  }

  private void updateVisibility() {
    hideZoom();
    deletedImage.setVisibility(View.GONE);
  }

  private void hideZoom() {
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
  public void getPrevImage() {
    Timber.tag(TAG).i( "BEFORE %s - %s", index, adapter.getCount() );
    if ( index <= 0 ){
      index = 0;
    } else {
      index--;
      settings.setImageIndex( index );
      showPdf();
    }
    Timber.tag(TAG).i( "AFTER %s - %s", index, adapter.getCount() );


  }

  @OnClick(R.id.info_card_pdf_reload)
  public void reloadImage(){

    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get( settings.getUid() ) )
      .setLabel(LabelType.SYNC);
    store.process( transaction );

    jobManager.addJobInBackground( new ReloadProcessedImageJob( settings.getUid() ) );

    getActivity().finish();
  }

  @OnClick(R.id.info_card_pdf_fullscreen_next_document)
  public void getNextImage() {
    Timber.tag(TAG).i( "BEFORE %s - %s", index, adapter.getCount() );
    if ( index >= adapter.getCount()-1 ){
      index = adapter.getCount()-1;
    } else {
      index++;
      settings.setImageIndex( index );
      showPdf();
    }
    Timber.tag(TAG).i( "AFTER %s - %s", index, adapter.getCount() );
  }

  private void showPdf() {
    try {
      hideBrokenImage();
      setPdfPreview();
    } catch (FileNotFoundException e) {
      Timber.e(e);
      disablePdfView();
      showBrokenImage();
    }
  }

  private void showBrokenImage() {
    broken_image.setVisibility(View.VISIBLE);
  }

  private void disablePdfView() {
    if (pdfView != null) {
      pdfView.recycle();
    }

    try {
      InputStream is = getContext().getAssets().open("empty.pdf");
      pdfView.fromStream( is ).load();
    } catch (IOException e) {
      e.printStackTrace();
    }


  }

  private void hideBrokenImage() {
    broken_image.setVisibility(View.GONE);
  }

  @OnClick(R.id.info_card_pdf_fullscreen_button)
  public void fullscreen() {
    // Start DocumentImageFullScreenActivity, which uses another instance of this fragment for full screen PDF view.
    Intent intent = DocumentImageFullScreenActivity.newIntent( getContext(), adapter.getItems(), index );
    startActivityForResult(intent, REQUEST_CODE_INDEX);
  }

  @OnClick(R.id.info_card_pdf_open)
  public void openPdf() {
    openInAnotherApp();
  }



  // This is called, when DocumentImageFullScreenActivity returns
  // (needed to switch to the image shown in full screen).
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if ( resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_INDEX ) {
      index = settings.getImageIndex();
      showPdf();
    }
    if ( resultCode == Activity.RESULT_CANCELED && requestCode == REQUEST_CODE_INDEX ) {
      updateAfterDocUpdated();
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mContext = context;
  }

  @Override
  public void onResume() {
    super.onResume();

    initSubscription();
  }

  @Override
  public void onPause() {
    super.onPause();

    if (sub != null) {
      sub.unsubscribe();
      directionSub = null;
    }
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

  // resolved https://tasks.n-core.ru/browse/MVDESD-13415
  // Если ЭО имеет формат, отличный от PDF, предлагать открыть во внешнем приложении
  @OnClick(R.id.open_in_another_app)
  public void openInAnotherApp() {
    if ( file != null && contentType != null) {
      Uri contentUri = FileProvider.getUriForFile(getContext(), "sed.mobile.fileprovider", file);
      Intent intent = new Intent();
      intent.setAction(Intent.ACTION_VIEW);
      intent.setDataAndType(contentUri, contentType);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

      Intent chooser = Intent.createChooser(intent, "Открыть с помощью");

      if (intent.resolveActivity(getContext().getPackageManager()) != null) {
        startActivity(chooser);
      } else {
        Toast.makeText(getContext(), "Подходящие приложения не установлены", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void initEvents() {
    Timber.tag(TAG).v("initEvents");
    unregisterEventBus();
    EventBus.getDefault().register(this);
  }

  private void unregisterEventBus() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
  }

  @Override
  public void onDestroy(){
    super.onDestroy();
    unregisterEventBus();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCurrentDocumentEvent event) throws Exception {
    Timber.tag(TAG).w("UpdateCurrentDocumentEvent %s", event.uid);
    if (Objects.equals(event.uid, uid != null ? uid : settings.getUid())) {
      updateAfterDocUpdated();
      getActivity().setResult(Activity.RESULT_CANCELED);
    }
  }

  private void updateAfterDocUpdated() {
    settings.setImageIndex(0);
    updateDocument();
  }

  private static class SwipeUtil{

    enum DIRECTION{
      UP("Для перехода к предыдущему электронному образу потяните ↓"), DOWN("Для перехода к следующему электронному образу потяните ↑"), OTHER("");

      private final String message;
      DIRECTION(String message) {
        this.message = message;
      }

      public String getMessage() {
        return message;
      }
    }

    DIRECTION direction;


    SwipeUtil() {
      this.direction = DIRECTION.OTHER;
    }

    Boolean isSameDirection(DIRECTION direction){
      return direction == this.direction;
    }

    void setDirection(DIRECTION direction) {
      this.direction = direction;
    }

    public String getMessage(){
      return direction.getMessage();
    }
  }
}
