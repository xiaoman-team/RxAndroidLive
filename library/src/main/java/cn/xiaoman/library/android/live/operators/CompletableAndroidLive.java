package cn.xiaoman.library.android.live.operators;


import androidx.lifecycle.LifecycleOwner;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.plugins.RxJavaPlugins;


/**
 * @author jiechic
 */
public final class CompletableAndroidLive extends Completable {
    /**
     * The source consumable Observable.
     */
    private final CompletableSource source;

    private final LifecycleOwner mLifecycleOwner;


    public CompletableAndroidLive(CompletableSource source, LifecycleOwner lifecycleOwner) {
        this.source = source;
        this.mLifecycleOwner = lifecycleOwner;
    }

    @Override
    protected void subscribeActual(CompletableObserver observer) {
        source.subscribe(new AndroidLiveCompletable(observer, mLifecycleOwner));
    }


    static final class AndroidLiveCompletable extends OpertorsAndroidLive implements CompletableObserver {

        /**
         * for upstream
         */
        private Disposable upstream;

        /**
         * for data onSubscribe
         */
        @NonNull
        private final CompletableObserver downstream;

        private volatile boolean downHasSubscribe = false;


        /**
         * for data onError
         */

        @Nullable
        private Throwable mThrowable = null;

        /**
         * for data onComplete
         */
        private boolean isComplete = false;

        private volatile boolean done = false;


        public AndroidLiveCompletable(CompletableObserver downstream, LifecycleOwner lifecycleOwner) {
            super(lifecycleOwner);
            this.downstream = downstream;
        }


        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                considerNotify();
            }
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            mThrowable = null;
            isComplete = true;
            considerNotify();
        }


        @Override
        public void onError(Throwable e) {
            if (done) {
                return;
            }
            mThrowable = e;
            considerNotify();
        }

        @Override
        void dispose() {
            if (upstream != null && !upstream.isDisposed()) {
                upstream.dispose();
            }
        }

        @Override
        void update() {
            if (upstream != null && !downHasSubscribe) {
                downstream.onSubscribe(upstream);
                downHasSubscribe = true;
            }

            if (mThrowable != null) {
                if (done) {
                    RxJavaPlugins.onError(mThrowable);
                    return;
                }
                done = true;
                downstream.onError(mThrowable);
                mThrowable = null;
            }


            if (isComplete) {
                if (done) {
                    return;
                }
                done = true;
                downstream.onComplete();
            }
        }
    }
}