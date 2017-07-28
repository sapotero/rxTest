package sapotero.rxtest.views.adapters.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;

public class FragmentBuilder extends Fragment {

  private ButtonBuilder button;

  @BindView(R.id.fragment_builder_title) TextView title;

  public FragmentBuilder setButton(ButtonBuilder button) {
    this.button = button;
    return this;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_builder, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getDataComponent().inject( this );

    setTitle();

    return view;
  }

  private void setTitle() {
    title.setText( button.getLabel() );
  }

  public Fragment build (){
    return this;
  }
}
