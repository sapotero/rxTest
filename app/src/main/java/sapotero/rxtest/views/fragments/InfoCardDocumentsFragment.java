package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.views.activities.DocumentImageFullScreenActivity;
import sapotero.rxtest.views.adapters.DocumentLinkAdapter;
import sapotero.rxtest.views.views.CircleLeftArrow;
import sapotero.rxtest.views.views.CircleRightArrow;
import timber.log.Timber;

public class InfoCardDocumentsFragment extends Fragment implements AdapterView.OnItemClickListener, GestureDetector.OnDoubleTapListener {

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;

  private OnFragmentInteractionListener mListener;
  private Context mContext;
  private String TAG = this.getClass().getSimpleName();


  // for PDF
  private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";
  private ParcelFileDescriptor mFileDescriptor;

  private PdfRenderer mPdfRenderer;
  private PdfRenderer.Page mCurrentPage;

//  @BindView(R.id.pdf_previous) Button mButtonPrevious;
//  @BindView(R.id.pdf_next) Button     mButtonNext;
//  @BindView(R.id.documents_files) Spinner mDocumentList;
//  @BindView(R.id.pageInfo) TextView pageInfo;

  @BindView(R.id.pdfView) PDFView pdfView;

  @BindView(R.id.info_card_pdf_fullscreen_prev_document) CircleLeftArrow prev_document;
  @BindView(R.id.info_card_pdf_fullscreen_next_document) CircleRightArrow next_document;
  @BindView(R.id.info_card_pdf_fullscreen_document_counter) TextView document_counter;
  @BindView(R.id.info_card_pdf_fullscreen_page_title)       TextView document_title;
  @BindView(R.id.info_card_pdf_fullscreen_page_counter)     TextView page_counter;
  @BindView(R.id.info_card_pdf_fullscreen_button) ImageButton fullscreen;

  @BindView(R.id.info_card_pdf_no_files) TextView no_files;
  @BindView(R.id.info_card_pdf_wrapper)  FrameLayout pdf_wrapper;



  private int index;

//  private PhotoViewAttacher mAttacher;
  private Image IMAGE;
  private String fileName;
  private Preference<String> UID;
  private DocumentLinkAdapter adapter;
  private Preference<String> HOST;


  public InfoCardDocumentsFragment() {
  }


  public static InfoCardDocumentsFragment newInstance(String param1, String param2) {
    InfoCardDocumentsFragment fragment = new InfoCardDocumentsFragment();

    return fragment;
  }

  private void loadSettings() {
    UID  = settings.getString("main_menu.uid");
    HOST = settings.getString("settings_username_host");
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
    EsdApplication.getComponent(mContext).inject( this );

    loadSettings();


    ArrayList<Image> documents = new ArrayList<Image>();
    adapter = new DocumentLinkAdapter(mContext, documents);
//    mDocumentList.setAdapter(adapter);

//    dataStore
//      .select(RDocumentEntity.class)
//      .where(RDocumentEntity.UID.eq(UID.get()))
//      .get()
//      .toObservable()
//      .subscribeOn(Schedulers.io())
//      .observeOn(AndroidSchedulers.mainThread())
//      .subscribe(
//        document -> {
//          if (document.getImages().size() > 0){
//            for (RImage image : document.getImages()) {
//
//              RImageEntity img = (RImageEntity) image;
//
//              Timber.tag(TAG).i("image " + img.getTitle() );
//            }
//          }
//        });
    List<RDocumentEntity> files = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(UID.get()))
      .get()
      .toList();

    for ( RDocumentEntity document: files){
      if (document.getImages().size() > 0){
        for (RImage image : document.getImages()) {
          RImageEntity img = (RImageEntity) image;
          Timber.tag(TAG).i("image " + img.getTitle() );
          adapter.add( img );
        }
      }
    }

//    mDocumentList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//      @Override
//      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        Image image = (Image) mDocumentList.getItemAtPosition(position);
//        IMAGE = image;
//        Timber.tag(TAG).i( " setOnItemClickListener " + image.getPath() );
//
//        File file = new File(mContext.getFilesDir(), image.getMd5()+"_"+image.getTitle());
//
//        pdfView
//          .fromFile(file)
//          .enableSwipe(true)
//          .enableDoubletap(true)
//          .defaultPage(0)
//          .swipeHorizontal(false)
//          .onLoad(nbPages -> {
//            progressBar.setVisibility(View.GONE);
//          })
//          .onError(t -> progressBar.setVisibility(View.GONE))
//          .onPageChange((page, pageCount) -> {
//            updatePageInfo();
//          })
//          .enableAnnotationRendering(false)
//          .password(null)
//          .scrollHandle(null)
//          .load();
//
//      }
//
//      @Override
//      public void onNothingSelected(AdapterView<?> parent) {
//
//      }
//    });
//

    index = 0;
    if (null != savedInstanceState) {
      index = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, 0);
    }



    updateDocument();

    return view;
  }

  public void updateDocument(){
    if (adapter.getCount() > 0) {
      setPdfPreview();
      updateDocumentCount();
      updatePageCount();
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

    pdfView
      .fromFile( file )
      .enableSwipe(true)
      .enableDoubletap(true)
      .defaultPage(0)
      .swipeHorizontal(false)
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
  }

  public void updateDocumentCount(){
    document_counter.setText( String.format("%s из %s", index + 1, adapter.getCount()) );
  }

  public void updatePageCount(){
    page_counter.setText( String.format("%s из %s страниц", pdfView.getCurrentPage() + 1, pdfView.getPageCount()) );
  }


  private void setDocumentPreview( int index ) {
    if (mPdfRenderer.getPageCount() <= index) {
      return;
    }

    if (null != mCurrentPage) {
      mCurrentPage.close();
    }

    getFromPdf(index);

    updatePreview();
  }

  public void getFromPdf(int index) {
    Boolean exist = true;

    mCurrentPage = mPdfRenderer.openPage(index);

    Bitmap image = Bitmap.createBitmap(
      getResources().getDisplayMetrics().densityDpi / 72 * mCurrentPage.getWidth(),
      getResources().getDisplayMetrics().densityDpi / 72 * mCurrentPage.getHeight(),
      Bitmap.Config.ARGB_8888
    );

    mCurrentPage.render(image, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);



    if ( IMAGE != null){
      fileName = String.format( "%s_%s", index, IMAGE.getMd5());

      File file = getActivity().getFileStreamPath(fileName);
      if(file == null || !file.exists()) {
        exist = false;
      }

      // если файл есть то читаем с диска
      if (exist){
        try {
//          mImageView.setImage(ImageSource.uri( mContext.getFileStreamPath( fileName ).toString() ));
//          mImageView.setImageBitmap( BitmapFactory.decodeStream(getActivity().openFileInput(fileName)) );
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      // если файла нет то пишем в tmp
      else {
        try {
          ByteArrayOutputStream bytes = new ByteArrayOutputStream();

          image.compress(Bitmap.CompressFormat.PNG, 100, bytes);

          FileOutputStream fo = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);
          fo.write(bytes.toByteArray());
          fo.close();

//          mImageView.setImage(ImageSource.uri(fileName));
//          mImageView.setImage(ImageSource.uri( mContext.getFileStreamPath( fileName ).toString() ));
//          mImageView.setImageBitmap( BitmapFactory.decodeStream(getActivity().openFileInput(fileName)) );
        } catch (Exception e) {
          e.printStackTrace();
        }
      }


    } else {
//      mImageView.setImageBitmap(image);
//      mImageView.setImage(ImageSource.bitmap(image));

    }

    System.gc();

  }

  private void updatePreview() {
    int index = mCurrentPage.getIndex();
    int pageCount = mPdfRenderer.getPageCount();
//    mButtonPrevious.setEnabled(0 != index);
//    mButtonNext.setEnabled(index + 1 < pageCount);
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

    updateDocument();
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

    updateDocument();
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

//  @OnClick(R.id.pdf_previous)
//  public void previousPage(View view) {
//    try {
//      pdfView.jumpTo( pdfView.getCurrentPage() - 1 );
//      updatePageInfo();
//    } catch (NullPointerException e) {
//      e.printStackTrace();
//    }
//
//  }

//  @OnClick(R.id.pdf_next)
//  public void nextPage(View view) {
//    try {
//      pdfView.jumpTo( pdfView.getCurrentPage() + 1 );
//      updatePageInfo();
//    } catch (NullPointerException e) {
//      e.printStackTrace();
//    }
//  }

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

//  private void updatePageInfo(){
//    if ( pdfView != null ){
//      pageInfo.setText( String.format(" Стр. %s/%s ", pdfView.getCurrentPage()+1 , pdfView.getPageCount()) );
//    }
//  }

  @Override
  public void onDetach() {
    super.onDetach();

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
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
  public boolean onDoubleTap(MotionEvent e) {
    Timber.tag(TAG).i("onDoubleTap");
    Intent intent = new Intent(mContext, DocumentImageFullScreenActivity.class);
    intent.putExtra("filename", fileName );
    startActivity(intent);
    return false;
  }

  @Override
  public boolean onDoubleTapEvent(MotionEvent e) {
    return false;
  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(FileDownloadedEvent event) {
    Log.d("FileDownloadedEvent", event.path);
//
//    if (!Objects.equals(event.path, "")){
//      try {
////        doc_tmp_layout.setVisibility(View.GONE);
////        mImageView.setVisibility(View.VISIBLE);
////        progressBar.setVisibility(View.GONE);
//
//        openRenderer( getActivity(), event.path );
//      } catch (IOException e) {
//        e.printStackTrace();
////        doc_tmp_layout.setVisibility(View.VISIBLE);
////        mImageView.setVisibility(View.GONE);
////        progressBar.setVisibility(View.GONE);
//      }
//    }
  }


}
