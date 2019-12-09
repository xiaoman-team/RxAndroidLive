package cn.xiaoman.library.android.rxandroidlive.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;

import cn.xiaoman.library.android.live.RxAndroidLive;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * @author jiechic
 */
public class MaybeActivity extends BaseViewActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Maybe");
        Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(MaybeEmitter<String> emitter) throws Exception {
//                emitter.onSuccess("Maybe");
                emitter.onComplete();
            }
        })
                .compose(RxAndroidLive.<String>bindLifecycle(this, AndroidSchedulers.mainThread()))
                .subscribe(new MaybeObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addContent("Maybe onSubscribe");
                    }

                    @Override
                    public void onSuccess(String s) {
                        addContent("Maybe onSuccess value " + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        addContent("Maybe onError  ");
                    }

                    @Override
                    public void onComplete() {
                        addContent("Maybe onComplete  ");
                    }
                });
    }
}
