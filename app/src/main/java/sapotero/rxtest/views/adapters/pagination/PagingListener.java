package sapotero.rxtest.views.adapters.pagination;

import java.util.List;
import rx.Observable;

public interface PagingListener<T> {
  Observable<List<T>> onNextPage(int offset);
}