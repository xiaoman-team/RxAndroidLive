package cn.xiaoman.library.android.live.operators;

import androidx.lifecycle.LifecycleOwner;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.plugins.RxJavaPlugins;


/**
 * @author jiechic
 */
public final class SingleAndroidLive<T> extends Single<T> {
    /**
     * The source consumable Observable.
     */
    private final SingleSource<T> source;

    private final LifecycleOwner mLifecycleOwner;


    public SingleAndroidLive(SingleSource<T> source, LifecycleOwner lifecycleOwner) {
        this.source = source;
        this.mLifecycleOwner = lifecycleOwner;
    }

    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new AndroidLiveSingle<T>(observer, mLifecycleOwner));

    }


    static final class AndroidLiveSingle<T> extends OpertorsAndroidLive implements SingleObserver<T> {

        /**
         * for upstream
         */
        private Disposable upstream;

        /**
         * for Downstream
         */

        @NonNull
        private final SingleObserver<? super T> downstream;

        private volatile boolean downHasSubscribe = false;

        /**
         * for data onNext
         */
        private int mVersion = -1;

        private int mLastVersion = -1;

        @Nullable
        private T mData = null;

        /**
         * for data onError
         */

        @Nullable
        private Throwable mThrowable = null;

        private volatile boolean done = false;

        public AndroidLiveSingle(SingleObserver<? super T> downstream, LifecycleOwner lifecycleOwner) {
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
        public void onSuccess(T t) {
            if (done) {
                return;
            }
            ++mVersion;
            mData = t;
            mThrowable = null;
            considerNotify();
        }

        @Override
        public void onError(Throwable e) {
            if (done) {
                return;
            }
            mThrowable = e;
            mData = null;
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
            if (mLastVersion < mVersion) {
                mLastVersion = mVersion;
                if (mData != null) {
                    if (done) {
                        return;
                    }
                    downstream.onSuccess(mData);
                    done = true;
                }
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
        }
    }
}