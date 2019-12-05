package cn.xiaoman.library.android.live.operators;

import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.Lifecycle.State;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.plugins.RxJavaPlugins;


/**
 * @author jiechic
 */
public final class ObservableAndroidLive<T> extends Observable<T> implements LifecycleObserver {
    /**
     * The source consumable Observable.
     */
    private final Observable<T> source;

    private final boolean mAutoRestart;

    private final LifecycleOwner mLifecycleOwner;


    public ObservableAndroidLive(Observable<T> source, boolean autoRestart, LifecycleOwner lifecycleOwner) {
        this.source = source;
        this.mAutoRestart = autoRestart;
        this.mLifecycleOwner = lifecycleOwner;
    }

    @Override
    protected void subscribeActual(Observer<? super T> t) {
        RestartObserver.subscribeActual(source, new AndroidLiveObservable<>(t, mLifecycleOwner), mAutoRestart, mLifecycleOwner);
    }

    /**
     * 上层激活类
     */
    static final class RestartObserver<T> implements Disposable, Observer<T>, LifecycleObserver {

        /**
         * The source consumable Observable.
         */
        private final Observable<T> source;

        private final boolean mAutoRestart;

        private final LifecycleOwner mLifecycleOwner;

        private Observer<? super T> mDownObserver;

        private boolean mActive = false;

        private Disposable sourceDispose;

        /**
         * 下层订阅是否已经取消，默认是未取消
         */
        boolean isDispose = false;

        /**
         * 是否启动下层订阅
         */
        boolean isSubscribe = false;

        private RestartObserver(Observable<T> source, Observer<? super T> downObserver, boolean autoRestart, LifecycleOwner lifecycleOwner) {
            this.source = source;
            this.mDownObserver = downObserver;
            this.mAutoRestart = autoRestart;
            this.mLifecycleOwner = lifecycleOwner;
        }

        private void init() {
            if (this.mLifecycleOwner.getLifecycle().getCurrentState() != State.DESTROYED) {
                this.mLifecycleOwner.getLifecycle().addObserver(this);
            }
            activeStateChanged(isActiveState(mLifecycleOwner.getLifecycle().getCurrentState()));
        }


        @OnLifecycleEvent(Event.ON_ANY)
        void onStateChange() {
            if (this.mLifecycleOwner.getLifecycle().getCurrentState() == State.DESTROYED) {
                mLifecycleOwner.getLifecycle().removeObserver(this);
                dispose();
            } else {
                activeStateChanged(isActiveState(mLifecycleOwner.getLifecycle().getCurrentState()));
            }
        }

        void activeStateChanged(boolean newActive) {
            //状态变化，才可以进入下层判定
            if (newActive != mActive) {
                mActive = newActive;
                considerNotify();
            }
        }

        /**
         * 状态变化，才会进入该方法
         * 1、如果没有订阅，或者需要重启订阅，则取消订阅在启动
         * 2、如果需要重订阅，那么在状态不可用时，取消订阅上层订阅
         */
        private void considerNotify() {
            if (mActive) {
                if (!isSubscribe || mAutoRestart) {
                    sourceDispose();
                    source.subscribe(this);
                }

            } else {
                if (mAutoRestart) {
                    sourceDispose();
                }
            }
        }

        static boolean isActiveState(State state) {
            return state.isAtLeast(State.STARTED) && state != State.CREATED;
        }


        /**
         * 下层订阅取消订阅
         * 1、取消上层订阅
         * 2、将订阅状态取消
         */
        @Override
        public void dispose() {
            sourceDispose();
            isDispose = true;
        }

        /**
         * 取消上层订阅
         */
        private void sourceDispose() {
            if (sourceDispose != null) {
                sourceDispose.dispose();
                sourceDispose = null;
            }
        }

        @Override
        public boolean isDisposed() {
            return isDispose;
        }

        /**
         * 下层没有取消订阅才往下传
         * 因为有重新激活订阅的要求，则上层传递一次订阅下来，只向下传递一次，并将当前对象作为Dispose传递下去，并获取上层Dispose对象用来取消上层订阅
         */
        @Override
        public void onSubscribe(Disposable d) {
            if (!isDisposed()) {
                sourceDispose = d;
                if (!isSubscribe) {
                    mDownObserver.onSubscribe(this);
                    isSubscribe = true;
                }
            }

        }

        @Override
        public void onNext(T o) {
            if (!isDisposed()) {
                mDownObserver.onNext(o);
            }
        }

        @Override
        public void onError(Throwable e) {
            if (!isDisposed()) {
                mDownObserver.onError(e);
            }
        }

        /**
         * 下层还未取消订阅，或者不需要重新激活的，则将onComplete事件向下传递
         */
        @Override
        public void onComplete() {
            if (!isDisposed() && !mAutoRestart) {
                mDownObserver.onComplete();
            }

        }


        static <T> void subscribeActual(Observable<T> source, Observer<? super T> downObserver, boolean autoRestart, LifecycleOwner lifecycleOwner) {
            (new RestartObserver<T>(source, downObserver, autoRestart, lifecycleOwner)).init();
        }
    }

    /**
     * 下层拦截类
     */
    static final class AndroidLiveObservable<T> extends OpertorsAndroidLive implements Observer<T> {

        /**
         * for upstream
         */
        private Disposable upstream;


        /**
         * for data onSubscribe
         */
        @NonNull
        private final Observer<? super T> downstream;

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

        /**
         * for data onComplete
         */
        private boolean isComplete = false;

        private volatile boolean done = false;


        public AndroidLiveObservable(Observer<? super T> downstream, LifecycleOwner lifecycleOwner) {
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
        public void onNext(T t) {
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
        public void onComplete() {
            if (done) {
                return;
            }
            mThrowable = null;
            isComplete = true;
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

            if (downstream != null && upstream != null && !downHasSubscribe) {
                downstream.onSubscribe(upstream);
                downHasSubscribe = true;
            }

            if (mLastVersion < mVersion) {
                mLastVersion = mVersion;
                if (mData != null) {
                    if (!done) {
                        downstream.onNext(mData);
                    }
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