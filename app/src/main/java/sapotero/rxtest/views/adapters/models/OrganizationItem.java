package sapotero.rxtest.views.adapters.models;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import java.util.Objects;
import java.util.Random;

public class OrganizationItem {
  private int id;
  private String name;
  private Integer count;

  private String template = "%-4s %s";

  public OrganizationItem(String name, Integer count) {

    this.name = name;
    this.count = count;
  }

  public String getName() {
    return name;
  }

  public Integer getCount() {
    return count;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public String getTitle() {
    String title = name;
    if (Objects.equals(title, "")){
      title = "Без организации";
    }
    return title;
  }

  public CharSequence getCountForDialog() {

    // TODO: Remove this line. It is for testing only
    int count = new Random().nextInt(200);

    final SpannableStringBuilder countText = new SpannableStringBuilder( String.valueOf(count) );
    final StyleSpan bold = new StyleSpan(Typeface.BOLD);
    countText.setSpan(bold, 0, String.valueOf(count).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    return countText;
  }

  public CharSequence getTitleForDialog() {
    String titleText = getTitle();
//    int maxLength = 33;
//    if (titleText.length() > maxLength){
//      titleText = titleText.toString().substring(0, maxLength - 3) + "...";
//    }
    return titleText;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OrganizationItem)) return false;

    OrganizationItem that = (OrganizationItem) o;

    if (!getName().equals(that.getName())) return false;
    return getCount().equals(that.getCount());

  }

  @Override
  public int hashCode() {
    int result = getName().hashCode();
    result = 31 * result + getCount().hashCode();
    return result;
  }
}
