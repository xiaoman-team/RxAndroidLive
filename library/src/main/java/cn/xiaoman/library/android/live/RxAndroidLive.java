package cn.xiaoman.library.android.live;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import org.reactivestreams.Publisher;

import cn.xiaoman.library.android.live.operators.CompletableAndroidLive;
import cn.xiaoman.library.android.live.operators.FlowableAndroidLive;
import cn.xiaoman.library.android.live.operators.MaybeAndroidLive;
import cn.xiaoman.library.android.live.operators.ObservableAndroidLive;
import cn.xiaoman.library.android.live.operators.SingleAndroidLive;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * @author jiechic
 */
public class RxAndroidLive<T> implements ObservableTransformer<T, T>,
        FlowableTransformer<T, T>,
        SingleTransformer<T, T>,
        MaybeTransformer<T, T>,
        CompletableTransformer {

    private final LifecycleOwner mLifecycleOwner;

    private Scheduler mScheduler;


    /**
     * only for Observable and Flowable
     */
    private final boolean mAutoRestart;

    private RxAndroidLive(LifecycleOwner owner, boolean autoRestart,
                          Scheduler mScheduler) {
        this.mLifecycleOwner = owner;
        this.mAutoRestart = autoRestart;
        this.mScheduler = mScheduler;
    }

    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        ObjectHelper.requireNonNull(mLifecycleOwner, "lifecycleOwner is null");
        Observable<T> result = RxJavaPlugins.onAssembly(new ObservableAndroidLive<T>(upstream, mAutoRestart, mLifecycleOwner));
        if (mScheduler != null) {
            return result.observeOn(mScheduler);
        }
        return result;
    }

    @Override
    public Publisher<T> apply(Flowable<T> upstream) {
        ObjectHelper.requireNonNull(mLifecycleOwner, "lifecycleOwner is null");
        Flowable<T> result = RxJavaPlugins.onAssembly(new FlowableAndroidLive<T>(upstream, mAutoRestart, mLifecycleOwner));
        if (mScheduler != null) {
            return result.observeOn(mScheduler);
        }
        return result;
    }

    @Override
    public MaybeSource<T> apply(Maybe<T> upstream) {
        ObjectHelper.requireNonNull(mLifecycleOwner, "lifecycleOwner is null");
        Maybe<T> result = RxJavaPlugins.onAssembly(new MaybeAndroidLive<T>(upstream, mLifecycleOwner));
        if (mScheduler != null) {
            return result.observeOn(mScheduler);
        }
        return result;
    }

    @Override
    public SingleSource<T> apply(Single<T> upstream) {
        ObjectHelper.requireNonNull(mLifecycleOwner, "lifecycleOwner is null");
        Single<T> result = RxJavaPlugins.onAssembly(new SingleAndroidLive<T>(upstream, mLifecycleOwner));
        if (mScheduler != null) {
            return result.observeOn(mScheduler);
        }
        return result;
    }

    @Override
    public CompletableSource apply(Completable upstream) {
        ObjectHelper.requireNonNull(mLifecycleOwner, "lifecycleOwner is null");
        Completable result = RxJavaPlugins.onAssembly(new CompletableAndroidLive(upstream, mLifecycleOwner));
        if (mScheduler != null) {
            return result.observeOn(mScheduler);
        }
        return result;
    }

    public static <T> RxAndroidLive<T> bindLifecycle(@NonNull LifecycleOwner owner) {
        return bindLifecycle(owner, false, null);
    }

    public static <T> RxAndroidLive<T> bindLifecycle(@NonNull LifecycleOwner owner, boolean autoRestart) {
        return bindLifecycle(owner, autoRestart, null);
    }

    public static <T> RxAndroidLive<T> bindLifecycle(@NonNull LifecycleOwner owner, @Nullable Scheduler scheduler) {
        ObjectHelper.requireNonNull(owner, "lifecycleOwner is null");
        return bindLifecycle(owner, false, scheduler);
    }

    public static <T> RxAndroidLive<T> bindLifecycle(@NonNull LifecycleOwner owner, boolean autoRestart, @Nullable Scheduler scheduler) {
        ObjectHelper.requireNonNull(owner, "lifecycleOwner is null");

        return new RxAndroidLive<>(owner, autoRestart, scheduler);
    }

}
