package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.events.decision.AddDecisionTemplateEvent;
import sapotero.rxtest.managers.menu.OperationManager;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.adapters.DecisionTemplateRecyclerAdapter;
import sapotero.rxtest.views.adapters.decorators.DividerItemDecoration;
import timber.log.Timber;

public class DecisionTemplateFragment extends Fragment {

  @Inject ISettings settings;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject OperationManager operationManager;

  private OnListFragmentInteractionListener mListener;
  private DecisionTemplateRecyclerAdapter adapter;
  private String TAG = this.getClass().getSimpleName();
  private String DECISION = "decision";

  public DecisionTemplateFragment() {
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_decision_template, container, false);

    EsdApplication.getManagerComponent().inject( this );

    initEvents();
    populateAdapter(view);
    initToolbar(view);

    return view;
  }

  private void initEvents() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);
  }

  private void initToolbar(View view) {
    Toolbar toolbar = (Toolbar) view.findViewById(R.id.fragment_decision_template_toolbar);

    toolbar.inflateMenu(R.menu.fragment_decision_template_menu);
    toolbar.setTitle("Шаблоны резолюции");

    toolbar.setOnMenuItemClickListener(item -> {
      switch (item.getItemId()){
        case R.id.fragment_decision_template_add:
          addDecisionTemplateDialog();
          break;
        case R.id.fragment_decision_template_refresh:
          refresh();
          break;
        default:
          break;
      }
      return false;
    });

  }

  private void addDecisionTemplateDialog() {
    MaterialDialog add_dialog = new MaterialDialog.Builder(getContext())
      .title(R.string.fragment_decision_template_add)
      .inputType(
        InputType.TYPE_CLASS_TEXT
          | InputType.TYPE_TEXT_FLAG_MULTI_LINE
          | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
          | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT)
      .input(R.string.fragment_decision_template_add_hint, R.string.dialog_empty_value,
        (dialog, input) -> {
          Timber.tag("ADD").e("asd");

          if (input != null && input.length() > 0) {
            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
          }
        })
      .negativeText(R.string.constructor_close)
      .onNegative((dialog, which) -> {
        Timber.tag("-").e("asd");
      })
      .positiveText(R.string.constructor_save)
      .onPositive((dialog, which) -> {

        CommandFactory.Operation operation = CommandFactory.Operation.CREATE_DECISION_TEMPLATE;
        CommandParams params = new CommandParams();
        params.setComment( dialog.getInputEditText().getText().toString() );
        params.setLabel( DECISION );
        operationManager.execute(operation, params);

      })
      .alwaysCallInputCallback()
      .build();

    add_dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
    add_dialog.show();
  }

  private void refresh() {

  }

  private void populateAdapter(View view) {

    Context context = view.getContext();
    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.fragment_decision_template_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(context));
    recyclerView.addItemDecoration( new DividerItemDecoration(ContextCompat.getDrawable(context, R.drawable.devider)));

    adapter = new DecisionTemplateRecyclerAdapter(new ArrayList<>(), mListener);
    recyclerView.setAdapter(adapter);

    invalidateDecisions();
  }

  private void invalidateDecisions() {
    dataStore
      .select(RTemplateEntity.class)
      .where(RTemplateEntity.USER.eq( settings.getLogin() ))
      .and(RTemplateEntity.TYPE.eq(DECISION))
      .get()
      .toObservable()
      .toList()
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        templates -> {
          Timber.tag(TAG).e("templates: %s", templates);
          if (templates.size() > 0) {
            adapter.addList( templates );
          }
        },
        error -> {
          Timber.tag(TAG).e(error);
        }
      );
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnListFragmentInteractionListener) {
      mListener = (OnListFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(AddDecisionTemplateEvent event){
    invalidateDecisions();
  }

  public interface OnListFragmentInteractionListener {
    void onListFragmentInteraction(RTemplateEntity item);
  }
}
