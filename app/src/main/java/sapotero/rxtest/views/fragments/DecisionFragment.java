package sapotero.rxtest.views.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.mapper.PerformerMapper;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.view.builders.BlockFactory;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.adapters.PrimaryConsiderationAdapter;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;
import sapotero.rxtest.views.dialogs.DecisionTextDialog;
import sapotero.rxtest.views.dialogs.SelectOshsDialogFragment;
import sapotero.rxtest.views.dialogs.SelectTemplateDialogFragment;
import timber.log.Timber;

public class DecisionFragment extends Fragment implements PrimaryConsiderationAdapter.Callback, SelectOshsDialogFragment.Callback, SelectTemplateDialogFragment.Callback {

  @Inject ISettings settings;
  @Inject Mappers mappers;

  @BindView(R.id.card_toolbar)  Toolbar  card_toolbar;
  @BindView(R.id.decision_text) EditText decision_text;
  @BindView(R.id.fragment_decision_record) ImageView speakButton;


  @BindView(R.id.fragment_decision_button_ask_to_acquaint) ToggleButton button_ask_to_acquaint;
  @BindView(R.id.fragment_decision_button_ask_to_report) ToggleButton button_ask_to_report;

  @BindView(R.id.fragment_decision_linear_people) LinearLayout people_view;

  @BindView(R.id.fragment_decision_hide_performers) CheckBox hide_performers;
  @BindView(R.id.fragment_decision_font_size) Spinner font_size;

  @BindView(R.id.fragment_decision_button_wrapper) LinearLayout buttons;
  @BindView(R.id.fragment_decision_text_before) ToggleButton fragment_decision_text_before;
  //  @BindView(R.id.decision_report_action) RadioGroup buttons;
  //  @BindView(R.id.head_font_selector) SpinnerWithLabel textSelector;
  //  @BindView(R.id.head_font_selector_wrapper) TextInputLayout head_font_selector_wrapper;


  public  static final int  RECOGNIZER_CODE = 777;
  private static final long SPEECH_RECOGNITION_DELAY = 300L;
  protected String mQuery = null;

  private String TAG = this.getClass().getSimpleName();
  private Context mContext;

  private int number;
  private Block block;
  private PrimaryConsiderationAdapter adapter;
  private SelectOshsDialogFragment oshs;
  private SelectTemplateDialogFragment templates;


  public Callback callback;
  private BlockFactory blockFactory;
  private boolean mSpeechRecognized = false;
  private int mSelection = -1;

  private boolean forReport   = false;
  private boolean forAcquaint = false;
  private int oldSize = 0;
  private int lastUpdate = -1;
  private String fontSize;

  private boolean scrollTo = false;

  public void setBlockFactory(BlockFactory blockFactory) {
    this.blockFactory = blockFactory;
  }

  public PrimaryConsiderationAdapter getPerformerAdapter() {
    return adapter;
  }

  public int getNumber() {
    return number;
  }

  public void dropAllOriginal() {
    Timber.tag("Block").i("number %s", block.getNumber());
    adapter.dropAllOriginal();
  }

  public interface Callback {
    void onUpdateSuccess(int number);
    void onUpdateError(Throwable error);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public DecisionFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle bundle = this.getArguments();
    if (bundle != null) {
      number = bundle.getInt("number", 0);

      Gson gson = new Gson();
      block = gson.fromJson( bundle.getString("block"), Block.class );
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_decision, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getDataComponent().inject( this );

    speakButton.setOnClickListener(v -> {
      Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
      startActivityForResult(intent, RECOGNIZER_CODE);
    });

    ArrayList<String> arrayStrings = new ArrayList<>();

    arrayStrings.add("8");
    arrayStrings.add("9");
    arrayStrings.add("10");
    arrayStrings.add("11");
    arrayStrings.add("12");
    arrayStrings.add("13");

    ArrayAdapter<String> tmp_adapter = new ArrayAdapter<>(mContext, R.layout.simple_spinner_item, arrayStrings);
//    font_size.setAdapter(new HintSpinnerAdapter( tmp_adapter, R.layout.hint_row_item, mContext));
    font_size.setAdapter(tmp_adapter);
    font_size.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        block.setFontSize( arrayStrings.get(i) );
        fontSize = arrayStrings.get(i);
        if (callback != null) {
          callback.onUpdateSuccess(lastUpdate);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    Timber.d("FONT_SIZE: %s", block.getFontSize());

    if (block.getFontSize() != null){
      for (int i = 0; i < arrayStrings.size(); i++) {
        String size = arrayStrings.get(i);
        if (Objects.equals(size, block.getFontSize())){
          font_size.setSelection(i);
          break;
        }
      }
    }

    card_toolbar.inflateMenu(R.menu.fragment_decision_menu);
    card_toolbar.setTitle("Блок " + number );
    decision_text.setText( block.getText() );

    // Disable EditText scrolling
    decision_text.setMovementMethod(null);

    decision_text.setOnClickListener(v -> {
      String title = getString(R.string.decision_text);
      new DecisionTextDialog(mContext, decision_text, title, title).show();
    });

    decision_text.addTextChangedListener(new TextWatcher() {

      public void onTextChanged(CharSequence s, int start, int before, int count) {
        Timber.tag(TAG).v( "onTextChanged" );

        if (callback != null) {
          callback.onUpdateSuccess(lastUpdate);
        }
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void afterTextChanged(Editable s) {
      }
    });

    card_toolbar.setOnMenuItemClickListener(
      item -> {

        switch ( item.getItemId() ){
          case R.id.decision_card_action_delete:

            if (blockFactory != null) {
              blockFactory.remove(this);

              if (callback != null) {
                callback.onUpdateSuccess(lastUpdate);
              }
            }
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            break;
          case R.id.decision_card_user_add:
            showAddOshsDialog();
            break;
          default:
            break;
        }

        getActivity().getSupportFragmentManager().popBackStack();
        return false;
      });

//    Boolean familirization = false;
//    if (block.getToFamiliarization() != null) {
//      familirization = block.getToFamiliarization();
//    }
//
//    Boolean is_responsible = false;
//    if (block.getToCopy() != null) {
//      is_responsible = block.getToCopy();
//    }


    if (block.getAppealText() != null) {
      forReport   = block.getAppealText().contains("дол");
      forAcquaint = block.getAppealText().contains("озн");
    }

    Timber.tag(TAG).v( " appeal text: %s | %s %s", block.getAppealText(),forReport, forAcquaint );

    button_ask_to_acquaint.setChecked( forAcquaint );
    button_ask_to_acquaint.setOnClickListener(v -> {
      Timber.tag(TAG).v( "button_ask_to_acquaint ++" );
      forReport = false;
      forAcquaint = ((ToggleButton) v).isChecked();
      updateAppeal();
    });

    button_ask_to_report.setChecked( forReport );
    button_ask_to_report.setOnClickListener(v -> {
      Timber.tag(TAG).v( "button_ask_to_report ++" );
      forReport = ((ToggleButton) v).isChecked();
      forAcquaint = false;
      updateAppeal();
    });

    Boolean hide = false;
    if (block.getHidePerformers() != null) {
      hide = block.getHidePerformers();
    }

    hide_performers.setChecked( hide );
    hide_performers.setOnCheckedChangeListener(
      (buttonView, isChecked) -> {
        if (callback != null) {
          callback.onUpdateSuccess(lastUpdate);
        }
      }
    );


    // настройка
    // Не отображать кнопки «Прошу доложить» и «Прошу ознакомить»
    if (settings.isHideButtons()){
      button_ask_to_acquaint.setVisibility(View.GONE);
      button_ask_to_report.setVisibility(View.GONE);
      buttons.setVisibility(View.GONE);
    }
    Timber.e(" ArrayList<PrimaryConsiderationPeople> people = new ArrayList<>(); ");



    ArrayList<PrimaryConsiderationPeople> people = new ArrayList<>();

    if (block.getPerformers().size() > 0){
      for ( Performer u: block.getPerformers() ) {
        Timber.tag(TAG).w("USER: %s [ %s | %s ]", u.getPerformerText(), u.getIsOriginal(), u.getIsResponsible() );
        Timber.tag(TAG).w("USER: %s ", new Gson().toJson(u) );
        PrimaryConsiderationPeople user =
                (PrimaryConsiderationPeople) mappers.getPerformerMapper().convert(u, PerformerMapper.DestinationType.PRIMARYCONSIDERATIONPEOPLE);

        people.add(user);
      }
    }

    if (block != null && block.getTextBefore() != null){
      fragment_decision_text_before.setChecked( block.getTextBefore() );
    }

    adapter = new PrimaryConsiderationAdapter( getContext(), people);
    adapter.registerCallBack(this);
    addUsersToView();

    return view;
  }



  private void updateAppeal() {
    block.setAskToReport(forReport);
    block.setAskToAcquaint(forAcquaint);
    button_ask_to_report.setChecked(forReport);
    button_ask_to_acquaint.setChecked(forAcquaint);

    String appealText = "";
    if (forReport) {
      appealText = button_ask_to_report.getTextOn().toString();
    } else if (forAcquaint) {
      appealText = button_ask_to_acquaint.getTextOn().toString();
    }
    block.setAppealText(appealText);

    if (callback != null) {
      callback.onUpdateSuccess(lastUpdate);
    }
  }

  private void updateUsers(){

    if ( adapter.getCount() > 0 ) {
      if ( oldSize != adapter.getCount() ){
        addUsersToView();
      }
      for ( PrimaryConsiderationPeople user : adapter.getAll()) {
        if (user.isOriginal()){
          lastUpdate = number;
        }
      }

    } else {
      lastUpdate = -1;
      people_view.removeAllViews();
      TextView empty_view = new TextView(getContext());
      empty_view.setText("Нет исполнителей");
      empty_view.setPadding(0,8,0,16);
      people_view.addView(empty_view);
      oldSize = 0;
    }

  }

  private void addUsersToView() {
    people_view.removeAllViews();
    oldSize = adapter.getCount();

    for (int i = 0; i < adapter.getCount(); i++) {
      View item = adapter.getView(i, null, people_view);
      people_view.addView(item);
    }
  }

  public Block getBlock(){
    Block block = new Block();

    if ( this.block != null ) {
      String appealText = "";
      if ( button_ask_to_report.isChecked()) {
        appealText = button_ask_to_report.getTextOn().toString();
      } else if ( button_ask_to_acquaint.isChecked()) {
        appealText = button_ask_to_acquaint.getTextOn().toString();
      }

      block.setId(this.block.getId());
      block.setNumber(number);
      block.setText( decision_text.getText().toString() );
      block.setAppealText( appealText );
      block.setTextBefore( fragment_decision_text_before.isChecked() );
      block.setHidePerformers( hide_performers.isChecked() );
      block.setToCopy(false);
      block.setToFamiliarization(false);
      block.setAskToAcquaint(forAcquaint);
      block.setAskToReport(forReport);

      if (fontSize == null ){
        block.setFontSize("13");
      } else {
        block.setFontSize(fontSize);
      }

      if ( adapter.getCount() > 0 ){
        ArrayList<Performer> performers = new ArrayList<>();

        for (int i = 0; i < adapter.getCount(); i++) {
          PrimaryConsiderationPeople item = adapter.getItem(i);
          Performer p = (Performer) mappers.getPerformerMapper().convert(item, PerformerMapper.DestinationType.PERFORMER);
          p.setNumber(i);
          performers.add(p);
        }

        block.setPerformers( performers );
      }
    }

    return block;
  }

  public void setNumber( int number){
    this.number = number;

    if (card_toolbar != null) {
      card_toolbar.setTitle("Блок " + number);
    }
  }

  private void showAddOshsDialog() {
    oshs = new SelectOshsDialogFragment();
    oshs.withSearch(true);

    //resolved https://tasks.n-core.ru/browse/MVDESD-13231 - убрать врио
    oshs.showWithAssistant(false);

    // resolved https://tasks.n-core.ru/browse/MVDESD-13440
    // Показывать организации в поиске исполнителей
    oshs.withOrganizations(true);

    oshs.registerCallBack( this );

    ArrayList<String> users = new ArrayList<>();

    // если есть люди из dialog как исполнители
    if ( adapter.getCount() > 0 ){
      for (PrimaryConsiderationPeople user: adapter.getAll()) {
        Timber.tag(TAG).e("user: %s", new Gson().toJson(user));
        users.add( user.getId() );
      }
    }

    // исключить подписанта из списка выбора исполнителей
    if (blockFactory != null) {
      String signerId = blockFactory.getDecision().getSignerId();
      if (signerId != null) {
        users.add(signerId);
      }

      String assistantId = blockFactory.getDecision().getAssistantId();
      if (assistantId != null) {
        users.add(assistantId);
      }
    }

    oshs.setIgnoreUsers( users );

    oshs.show( getActivity().getFragmentManager(), "SelectOshsDialogFragment");
  }

  @Override
  public void onResume() {
    super.onResume();

    if (mSpeechRecognized) {
      mSpeechRecognized = false;
      new Handler().postDelayed(() -> setQuery(mQuery, true), SPEECH_RECOGNITION_DELAY);
    }

    if ( scrollTo ) {
      View view = getView();

      if (view != null) {
        view.getParent().requestChildFocus(view, view);
      }
    }
  }
  public void setQuery(@NonNull String query, boolean submit) {
    this.mQuery = query;
    this.mSelection = query.length();

    if (decision_text == null) {
      return;
    }
    decision_text.setText(query);
    decision_text.setSelection(mSelection);

  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RECOGNIZER_CODE && resultCode == Activity.RESULT_OK) {
      List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      if (results != null && results.size() > 0) {
        mQuery = results.get(0) ;
        mSpeechRecognized = true;
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mContext = context;
  }

  @Override
  public void onRemove() {
    updateUsers();
    if (callback != null) {
      callback.onUpdateSuccess(lastUpdate);
    }
  }

  @Override
  public void onChange() {
    updateUsers();
    if (callback != null) {
      callback.onUpdateSuccess(lastUpdate);
    }
  }


  @Override
  public void onSearchSuccess(Oshs user, CommandFactory.Operation operation, String uid) {
    Timber.tag("FROM DIALOG").i( "[%s] %s | %s", user.getId(), user.getName(), user.getOrganization());

    PrimaryConsiderationPeople item =
            (PrimaryConsiderationPeople) mappers.getPerformerMapper().convert(user, PerformerMapper.DestinationType.PRIMARYCONSIDERATIONPEOPLE);

    adapter.add( item );
    updateUsers();

    if (callback != null) {
      callback.onUpdateSuccess(lastUpdate);
    }
  }

  @Override
  public void onSearchError(Throwable error) {

  }



  @OnClick(R.id.fragment_decision_button_get_template)
  public void template(){
    Timber.tag("ADD template").e("CLICKED");

    if (templates == null){
      templates = new SelectTemplateDialogFragment();
      templates.setType("decision");
      templates.registerCallBack( this );
    }

    templates.show( getActivity().getFragmentManager(), "SelectTemplateDialogFragment");
  }

  @OnClick(R.id.fragment_decision_button_add_people)
  public void add(){
    Timber.tag("ADD PEOPLE").e("CLICKED");

    showAddOshsDialog();
  }

  @OnClick(R.id.fragment_decision_text_before)
  public void text(){
    if (callback != null) {
      callback.onUpdateSuccess(lastUpdate);
    }
  }

  @Override
  public void onSelectTemplate(String template) {
    Timber.tag("ADD template").e("onSelectTemplate %s", template);
    decision_text.setText( template );

  }

  public void withScrollTo(boolean scrollTo) {
    this.scrollTo = scrollTo;
  }
}