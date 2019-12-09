package cn.xiaoman.library.android.rxandroidlive.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import cn.xiaoman.library.android.live.RxAndroidLive;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * @author jiechic
 */
public class ObservableNotRestartActivity extends BaseViewActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("ObservableNotRestart");
        Observable.interval(1, TimeUnit.SECONDS)
                .compose(RxAndroidLive.<Long>bindLifecycle(this, AndroidSchedulers.mainThread()))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addContent("Observable onSubscribe");
                    }

                    @Override
                    public void onNext(Long aLong) {
                        addContent("Observable onNext value " + aLong);

                    }

                    @Override
                    public void onError(Throwable e) {
                        addContent("Observable onError");

                    }

                    @Override
                    public void onComplete() {
                        addContent("Observable onComplete");
                    }
                });
    }
}
