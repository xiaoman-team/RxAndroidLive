# RxAndroidLive

[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Jcenter](https://img.shields.io/badge/%20Jcenter%20-1.0.0-5bc0de.svg)](https://bintray.com/xiaoman-team/maven/RxAndroidLive/_latestVersion)

[^_^]:[![Methods](https://img.shields.io/badge/%20Methods%20%7C%20Size%20-%20239%20%7C%2040%20KB-d9534f.svg)](http://www.methodscount.com/?lib=cn.xiaoman.library.android%3ARxAndroidLive%3A1.0.0)
[^_^]:[![Maven](https://img.shields.io/badge/%20Maven%20-1.0.0-5bc0de.svg)](https://mvnrepository.com/artifact/cn.xiaoman.library.android/RxAndroidLive/1.0.0)
[^_^]:[![Arsenal](https://img.shields.io/badge/%20Arsenal%20-%20RxService%20-4cae4c.svg?style=flat)](https://android-arsenal.com/details/1/6027)

An rxjava implementation of Livedata for Lifecycle cache and use Active or unActive

## Installation

Using *JCenter*:
```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'cn.xiaoman.library.android:rxandroidlive:1.0.0'
}
```

## Requirements

RxJava 2.1.0+
AndroidX Lifecycle 2.1.0+

## Usage

Just enable RxService as soon as possible. In Android, for example:

```kotlin

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
```

## Screenshot

![avatar](./Screenshot/Screenshot_2019-12-09-17-23-52-313.jpg)

![avatar](./Screenshot/Screenshot_2019-12-09-17-24-14-810.jpg)