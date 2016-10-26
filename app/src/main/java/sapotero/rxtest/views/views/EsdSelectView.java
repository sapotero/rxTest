package sapotero.rxtest.views.views;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.List;

import sapotero.rxtest.views.adapters.utils.Listable;

public class EsdSelectView<T extends Listable> extends EditText {


  List<T> mItems;
  String[] mListableItems;
  CharSequence mHint;

  OnItemSelectedListener<T> onItemSelectedListener;

  public EsdSelectView(Context context) {
    super(context);

    mHint = getHint();

    setHint("Нажмите для выбора срочности");
  }

  public EsdSelectView(Context context, AttributeSet attrs) {
    super(context, attrs);

    mHint = getHint();
    setHint("Нажмите для выбора срочности");
  }

  public EsdSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    mHint = getHint();
    setHint("Нажмите для выбора срочности");
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    setFocusable(false);
    setClickable(true);
  }

  public void setItems(List<T> items) {
    this.mItems = items;
    this.mListableItems = new String[items.size()];

    int i = 0;

    for (T item : mItems) {
      mListableItems[i++] = item.getLabel();
    }

    configureOnClickListener();
  }

  private void configureOnClickListener() {
    setOnClickListener(view -> {
      AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
      builder.setTitle(mHint);
      builder.setItems(mListableItems, (dialogInterface, selectedIndex) -> {
        setText(mListableItems[selectedIndex]);

        if (onItemSelectedListener != null) {
          onItemSelectedListener.onItemSelectedListener(mItems.get(selectedIndex), selectedIndex);
        }
      });
      builder.setPositiveButton(android.R.string.cancel, null);
      builder.create().show();
    });
  }

  public void setOnItemSelectedListener(OnItemSelectedListener<T> onItemSelectedListener) {
    this.onItemSelectedListener = onItemSelectedListener;
  }

  public interface OnItemSelectedListener<T> {
    void onItemSelectedListener(T item, int selectedIndex);
  }
}
