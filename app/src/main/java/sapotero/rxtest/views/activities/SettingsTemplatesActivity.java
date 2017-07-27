package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.managers.menu.OperationManager;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.fragments.DecisionRejectionTemplateFragment;
import sapotero.rxtest.views.fragments.DecisionTemplateFragment;
import timber.log.Timber;

public class SettingsTemplatesActivity extends AppCompatActivity implements DecisionTemplateFragment.OnListFragmentInteractionListener, DecisionRejectionTemplateFragment.OnListFragmentInteractionListener {

  @Inject OperationManager operationManager;
  @Inject ISettings settings;

  @BindView(R.id.activity_settings_content_wrapper) LinearLayout wrapper;
  @BindView(R.id.toolbar) Toolbar toolbar;

  private String TAG = this.getClass().getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings_templates);

    ButterKnife.bind(this);
    EsdApplication.getManagerComponent().inject(this);



    initToolBar();

    initFragments();

  }

  private void initFragments() {
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

    fragmentTransaction.add( R.id.activity_settings_content_wrapper, new DecisionTemplateFragment() );
    fragmentTransaction.add( R.id.activity_settings_content_wrapper, new DecisionRejectionTemplateFragment() );

    fragmentTransaction.commit();
  }

  private void initToolBar() {
    toolbar.setTitle("Настройки приложения");
    toolbar.setSubtitle("Шаблоны резолюции/отклонения");
    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.setTitleTextColor( ContextCompat.getColor(this, R.color.md_grey_100) );
    toolbar.setSubtitleTextColor( ContextCompat.getColor(this, R.color.md_grey_400) );

    toolbar.setNavigationOnClickListener(v -> {
      finish();
      }
    );
  }


  @Override protected void onResume() {
    super.onResume();

  }

  @Override protected void onPause() {
    super.onPause();
  }

  @Override
  public void onListFragmentInteraction(RTemplateEntity item) {
    if (item != null) {
      MaterialDialog add_dialog = new MaterialDialog.Builder(this)
        .title(R.string.fragment_decision_template_edit)
        .inputType(
          InputType.TYPE_CLASS_TEXT
            | InputType.TYPE_TEXT_FLAG_MULTI_LINE
            | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
            | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT)
        .input("Введите текст резолюции", item.getTitle(),
          (dialog, input) -> {
            Timber.tag("ADD").e("input");
          })
        .negativeText(R.string.constructor_close)
        .onNegative((dialog, which) -> {
          Timber.tag("-").e("close");
        })
        .neutralText(R.string.constructor_delete)
        .onNeutral((dialog, which) -> {
          Timber.tag("-+").e("delete");

          CommandFactory.Operation operation = CommandFactory.Operation.DELETE_DECISION_TEMPLATE;
          CommandParams params = new CommandParams();
          params.setUser( settings.getLogin() );
          params.setToken( settings.getToken() );
          params.setUuid( item.getUid() );
          operationManager.execute(operation, params);

        })
        .positiveText(R.string.constructor_save)
        .onPositive((dialog, which) -> {

          CommandFactory.Operation operation = CommandFactory.Operation.UPDATE_DECISION_TEMPLATE;
          CommandParams params = new CommandParams();
          params.setUser( settings.getLogin() );
          params.setToken( settings.getToken() );
          params.setComment( dialog.getInputEditText().getText().toString() );
          params.setUuid( item.getUid() );
          operationManager.execute(operation, params);

        })
        .build();

      // у нас нет апдейта шаблонов в v2
      add_dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
      add_dialog.show();
    }
  }
}
