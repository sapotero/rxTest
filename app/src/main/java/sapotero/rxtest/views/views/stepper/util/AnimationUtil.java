package sapotero.rxtest.views.views.stepper.util;

import android.animation.Animator;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewPropertyAnimator;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public final class AnimationUtil {

    @Retention(SOURCE)
    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @interface Visibility {}

    private static final int DEFAULT_DURATION = 300;

    private AnimationUtil() {
        throw new AssertionError("Please do not instantiate this class");
    }

    public static void fadeViewVisibility(@NonNull final View view, @Visibility final int visibility, boolean animate) {
        ViewPropertyAnimator animator = view.animate();
        animator.cancel();
        animator.alpha(visibility == View.VISIBLE ? 1 : 0)
                .setDuration(animate ? DEFAULT_DURATION : 0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animation) {
                        if (visibility == View.VISIBLE) {
                            view.setVisibility(visibility);
                        }
                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animation) {
                        if (visibility == View.INVISIBLE || visibility == View.GONE) {
                            view.setVisibility(visibility);
                        }
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animation) {
                        if (visibility == View.INVISIBLE || visibility == View.GONE) {
                            view.setVisibility(visibility);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animation) {
                    }
                }).start();
    }
}
