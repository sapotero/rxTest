package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.BuildConfig;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.retrofit.DocumentLinkService;
import sapotero.rxtest.retrofit.models.DownloadLink;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.views.adapters.DocumentLinkAdapter;
import timber.log.Timber;
import uk.co.senab.photoview.PhotoViewAttacher;

public class InfoCardDocumentsFragment extends Fragment implements AdapterView.OnItemClickListener {

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;

  private OnFragmentInteractionListener mListener;
  private Context mContext;
  private Document document;
  private String TAG = this.getClass().getSimpleName();


  // for PDF
  private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";
  private ParcelFileDescriptor mFileDescriptor;

  private PdfRenderer mPdfRenderer;
  private PdfRenderer.Page mCurrentPage;

//  private ImageView mImageView;
//  private Button mButtonPrevious;
//  private Button mButtonNext;

  @BindView(R.id.pdf_image) ImageView mImageView;
  @BindView(R.id.pdf_previous) Button mButtonPrevious;
  @BindView(R.id.pdf_next) Button     mButtonNext;
  @BindView(R.id.documents_files) ListView mDocumentList;
  private int index;

  private PhotoViewAttacher mAttacher;


  public InfoCardDocumentsFragment() {
  }


  public static InfoCardDocumentsFragment newInstance(String param1, String param2) {
    InfoCardDocumentsFragment fragment = new InfoCardDocumentsFragment();

    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_documents, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getComponent(mContext).inject( this );

    Gson gson = new Gson();
    Preference<String> documentJson = settings.getString("document.images");

    Type listType = new TypeToken<ArrayList<Image>>(){}.getType();
    ArrayList<Image> documents = new Gson().fromJson(documentJson.get(), listType);


    Timber.tag(TAG).i( documents.toString() );

    DocumentLinkAdapter adapter = new DocumentLinkAdapter(mContext, documents);
    mDocumentList.setAdapter(adapter);

    mDocumentList.setOnItemClickListener(
      (parent, view1, position, id) -> {
        Image image = (Image) mDocumentList.getItemAtPosition(position);
        Timber.tag(TAG).i( " setOnItemClickListener " + image.getPath() );


        try{
          downloadFile( "http://mobile.sed.a-soft.org", image.getPath(), "", image.getMd5()+"_"+image.getTitle()  );
        }catch (Exception e){
          e.printStackTrace();
        }
      }
    );

    index = 0;
    if (null != savedInstanceState) {
      index = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, 0);
    }


    mAttacher = new PhotoViewAttacher(mImageView);

    // Lets attach some listeners, not required though!
    mAttacher.setOnMatrixChangeListener(new MatrixChangeListener());
    mAttacher.setOnPhotoTapListener(new PhotoTapListener());
    mAttacher.setOnSingleFlingListener(new SingleFlingListener());

    return view;
  }

  private void setDocumentPreview( int index ) {
    if (mPdfRenderer.getPageCount() <= index) {
      return;
    }

    if (null != mCurrentPage) {
      mCurrentPage.close();
    }

    mCurrentPage = mPdfRenderer.openPage(index);
    Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(), Bitmap.Config.ARGB_8888);
    mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
    mImageView.setImageBitmap(bitmap);
    updatePreview();
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
      setDocumentPreview(mCurrentPage.getIndex() - 1);
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  @OnClick(R.id.pdf_next)
  public void nextPage(View view) {
    try {
      setDocumentPreview(mCurrentPage.getIndex() + 1);
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

    try {
      openRenderer(getActivity(), null);
    } catch (IOException e) {
      e.printStackTrace();
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
    mListener = null;
    mAttacher.cleanup();
  }


  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Toast.makeText(getContext(), "test", Toast.LENGTH_SHORT).show();
  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }

  public void downloadFile(String host, String strUrl, String folderName, String fileName) {
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);

    try {
//      File dir = new File(Environment.getExternalStorageDirectory() + "/" + folderName);
//      File dir = new File(Environment.getExternalStorageDirectory().toString());
//
//      if (!dir.exists()) {
//        dir.mkdirs();
//      }

      String admin = settings.getString("login").get();
      String token = settings.getString("token").get();

      Uri builtUri = Uri.parse(host + strUrl)
        .buildUpon()
        .appendQueryParameter("login",      admin)
        .appendQueryParameter("auth_token", token)
        .build();

//      Timber.tag(TAG).i( " downloadFile " + builtUri.toString() );

      Retrofit retrofit = new RetrofitManager( getContext(), Constant.HOST, okHttpClient).process();
      DocumentLinkService documentLinkService = retrofit.create( DocumentLinkService.class );

      strUrl = strUrl.replace("?expired_link=1", "");
      Observable<DownloadLink> user = documentLinkService.getByLink( strUrl, admin, token, "1" );

      user.subscribeOn( Schedulers.newThread() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          link -> {

            Uri new_builtUri = Uri.parse(host + link.getExpiredLink())
              .buildUpon()
              .appendQueryParameter("login", admin)
              .appendQueryParameter("auth_token", token)
              .build();

              URL new_url = null;
              try {
                new_url = new URL(new_builtUri.toString());
              } catch (MalformedURLException e) {
                e.printStackTrace();
              }
            Timber.tag(TAG).d( "SUCCESS -> " + new_url );

            File file = new File(mContext.getFilesDir(), fileName);
            final URLConnection[] urlConnection = {null};
            URL finalNew_url = new_url;
            try {
              assert finalNew_url != null;
              urlConnection[0] = finalNew_url.openConnection();

              InputStream inputStream = urlConnection[0].getInputStream();
              BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

              byte[] data = new byte[1024];
              int current = 0;

              while ((current = bufferedInputStream.read(data,0,data.length)) != -1) {
                byteArrayOutputStream.write(data,0,current);
              }

              FileOutputStream fileOutputStream = new FileOutputStream(file);
              fileOutputStream.write(byteArrayOutputStream.toByteArray());
              fileOutputStream.flush();
              fileOutputStream.close();

              try {
                openRenderer(getActivity(), file.getAbsolutePath());
              }
              catch (  FileNotFoundException e) {
                e.printStackTrace();
              }


            } catch (IOException e) {
              e.printStackTrace();
            }




          },
          error -> {
            error.printStackTrace();
          }
        );

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private class PhotoTapListener implements PhotoViewAttacher.OnPhotoTapListener {

    @Override
    public void onPhotoTap(View view, float x, float y) {
      float xPercentage = x * 100f;
      float yPercentage = y * 100f;

      Log.d("onOutsidePhotoTap", String.format("%s %s", xPercentage, yPercentage));
    }

    @Override
    public void onOutsidePhotoTap() {
      Log.d("onOutsidePhotoTap", "You have a tap event on the place where out of the photo.");
    }
  }

  private class MatrixChangeListener implements PhotoViewAttacher.OnMatrixChangedListener {

    @Override
    public void onMatrixChanged(RectF rect) {
      Log.d("PhotoView", String.format("%s", rect.toString() ));
    }
  }

  private class SingleFlingListener implements PhotoViewAttacher.OnSingleFlingListener {

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      if (BuildConfig.DEBUG) {
        Log.d("PhotoView", String.format("%s %s", velocityX, velocityY));
      }
      return true;
    }
  }
}
