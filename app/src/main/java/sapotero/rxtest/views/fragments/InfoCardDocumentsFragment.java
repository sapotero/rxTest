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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.github.barteksc.pdfviewer.PDFView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.events.bus.FileDownloadedEvent;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.views.activities.DocumentImageFullScreenActivity;
import sapotero.rxtest.views.adapters.DocumentLinkAdapter;
import timber.log.Timber;
//import uk.co.senab.photoview.PhotoViewAttacher;

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

//  @BindView(R.id.pdf_image) ImageView mImageView;
//  @BindView(R.id.pdf_image) SubsamplingScaleImageView mImageView;
//  @BindView(R.id.empty_list_image) ImageView empty_list_image;
//  @BindView(R.id.doc_tmp_layout) FrameLayout doc_tmp_layout;
//  @BindView(R.id.documents_files_progressbar) ProgressBar progressBar;


  @BindView(R.id.pdf_previous) Button mButtonPrevious;
  @BindView(R.id.pdf_next) Button     mButtonNext;
  @BindView(R.id.documents_files) Spinner mDocumentList;


  @BindView(R.id.pageInfo) TextView pageInfo;




  @BindView(R.id.pdfView) PDFView pdfView;

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
    UID  = settings.getString("info.uid");
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
    mDocumentList.setAdapter(adapter);

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(UID.get()))
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        document -> {
          if (document.getImages().size() > 0){
            for (RImage image : document.getImages()) {

              RImageEntity img = (RImageEntity) image;

              Timber.tag(TAG).i("image " + img.getMd5() );
              adapter.add( img );
            }
          }
        });


    mDocumentList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Image image = (Image) mDocumentList.getItemAtPosition(position);
        IMAGE = image;
        Timber.tag(TAG).i( " setOnItemClickListener " + image.getPath() );

        File file = new File(mContext.getFilesDir(), image.getMd5()+"_"+image.getTitle());

        pdfView
          .fromFile(file)
          .enableSwipe(true)
          .enableDoubletap(true)
          .defaultPage(0)
          .swipeHorizontal(false)
//          .onDraw(onDrawListener)
//          .onLoad(onLoadCompleteListener)
          .onPageChange((page, pageCount) -> {
            updatePageInfo();
          })
//          .onPageScroll(onPageScrollListener)
//          .onError(onErrorListener)
          .enableAnnotationRendering(false)
          .password(null)
          .scrollHandle(null)
          .load();


//        pdfViewPager = new PDFViewPager(mContext, file.getAbsolutePath() );

//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
//        jobManager.addJobInBackground( new DownloadFileJob(HOST.get(), image.getPath(), image.getMd5()+"_"+image.getTitle()) );

      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    index = 0;
    if (null != savedInstanceState) {
      index = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, 0);
    }


//    mAttacher = new PhotoViewAttacher(mImageView);
//    mAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
//    mAttacher.setOnDoubleTapListener(this);

    return view;
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
    mButtonPrevious.setEnabled(0 != index);
    mButtonNext.setEnabled(index + 1 < pageCount);
  }

  private void openRenderer(Context context, String file) throws IOException {
    if (file != null){
      Timber.tag(TAG).i( "PATH " + file );

      File fd = new File(file);
      mFileDescriptor = ParcelFileDescriptor.open( fd ,ParcelFileDescriptor.MODE_READ_ONLY);
      mPdfRenderer = new PdfRenderer(mFileDescriptor);

      setDocumentPreview(index);
    }
  }

  private void closeRenderer() throws IOException {
    if (null != mCurrentPage) {
      mCurrentPage.close();
    }
    if (mPdfRenderer != null){
      mPdfRenderer.close();
    }
    if (mFileDescriptor != null){
      mFileDescriptor.close();
    }
  }

  public int getPageCount() {
    return mPdfRenderer.getPageCount();
  }

  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  @OnClick(R.id.pdf_previous)
  public void previousPage(View view) {
    try {
//      setDocumentPreview(mCurrentPage.getIndex() - 1);
      pdfView.jumpTo( pdfView.getCurrentPage() - 1 );
      updatePageInfo();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

  }

  @OnClick(R.id.pdf_next)
  public void nextPage(View view) {
    try {
      pdfView.jumpTo( pdfView.getCurrentPage() + 1 );
      updatePageInfo();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
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

  private void updatePageInfo(){
    if ( pdfView != null ){
      pageInfo.setText( String.format(" Стр. %s/%s ", pdfView.getCurrentPage()+1 , pdfView.getPageCount()) );
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    try {
      closeRenderer();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }

//    mAttacher.cleanup();
//    mAttacher = null;
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

    if (!Objects.equals(event.path, "")){
      try {
//        doc_tmp_layout.setVisibility(View.GONE);
//        mImageView.setVisibility(View.VISIBLE);
//        progressBar.setVisibility(View.GONE);

        openRenderer( getActivity(), event.path );
      } catch (IOException e) {
        e.printStackTrace();
//        doc_tmp_layout.setVisibility(View.VISIBLE);
//        mImageView.setVisibility(View.GONE);
//        progressBar.setVisibility(View.GONE);
      }
    }
  }


}
