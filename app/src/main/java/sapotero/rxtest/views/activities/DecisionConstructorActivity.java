package sapotero.rxtest.views.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.views.adapters.models.UrgencyItem;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.views.EsdSelectView;
import timber.log.Timber;

public class DecisionConstructorActivity extends AppCompatActivity implements DecisionFragment.OnFragmentInteractionListener, DecisionPreviewFragment.OnFragmentInteractionListener {

  @Inject RxSharedPreferences settings;

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.button_add_decision) Button button_add_decision;

  @BindView(R.id.signup_text_input_job_category) EsdSelectView<UrgencyItem> editTextJobCategory;

  private String TAG = this.getClass().getSimpleName();
  private final DecisionManager manager = new DecisionManager(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_decision_constructor);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    toolbar.setTitle("Редактор резолюции ");
    toolbar.inflateMenu(R.menu.info_decision_constructor);
    toolbar.setNavigationOnClickListener(v ->{
      finish();
      }
    );


    manager.addPreview();

    Intent intent = getIntent();
    if (null != intent) {
      String data = intent.getStringExtra("decision");

      Gson gson = new Gson();
      Decision decisions = gson.fromJson(data, Decision.class);

      if ( decisions.getBlocks().size() > 0 ){
        for ( Block block: decisions.getBlocks() ) {
          manager.add( block );
        }
      }

    } else {
      manager.add(new Block());
    }




//    ArrayList<UrgencyItem> array = new ArrayList<UrgencyItem>();
//
//    UrgencyItem item1 = new UrgencyItem("label1", "value1");
//
//    array.add( new UrgencyItem("label", "value") );
//    array.add( item1 );
//
//    Timber.tag(TAG).i("  +++ BEFORE +++ "+String.valueOf(array.toArray()));
//    if ( array.contains(item1) ){
//      array.remove(item1);
//      Timber.tag(TAG).i("  +++ DELETED +++ ");
//      Timber.tag(TAG).i(String.valueOf(array));
//    } else {
//      Timber.tag(TAG).i("  --- DELETED --- ");
//      Timber.tag(TAG).i(String.valueOf(array));
//    }


    List<UrgencyItem> urgency = new ArrayList<>();

    urgency.add( new UrgencyItem( "Весьма срочно"    , "Весьма срочно" ) );
    urgency.add( new UrgencyItem( "Крайне срочно"    , "Крайне срочно" ) );
    urgency.add( new UrgencyItem( "Няшная срочность" , "Няшная срочность" ) );
    urgency.add( new UrgencyItem( "Очень срочно"     , "Очень срочно" ) );
    urgency.add( new UrgencyItem( "Срочно"           , "Срочно" ) );

    editTextJobCategory.setItems(urgency);
    editTextJobCategory.setOnItemSelectedListener(
      (item, selectedIndex) -> Timber.tag(TAG).v(String.valueOf(item))
    );


  }

  @OnClick(R.id.button_add_decision)
  public void submit(View view) {
    manager.add(new Block());
  }

  @Override
  public void onFragmentInteraction(Uri uri) {
  }

  public DecisionManager getDecisionManager(){
    return manager;
  }


  public class DecisionManager {

    private Context context;
    private ArrayList<DecisionFragment> fragments = new ArrayList<>();
    private int index = 1;

    DecisionManager(Context decisionConstructorActivity) {
      context = decisionConstructorActivity;
    }

    public void addPreview(){
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      DecisionPreviewFragment body = new DecisionPreviewFragment();
      transaction.add(R.id.decision_constructor_decision_preview, body );
      transaction.commit();
    }

    @Nullable
    public void add( Block block ) {
      try{
        FragmentTransaction decision_manager = getSupportFragmentManager().beginTransaction();

        DecisionFragment fragment = new DecisionFragment();

        Bundle bundle = new Bundle();
        bundle.putInt( "number", index++ );

        Gson gson = new Gson();
        bundle.putString( "block", gson.toJson(block) );

        fragment.setArguments(bundle);

        decision_manager.add(R.id.decisions_container, fragment );
        decision_manager.commit();

        fragments.add( fragment );
      } catch (Exception e){
        Timber.tag(TAG).e( e );
      }
    }

    public void remove( DecisionFragment fragment ){
      try{
        index--;
        fragments.remove( fragment );
        update();
      } catch (Exception e){
        Timber.tag(TAG).e( e );
      }
    }

    public void update(){
      int _index = 1;
      for (DecisionFragment item: fragments ) {
        item.setNumber( _index++ );
        Timber.tag(TAG).i( item.getDecision().toString() );
      }
    }

    public String getDate(){

      Calendar c = Calendar.getInstance();
      SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy г.", new Locale("ru"));
      return format.format(c.getTime());
    }
  }

}
