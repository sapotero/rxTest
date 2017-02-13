package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import sapotero.rxtest.views.activities.InfoNoMenuActivity;
import timber.log.Timber;

public class RoutePreviewFragment extends Fragment {

  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.fragment_route_wrapper) LinearLayout wrapper;

  private Preference<String> DOCUMENT_UID;
  private OnFragmentInteractionListener mListener;
  private String uid;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @RequiresApi(api = Build.VERSION_CODES.M)
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

  public Fragment withUid(String uid) {
    this.uid = uid;
    return this;
  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void loadSettings() {
    DOCUMENT_UID = settings.getString("activity_main_menu.uid");

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( uid == null? DOCUMENT_UID.get() : uid  ))
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

                    item.withNameCallback( card.getUid() );
                    item.withName( card.getFullTextApproval() );

//                    item.withAction( String.format("%s - %s", card.getUid(), card.getFullTextApproval()) );

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

                    item.withName( user.getOfficialName());

                    if ( user.getSignPng() != null ){
                      Timber.tag("SIGN+").e("assigned!");
                      item.withSign();
                    }


                    if (user.getActions() != null && user.getActions().size() > 0) {
                      Timber.tag("actions").w("%s", new Gson().toJson( user.getActions()));
                      item.withAction( String.format( "%s - %s", user.getActions().get(user.getActions().size()-1).getDate(), user.getActions().get(user.getActions().size()-1).getStatus()  ) );
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
    private LinearLayout titleView;

    public PanelBuilder(Context context) {
      this.context = context;
    }

    public PanelBuilder withTitle(String title) {
      this.title = title;

      titleView = new LinearLayout(context);
//      titleView.setBackground( ContextCompat.getDrawable( getContext() ,R.drawable.panel_builder_title) );

      TextView text = new TextView(context);
      text.setTextColor( ContextCompat.getColor(context, R.color.md_grey_300) );
      text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
      text.setText(title);
      text.setPadding(16,0,0,0);

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
//        itemsLayout.setBackground( ContextCompat.getDrawable( getContext() ,R.drawable.panel_builder_body) );

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
    private String uid;
    private boolean withSign = false;

    public ItemBuilder(Context context) {
      this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public ItemBuilder withName(String name) {
      this.name = name;

      nameView = new FrameLayout(context);

      TextView text = new TextView(context);
      text.setTextColor( context.getColor(R.color.md_grey_700) );
      text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

      text.setText(name);
//      text.setForeground( ContextCompat.getDrawable( getContext(), R.drawable.card_foreground ) );


      if (uid != null){

        Integer count = dataStore
          .count(RDocumentEntity.class)
          .where(RDocumentEntity.UID.eq(uid)).get().value();

        if ( count > 0){
          text.setTextColor( ContextCompat.getColor(getContext(), R.color.md_yellow_A400) );
          text.setOnClickListener(v -> {
            Toast.makeText( getContext(), "Go to Preview: "+uid, Toast.LENGTH_SHORT ).show();
            Intent intent = new Intent(context, InfoNoMenuActivity.class);
            intent.putExtra("UID", uid);
            startActivity(intent);
          });
        }
//        else {
//          Toast.makeText( getContext(), "NO DOCUMENT WITH UID "+uid, Toast.LENGTH_SHORT ).show();
//        }
      }


      nameView.addView( text );

      return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public ItemBuilder withAction(String action) {
      this.action = action;

      actionView = new FrameLayout(context);


      TextView text = new TextView(context);
      text.setTextColor( context.getColor(R.color.md_grey_400) );
      text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
      text.setText(action);

      actionView.addView( text );


      return this;
    }

    public LinearLayout build(){
      LinearLayout layout = new LinearLayout(context);
      layout.setOrientation(LinearLayout.VERTICAL);
//      layout.setBackground( ContextCompat.getDrawable( getContext() ,R.drawable.panel_builder_item) );
      layout.setPadding(16,20,0,20);

      if (name != null){
        layout.addView( nameView );
      }
      if (action != null){
        layout.addView( actionView );
      }
      if (withSign){
        View view = new View(context);
        view.setMinimumHeight(1);
        view.setBackground( ContextCompat.getDrawable(context, R.color.md_green_500) );
        layout.addView( view );
      }

//      View delimiter = new View(context);
//      delimiter.setMinimumHeight(1);
//      delimiter.setBackground( ContextCompat.getDrawable(context, R.color.md_grey_300) );
//      layout.addView(delimiter);

      return layout;
    }

    public void withNameCallback(String uid) {
      this.uid = uid;
    }

    public ItemBuilder withSign() {
      withSign = true;
      return this;
    }
  }


}
