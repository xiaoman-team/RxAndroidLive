package cn.xiaoman.library.android.rxandroidlive.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import cn.xiaoman.library.android.live.RxAndroidLive;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * @author jiechic
 */
public class SingleActivity extends BaseViewActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Single");
        Single.just("Single")
                .compose(RxAndroidLive.<String>bindLifecycle(this, AndroidSchedulers.mainThread()))
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addContent("Single onSubscribe");
                    }

                    @Override
                    public void onSuccess(String s) {
                        addContent("Single onSuccess value " + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        addContent("Single onError  ");
                    }
                });
    }
}
