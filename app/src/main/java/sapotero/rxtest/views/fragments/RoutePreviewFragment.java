package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.retrofit.models.document.Action;
import sapotero.rxtest.retrofit.models.document.AnotherApproval;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.Person;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.activities.InfoNoMenuActivity;
import sapotero.rxtest.views.fragments.interfaces.PreviewFragment;
import timber.log.Timber;

public class RoutePreviewFragment extends PreviewFragment {

  @Inject ISettings settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.fragment_route_wrapper) LinearLayout linearLayoutWrapper;

  private String TAG = this.getClass().getSimpleName();
  private String uid;

  private State state;

  private ImageButton button;

  private FrameLayout card;
  private FrameLayout frame;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_route_preview, container, false);
    linearLayoutWrapper = (LinearLayout) view.findViewById(R.id.fragment_route_wrapper);
    button    = (ImageButton)  view.findViewById(R.id.route_preview_fragment_change_state);
    frame     = (FrameLayout)  view.findViewById(R.id.route_preview_fragment_frame_view);
    card      = (FrameLayout)  view.findViewById(R.id.route_preview_fragment_card_view);
    button.setOnClickListener(this::changeState);

    state  = State.LAST;

    EsdApplication.getDataComponent().inject(this);
    ButterKnife.bind(view);

    initEvents();

    //https://tasks.n-core.ru/browse/MPSED-2248
    //вызов loadRoute() не должен вызываться из onResume() текущего класса,
    //поскольку его вызывают в update() из InfoActivity.onResume()
    loadRoute();

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    showPreview(true);
    updateButtonView();

  }

  @Override
  public void update() {
    Timber.tag(TAG).d("update!");
    showPreview(true);
    loadRoute();
    updateButtonView();
  }



  private void changeState(View view){
    switch (state){
      case ALL:
        state = State.LAST;
        break;
      default:
        state = State.ALL;
        break;
    }

    showPreview(false);
    updateButtonView();
    loadRoute();
  }

  private void showPreview(Boolean force) {

    if (!force) {
      frame.setVisibility(View.VISIBLE);

      int durationMillis = 300;

      Animation fadeIn = new AlphaAnimation(0, 1);
      fadeIn.setInterpolator(new DecelerateInterpolator());
      fadeIn.setDuration(durationMillis);

      Animation fadeOut = new AlphaAnimation(1, 0);
      fadeOut.setInterpolator(new AccelerateInterpolator());
      fadeOut.setStartOffset(durationMillis);
      fadeOut.setDuration(durationMillis);

      AnimationSet animation = new AnimationSet(false);
      AnimationSet wrapperAnimation = new AnimationSet(false);

      wrapperAnimation.addAnimation(fadeIn);
      animation.addAnimation(fadeOut);

      frame.setAnimation(animation);
      linearLayoutWrapper.setAnimation(wrapperAnimation);

      Observable.just("")
        .delay(durationMillis, TimeUnit.MILLISECONDS)
        .subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data  -> {
            frame.setVisibility(View.GONE);
          },
          Timber::e
        );
    } else {
      frame.setVisibility(View.GONE);
    }

  }

  private void updateButtonView() {
    int fromDegrees;
    int toDegrees;

    switch (state){
      case ALL:
        fromDegrees = -180;
        toDegrees   = 0;
        break;
      default:
        fromDegrees = 0;
        toDegrees   = -180;
        break;
    }

    AnimationSet animSet = new AnimationSet(true);
    animSet.setInterpolator(new DecelerateInterpolator());
    animSet.setFillAfter(true);
    animSet.setFillEnabled(true);

    final RotateAnimation animRotate = new RotateAnimation(fromDegrees, toDegrees,
      RotateAnimation.RELATIVE_TO_SELF, 0.5f,
      RotateAnimation.RELATIVE_TO_SELF, 0.5f);

    animRotate.setDuration(300);
    animRotate.setFillAfter(true);
    animSet.addAnimation(animRotate);

    button.startAnimation(animSet);

  }

  public Fragment withUid(String uid) {
    this.uid = uid;
    return this;
  }

  private String getUid() {
    return uid == null ? settings.getUid() : uid ;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }
  }

  private void loadRoute() {

    RDocumentEntity doc = dataStore
      .select( RDocumentEntity.class )
      .where( RDocumentEntity.UID.eq( getUid()) )
      .orderBy( RDocumentEntity.ROUTE_ID.asc() )
      .get()
      .firstOrNull();

    if (doc != null) {
      if (linearLayoutWrapper != null) {
        linearLayoutWrapper.removeAllViews();
      }

      if ( doc.getRoute() != null && ((RRouteEntity) doc.getRoute()).getSteps() != null ) {
        Observable
          .from( ((RRouteEntity) doc.getRoute()).getSteps() )
          .map( rStep -> (RStepEntity) rStep )
          .filter(rStepEntity -> rStepEntity != null)
          .sorted( (e1, e2) -> e1.getNumber().compareTo(e2.getNumber()) )
          .map(PanelBuilder::new)
          .subscribeOn( Schedulers.computation() )
          .observeOn( AndroidSchedulers.mainThread() )
          .subscribe(
            panel -> {
              LinearLayout build = panel.build();
              if (build != null) {
                linearLayoutWrapper.addView(build);
              }
            },
            error -> {
              Timber.tag(TAG).e(error);
            }
          );
      }
    }
  }

  private enum PanelType {
    PEOPLE,
    CARD,
    APPROVAL
  };

  private enum State {
    ALL,
    LAST
  };

  class PanelBuilder{


    private final Context context;
    private final RStepEntity rStepEntity;
    private PanelType type;

    private ArrayList<ItemBuilder> items = new ArrayList<>();
    private LinearLayout titleView;
    private LinearLayout layout;

    PanelBuilder(RStepEntity RStepEntity) {
      this.context     = getContext();
      this.rStepEntity = RStepEntity;
      this.type = null;

      prebuild();
    }

    private void prebuild() {

      if ( rStepEntity != null ){
        if ( !Objects.equals(rStepEntity.getTitle(), "")){
          withTitle( rStepEntity.getTitle() );
        }
        setType();
      }
    }

    private void setType() {
      if (rStepEntity.getPeople() != null){
        type = PanelType.PEOPLE;
      }

      if (rStepEntity.getAnother_approvals() != null){
        type = PanelType.APPROVAL;
      }

      if (rStepEntity.getCards() != null){
        type = PanelType.CARD;
      }

      if (type != null) {
        switch (type){
          case PEOPLE:
            addPeople();
            break;
          case APPROVAL:
            addApproval();
            break;
          case CARD:
            addCard();
            break;
          default:
            break;
        }
      }
    }

    private void addApproval() {
      AnotherApproval[] anotherApprovals = new Gson().fromJson(rStepEntity.getAnother_approvals(), AnotherApproval[].class);

      for (AnotherApproval user : anotherApprovals) {

        if (user.getOfficialName() != null || user.getComment() != null) {
          ItemBuilder item = new ItemBuilder(getContext());

          item.withName(user.getOfficialName());

          if (user.getComment() != null) {
            item.withAction(user.getComment());
          }
          items.add(item);
        }
      }
    }

    private void addCard() {
      Card[] users = new Gson().fromJson(rStepEntity.getCards(), Card[].class);
      for (Card card : users) {

        if (card.getOriginalApproval() != null) {
          ItemBuilder item = new ItemBuilder(getContext());

          item.withNameCallback(card.getUid());
          item.withName(card.getFullTextApproval());

          item.withAction(card.getOriginalApproval());

          items.add(item);
        }
      }
    }

    private void addPeople() {
      Person[] users = new Gson().fromJson( rStepEntity.getPeople(), Person[].class);

      for (Person user : users) {

        if (user.getOfficialId() != null && user.getOfficialName() != null) {
          ItemBuilder item = new ItemBuilder(getContext());

          item.withName(user.getOfficialName());

          if (user.getSignPng() != null) {
            Timber.tag("SIGN+").e("assigned!");
            item.setWithSign();
          }


          if (user.getActions() != null && user.getActions().size() > 0) {
            Timber.tag("actions").w("%s", new Gson().toJson(user.getActions()));


            switch ( state ){
              case ALL:
                for (Action action : user.getActions()) {
                  if (action.getComment() != null) {
                    item.withAction( String.format("%s - %s\n%s", action.getDate(), action.getStatus(), action.getComment()) );
                  } else {
                    item.withAction(String.format("%s - %s", action.getDate(), action.getStatus()));
                  }
                }
                break;
              case LAST:
                Action action = user.getActions().get( user.getActions().size()-1 );

                if (action.getComment() != null) {
                  item.withAction(String.format("%s - %s\n%s", action.getDate(), action.getStatus(), action.getComment()));
                } else {
                  item.withAction(String.format("%s - %s", action.getDate(), action.getStatus()));
                }
                break;
              default:
                break;
            }



          }
          items.add(item);
        }
      }
    }


    PanelBuilder withTitle(String title) {
      titleView = new LinearLayout( context );

      TextView text = new TextView(context);
      text.setTextColor( ContextCompat.getColor(context, R.color.md_grey_600) );
      text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
      text.setTypeface( Typeface.create("sans-serif", Typeface.NORMAL) );
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

      if ( items.size() > 0 && type != null) {
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.setPadding(4, 8, 4, 8);


        if (titleView != null) {
          layout.addView(titleView);
        }


        LinearLayout itemsLayout = new LinearLayout(context);
        itemsLayout.setOrientation(LinearLayout.VERTICAL);


        for (ItemBuilder item : items) {
          itemsLayout.addView(item.build());
        }

        layout.addView(itemsLayout);


        View delimiter = new View(context);
        delimiter.setMinimumHeight(1);
        delimiter.setBackground(ContextCompat.getDrawable(context, R.color.md_grey_300));
        layout.addView(delimiter);
      }

      return layout;
    }

    public void clear() {
      if (layout != null) {
        layout.removeAllViews();
      }
    }

    public void add(ItemBuilder item) {
      items.add( item );
    }

    public ArrayList<ItemBuilder> getItems() {
      return items;
    }

    public RStepEntity getStep() {
      return rStepEntity;
    }
  }

  class ItemBuilder{
    private final Context context;

    private String name;
    private String action;
    private FrameLayout nameView;
    private LinearLayout actionView;
    private String uid;
    private boolean withSign;

    ItemBuilder(Context context) {
      this.context = context;
      actionView = new LinearLayout(context);
      actionView.setOrientation(LinearLayout.VERTICAL);
    }

    ItemBuilder withName(String name) {
      this.name = name;

      nameView = new FrameLayout(context);

      TextView text = new TextView(context);
      text.setTextColor( ContextCompat.getColor(context, R.color.md_grey_900) );
      text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
      text.setTypeface( Typeface.create("sans-serif", Typeface.NORMAL) );
      text.setText(name);

      nameView.addView( text );

      return this;
    }

    //refactor
    ItemBuilder withAction(String action) {
      this.action = action;
      int color = ContextCompat.getColor(context, R.color.md_grey_600);

      TextView text = new TextView(context);

      text.setTextColor( color );

      if (action.contains("На ") || action.contains("К ")){
        color = ContextCompat.getColor(context, R.color.md_blue_600);
      }

      if (action.contains("Отклонено")  || action.contains("Возвращен")){
        color = ContextCompat.getColor(context, R.color.md_red_600);
      }

      if (action.contains("Отправлен") || action.contains("Согласовано")|| action.contains("Подписано")  ){
        color = ContextCompat.getColor(context, R.color.md_green_600);
      }

      if (action.contains("передано")){
        color = ContextCompat.getColor(context, R.color.md_yellow_800);
      }

      text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
      text.setTypeface( Typeface.create("sans-serif", Typeface.NORMAL) );

      if (action.contains("\n")){
        final SpannableStringBuilder sb = new SpannableStringBuilder(action);

        final ForegroundColorSpan comment = new ForegroundColorSpan( ContextCompat.getColor(context, R.color.md_grey_600) );
        final ForegroundColorSpan status = new ForegroundColorSpan( color );
        sb.setSpan(status, 0, action.indexOf("\n"), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(comment, action.indexOf("\n"), action.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        text.setPadding(0,8,0,8);
        text.setText(sb);

      } else {
        text.setTextColor( color );
        text.setText(action);
      }


      actionView.addView( text );
      return this;
    }

    public LinearLayout build(){
      LinearLayout layout = new LinearLayout(context);
      layout.setOrientation(LinearLayout.VERTICAL);

      layout.setPadding(16,20,0,16);

      if (name != null){
        layout.addView( nameView );
      }
      if (action != null){
        layout.addView( actionView );
      }

      LinearLayout final_layout = new LinearLayout(context);
      layout.setOrientation(LinearLayout.VERTICAL);

      TextView icon = new TextView(context);
      icon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.user, 0, 0, 0);


      // resolved https://tasks.n-core.ru/browse/MVDESD-12651
      // В маршруте прохождения добавить изображение штампа подписи
      if( withSign ){
        icon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verified_user, 0, 0, 0);
      }

      icon.setPadding(0, action != null ? 32 : 20,0,0);

      final_layout.addView(icon);
      final_layout.addView(layout);

      if ( uid != null ){

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, typedValue, true);

        final_layout.setClickable(true);
        final_layout.setBackgroundResource(typedValue.resourceId);

        final_layout.setOnClickListener(v -> {
          settings.setImageIndex(0);
          Intent intent = new Intent(context, InfoNoMenuActivity.class);
          intent.putExtra( "UID", uid );
          intent.putExtra( "CARD", true );
          startActivity(intent);
        });
      }

      return final_layout;
    }

    void withNameCallback(String uid) {
      this.uid = uid;
    }

    void setWithSign() {
      withSign = true;
    }
  }


  private void initEvents() {
    Timber.tag(TAG).v("initEvents");
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCurrentDocumentEvent event) throws Exception {
    Timber.tag(TAG).w("UpdateCurrentDocumentEvent %s", event.uid);
    if (Objects.equals(event.uid, settings.getUid())){
      loadRoute();
    }
  }


}
