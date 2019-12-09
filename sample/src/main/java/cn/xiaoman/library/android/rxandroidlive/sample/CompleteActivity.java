package cn.xiaoman.library.android.rxandroidlive.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;

import cn.xiaoman.library.android.live.RxAndroidLive;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * @author jiechic
 */
public class CompleteActivity extends BaseViewActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Completable");
        Completable.complete()
                .compose(RxAndroidLive.<String>bindLifecycle(this, AndroidSchedulers.mainThread()))
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addContent("Completable onSubscribe");
                    }

                    @Override
                    public void onComplete() {
                        addContent("Completable onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        addContent("Completable onError");
                    }
                });
    }
}
