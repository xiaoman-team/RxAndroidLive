package cn.xiaoman.library.android.rxandroidlive.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import cn.xiaoman.library.android.live.RxAndroidLive;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * @author jiechic
 */
public class FlowableAutoRestartActivity extends BaseViewActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("FlowableAutoRestart");
        Flowable.interval(1, TimeUnit.SECONDS)
                .compose(RxAndroidLive.<Long>bindLifecycle(this, true, AndroidSchedulers.mainThread()))
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        addContent("Flowable onSubscribe");
                        final int requestNum = 10;
                        s.request(requestNum);
                        addContent("Flowable request " + requestNum);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        addContent("Flowable onNext value " + aLong);

                    }

                    @Override
                    public void onError(Throwable t) {
                        addContent("Flowable onError");

                    }

                    @Override
                    public void onComplete() {
                        addContent("Flowable onComplete");
                    }
                });
    }
}
