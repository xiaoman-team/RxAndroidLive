package cn.xiaoman.library.android.live.operators;

import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.Lifecycle.State;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.plugins.RxJavaPlugins;


/**
 * @author jiechic
 */
public final class FlowableAndroidLive<T> extends Flowable<T> {
    /**
     * The source consumable Observable.
     */
    private final Flowable<T> source;

    private final boolean mAutoRestart;

    private final LifecycleOwner mLifecycleOwner;


    public FlowableAndroidLive(Flowable<T> source, boolean autoRestart, LifecycleOwner lifecycleOwner) {
        this.source = source;
        this.mAutoRestart = autoRestart;
        this.mLifecycleOwner = lifecycleOwner;
    }


    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        RestartFlowable.subscribeActual(source, new AndroidLiveFlowable<>(s, mLifecycleOwner), mAutoRestart, mLifecycleOwner);
    }


    /**
     * 上层激活类
     */
    static final class RestartFlowable<T> implements Subscription, Subscriber<T>, LifecycleObserver {

        /**
         * The source consumable Observable.
         */
        private final Flowable<T> source;

        private final boolean mAutoRestart;

        private final LifecycleOwner mLifecycleOwner;

        private Subscriber<? super T> mDownSubscriber;

        private volatile boolean mActive = false;

        private Subscription sourceSubscription;

        /**
         * 下层订阅是否已经取消，默认是未取消
         */
        boolean isCancel = false;

        /**
         * 剩余请求数
         */
        volatile long requestNum = 0L;


        /**
         * 是否启动下层订阅
         */
        boolean isSubscribe = false;

        private RestartFlowable(Flowable<T> source, Subscriber<? super T> downSubscriber, boolean autoRestart, LifecycleOwner lifecycleOwner) {
            this.source = source;
            this.mDownSubscriber = downSubscriber;
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
                    source.subscribe(this);
                    isSubscribe = true;
                }
            } else {
                if (mAutoRestart) {
                    //结束上层订阅
                    sourceCancel();
                }
            }
        }

        static boolean isActiveState(State state) {
            return state.isAtLeast(State.STARTED) && state != State.CREATED;
        }


        @Override
        public void request(long n) {
            synchronized (RestartFlowable.class) {
                requestNum += n;
            }
            if (sourceSubscription != null) {
                sourceSubscription.request(n);
            }
        }

        /**
         * 下层订阅取消订阅
         * 1、取消上层订阅
         * 2、将订阅状态取消
         */
        @Override
        public void cancel() {
            sourceCancel();
            isCancel = true;
        }

        /**
         * 取消上层订阅
         */
        private void sourceCancel() {
            if (sourceSubscription != null) {
                sourceSubscription.cancel();
                sourceSubscription = null;
            }
        }

        /**
         * 1、对上层发起订阅，同时，如果还没吧请求提交给下层，则交给下层
         * 2、若下层请求已经发起，并且在中断前还有数据为请求完成，则继续请求
         *
         * @param s
         */
        @Override
        public void onSubscribe(Subscription s) {
            if (!isCancel) {
                sourceSubscription = s;
                if (!isSubscribe) {
                    mDownSubscriber.onSubscribe(this);
                    isSubscribe = true;
                }
                if (requestNum > 0) {
                    sourceSubscription.request(requestNum);
                }
            }


        }

        @Override
        public void onNext(T t) {
            if (mDownSubscriber != null) {
                synchronized (RestartFlowable.class) {
                    requestNum -= 1;
                }
                mDownSubscriber.onNext(t);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (mDownSubscriber != null) {
                mDownSubscriber.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (mDownSubscriber != null) {
                mDownSubscriber.onComplete();
            }
        }


        static <T> void subscribeActual(Flowable<T> source, Subscriber<? super T> downObserver, boolean autoRestart, LifecycleOwner lifecycleOwner) {
            (new RestartFlowable<T>(source, downObserver, autoRestart, lifecycleOwner)).init();
        }

    }


    static final class AndroidLiveFlowable<T> extends OpertorsAndroidLive implements Subscriber<T> {

        /**
         * for upstream
         */
        private Subscription upstream;

        /**
         * for Downstream
         */

        @NonNull
        private final Subscriber<? super T> downstream;

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

        public AndroidLiveFlowable(Subscriber<? super T> downstream, LifecycleOwner lifecycleOwner) {
            super(lifecycleOwner);
            this.downstream = downstream;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

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
            if (upstream != null) {
                upstream.cancel();
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