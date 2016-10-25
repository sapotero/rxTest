package sapotero.rxtest.views.activities;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

class BindingHolder<B extends ViewDataBinding> extends RecyclerView.ViewHolder {

  protected final B binding;

  public BindingHolder(B binding) {
    super(binding.getRoot());
    this.binding = binding;
  }
}