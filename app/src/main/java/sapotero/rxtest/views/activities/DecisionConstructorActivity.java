package sapotero.rxtest.views.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
import sapotero.rxtest.events.bus.UpdateDecisionPreviewEvent;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.views.adapters.OshsAutoCompleteAdapter;
import sapotero.rxtest.views.adapters.models.UrgencyItem;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.views.DelayAutoCompleteTextView;
import sapotero.rxtest.views.views.EsdSelectView;
import timber.log.Timber;

public class DecisionConstructorActivity extends AppCompatActivity implements DecisionFragment.OnFragmentInteractionListener, DecisionPreviewFragment.OnFragmentInteractionListener {

  @Inject RxSharedPreferences settings;

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.button_add_decision) Button button_add_decision;

  @BindView(R.id.fragment_decision_urgency_selector) EsdSelectView<UrgencyItem> urgencySelector;

  @BindView(R.id.fragment_decision_autocomplete_field) DelayAutoCompleteTextView user_autocomplete;
  @BindView(R.id.fragment_decision_autocomplete_field_loading_indicator) ProgressBar indicator;

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
    toolbar.setNavigationOnClickListener(v -> {
      finish();
      }
    );

    Decision raw_decision = null;
    Gson gson = new Gson();

    Intent intent = getIntent();

    if (null != intent) {
      String data = intent.getStringExtra("decision");
      raw_decision = gson.fromJson(data, Decision.class);

      Timber.tag(TAG).v( "getIntent ++" + raw_decision);
      if (raw_decision == null) {
        raw_decision = new Decision();
        raw_decision.setLetterhead("TEST");
        raw_decision.setShowPosition(true);
        raw_decision.setSignerPositionS("--");
        raw_decision.setSignerBlankText("---");
        raw_decision.setUrgencyText("URGENCY");
        raw_decision.setId("---");
        raw_decision.setDate("---");
        raw_decision.setBlocks(new ArrayList<>());
        Timber.tag(TAG).v( "raw_decision" + gson.toJson( raw_decision, Decision.class ) );
      }
    }

    manager.setDecision(raw_decision);
    manager.addPreview();

    if (raw_decision!= null && raw_decision.getBlocks().size() > 0) {
      for (Block block : raw_decision.getBlocks()) {
        manager.add(block);
      }
    } else {
      manager.add(new Block());
    }

    List<UrgencyItem> urgency = new ArrayList<>();

    urgency.add(new UrgencyItem("Весьма срочно", "Весьма срочно"));
    urgency.add(new UrgencyItem("Крайне срочно", "Крайне срочно"));
    urgency.add(new UrgencyItem("Няшная срочность", "Няшная срочность"));
    urgency.add(new UrgencyItem("Очень срочно", "Очень срочно"));
    urgency.add(new UrgencyItem("Срочно", "Срочно"));

    urgencySelector.setItems(urgency);
    urgencySelector.setOnItemSelectedListener(
      (item, selectedIndex) -> {
        Timber.tag(TAG).v(String.valueOf(item));
        manager.setUrgency( item.getLabel() );
      }
    );

    user_autocomplete.setThreshold(2);
    user_autocomplete.setAdapter( new OshsAutoCompleteAdapter(this) );
    user_autocomplete.setLoadingIndicator( indicator );
    user_autocomplete.setOnItemClickListener(
      (adapterView, view1, position, id) -> {
        Oshs user = (Oshs) adapterView.getItemAtPosition(position);
        user_autocomplete.setText( String.format("%s - %s", user.getName(), user.getOrganization() ) );
        manager.setSigner( user );
      }
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
    private int index = 0;
    private DecisionPreviewFragment body;
    private Decision decision;

    DecisionManager(Context decisionConstructorActivity) {
      context = decisionConstructorActivity;
    }

    void addPreview(){

      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      body = new DecisionPreviewFragment();

      if ( decision != null ){
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        bundle.putString("decision", gson.toJson(decision, Decision.class) );

        body.setArguments(bundle);
      }

      transaction.add(R.id.decision_constructor_decision_preview, body );
      transaction.commit();
    }

    public void add( Block block ) {
      try{
        FragmentTransaction decision_manager = getSupportFragmentManager().beginTransaction();

        DecisionFragment fragment = new DecisionFragment();

        Bundle bundle = new Bundle();
        bundle.putInt( "number", ++index );

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
        --index;
        fragments.remove( fragment );
        update();
      } catch (Exception e){
        Timber.tag(TAG).e( e );
      }
    }

    public void update(){
      int _index = 1;
      ArrayList<Block> blocks = new ArrayList<>();

      for (DecisionFragment item: fragments ) {
        item.setNumber( _index++ );
        Timber.tag(TAG).i( item.getBlock().toString() );
        blocks.add( item.getBlock() );
      }

      updateBlocks(blocks);
    }

    private void updateBlocks(ArrayList<Block> blocks) {
      body.setBlocks(blocks);
      body.getBlocksInfo();
    }

    public String getDate(){

      Calendar c = Calendar.getInstance();
      SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy г.", new Locale("ru"));
      return format.format(c.getTime());
    }

    void setDecision(Decision decision) {
      Timber.tag(TAG).v( "decision" + decision );
      this.decision = decision;
    }

    public void setSigner(Oshs user) {
      body.decision.setSigner( String.format("%s - %s", user.getName(), user.getOrganization()) );
      body.decision.setSignerId( user.getId() );
      body.decision.setSignerBlankText( user.getName() );
      body.decision.setSignerPositionS( user.getPosition() );
    }

    public void setUrgency(String urgency) {
      body.decision.setUrgencyText(urgency);
    }
  }

  @Override
  public void onStart() {
    super.onStart();

    if ( !EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().register(this);
    }
  }
  @Override protected void onPause() {
    super.onPause();

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
      EventBus.getDefault().register(this);
    }

  }

  @Override
  public void onStop() {
    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }
    super.onStop();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateDecisionPreviewEvent event) {
    manager.update();
  }

}
