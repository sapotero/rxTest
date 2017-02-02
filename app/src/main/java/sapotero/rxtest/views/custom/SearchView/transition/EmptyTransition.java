package sapotero.rxtest.views.custom.SearchView.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.transitionseverywhere.TransitionValues;
import com.transitionseverywhere.Visibility;
import com.transitionseverywhere.utils.ViewUtils;

class EmptyTransition extends Visibility {

    public EmptyTransition() {
    }

    @SuppressWarnings("SameParameterValue")
    private Animator createAnimation(@NonNull View view, int endAlpha) {
      return ObjectAnimator.ofFloat(view, ViewUtils.getAlphaProperty(), endAlpha);
    }

    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
      return createAnimation(view, 1);
    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, final View view, TransitionValues startValues, TransitionValues endValues) {
        return createAnimation(view, 1);
    }

}