package sapotero.rxtest.views.custom.SearchView;

import com.transitionseverywhere.Transition;

abstract class SuggestionDismissListener implements Transition.TransitionListener {

    @Override
    public void onTransitionStart(Transition transition) {

    }

    @Override
    public void onTransitionEnd(Transition transition) {
        onSuggestionDismissed();
    }

    @Override
    public void onTransitionCancel(Transition transition) {

    }

    @Override
    public void onTransitionPause(Transition transition) {

    }

    @Override
    public void onTransitionResume(Transition transition) {

    }

    public abstract void onSuggestionDismissed();
}
