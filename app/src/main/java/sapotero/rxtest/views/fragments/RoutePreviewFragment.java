package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RStep;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.Person;
import timber.log.Timber;

public class RoutePreviewFragment extends Fragment {

  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.fragment_route_wrapper) LinearLayout wrapper;

  private Preference<String> DOCUMENT_UID;
  private OnFragmentInteractionListener mListener;



  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_route_preview, container, false);

    EsdApplication.getComponent( getContext() ).inject(this);
    ButterKnife.bind(view);

    loadSettings();
    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }

  private void loadSettings() {
    DOCUMENT_UID = settings.getString("main_menu.uid");

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( DOCUMENT_UID.get() ))
      .orderBy( RDocumentEntity.ROUTE_ID.asc() )
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(doc -> {
        Timber.tag("LOAD").e(" doc: %s ", doc.getUid() );

        if ( doc.getRoute() != null ){
          RRouteEntity route = (RRouteEntity) doc.getRoute();



          Timber.tag("ROUTE").e(" is: %s | %s - %s", route.getId(), route.getText(), route.getSteps().size() );



          if ( route.getSteps() != null && route.getSteps().size() > 0  ){


            HashMap< Integer, PanelBuilder > hashMap = new HashMap<>();

            for (RStep step : route.getSteps() ){
              RStepEntity r_step = (RStepEntity) step;


              PanelBuilder panel = new PanelBuilder(getContext()).withTitle( r_step.getTitle() );

              ArrayList<ItemBuilder> items = new ArrayList<ItemBuilder>();

              Boolean valid = false;

              Timber.tag("STEP").e(" %s | %s - %s", r_step.getId(), r_step.getNumber(), r_step.getTitle() );

              if ( r_step.getCards() != null ){
                Timber.tag("cards").e(" %s", r_step.getCards() );

                Card[] users = new Gson().fromJson( r_step.getCards(), Card[].class );

                for (Card card: users) {
                  valid = true;

                  if (card.getOriginalApproval() != null) {
                    ItemBuilder item = new ItemBuilder(getContext());

                    item.withName( card.getOriginalApproval() );
                    item.withAction( String.format("%s - %s", card.getUid(), card.getFullTextApproval()) );

                    items.add(item);
                  }
                }

              }
              if ( r_step.getPeople() != null ){
                Timber.tag("people").e(" %s", r_step.getPeople() );

                Person[] users = new Gson().fromJson( r_step.getPeople(), Person[].class );

                for (Person user: users){
                  valid = true;

                  if ( user.getOfficialId() != null && user.getOfficialName()!= null ) {
                    ItemBuilder item = new ItemBuilder(getContext());

                    Timber.tag("people").w("[ %s ] %s", user.getOfficialId(), user.getOfficialName() );

                    item.withName( user.getOfficialName());



                    if (user.getActions() != null && user.getActions().size() > 0) {
                      Timber.tag("people").w("%s - %s", user.getActions().get(0).getDate(), user.getActions().get(0).getComment() );
                      item.withAction( String.format( "%s - %s", user.getActions().get(0).getDate(), user.getActions().get(0).getStatus()  ) );
                    }
                    items.add( item );
                  }
                }



              }
              if ( r_step.getAnother_approvals() != null ){
                Timber.tag("another_approvals").e(" %s", r_step.getAnother_approvals() );
              }

              if (valid){
//
                panel.withItems( items );

                hashMap.put(Integer.valueOf(r_step.getNumber()), panel );
              }

            }

            if (hashMap.values().size() > 0){
              LinearLayout wrapper = (LinearLayout) getView().findViewById(R.id.fragment_route_wrapper);

              Map<Integer, PanelBuilder> map = new TreeMap<>(hashMap);

              for (PanelBuilder panel: map.values()){
                wrapper.addView( panel.build() );
              }

              for (Integer number: map.keySet()){
                Timber.tag("SORT").i( "ORDER: %s", number );
              }
            }

          }



        }




      });

  }

  class PanelBuilder{
    private final Context context;
    private String title;
    private ArrayList<ItemBuilder> items;
    private FrameLayout titleView;

    public PanelBuilder(Context context) {
      this.context = context;
    }

    public PanelBuilder withTitle(String title) {
      this.title = title;

      titleView = new FrameLayout(context);
      titleView.setBackground( ContextCompat.getDrawable( getContext() ,R.drawable.panel_builder_title) );

      TextView text = new TextView(context);
      text.setText(title);

      titleView.addView( text );

      return this;
    }
    public PanelBuilder withItems(ArrayList<ItemBuilder> items) {
      this.items = items;
      return this;
    }

    public LinearLayout build(){
      LinearLayout layout = new LinearLayout(context);
      layout.setOrientation(LinearLayout.VERTICAL);

      layout.setPadding(4,8,4,8);


      if (titleView != null) {
        layout.addView( titleView );
      }

      if (items != null && items.size() > 0) {
        LinearLayout itemsLayout = new LinearLayout(context);
        itemsLayout.setOrientation(LinearLayout.VERTICAL);
        itemsLayout.setBackground( ContextCompat.getDrawable( getContext() ,R.drawable.panel_builder_body) );

        for ( ItemBuilder item: items ){
          itemsLayout.addView( item.build() );
        }

        layout.addView(itemsLayout);
      }

      return layout;
    }

  }

  class ItemBuilder{
    private final Context context;

    private String name;
    private String action;
    private FrameLayout nameView;
    private FrameLayout actionView;

    public ItemBuilder(Context context) {
      this.context = context;
    }

    public ItemBuilder withName(String name) {
      this.name = name;

      nameView = new FrameLayout(context);

      TextView text = new TextView(context);
      text.setText(name);

      nameView.addView( text );

      return this;
    }

    public ItemBuilder withAction(String action) {
      this.action = action;

      actionView = new FrameLayout(context);


      TextView text = new TextView(context);
      text.setText(action);

      actionView.addView( text );


      return this;
    }

    public LinearLayout build(){
      LinearLayout layout = new LinearLayout(context);
      layout.setOrientation(LinearLayout.VERTICAL);
      layout.setBackground( ContextCompat.getDrawable( getContext() ,R.drawable.panel_builder_item) );
      layout.setPadding(4,4,4,4);

      if (name != null){
        layout.addView( nameView );
      }
      if (action != null){
        layout.addView( actionView );
      }

      return layout;
    }

  }


}
