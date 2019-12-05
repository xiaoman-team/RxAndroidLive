package cn.xiaoman.library.android.live.operators;

import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.Lifecycle.State;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

abstract class OpertorsAndroidLive<E extends Event> implements LifecycleObserver {

    private final LifecycleOwner lifecycleOwner;
    private boolean mActive;

    abstract void dispose();

    abstract void update();

    OpertorsAndroidLive(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
        if (this.lifecycleOwner.getLifecycle().getCurrentState() != State.DESTROYED) {
            this.lifecycleOwner.getLifecycle().addObserver(this);
        }
    }

    static boolean isActiveState(State state) {
        return state.isAtLeast(State.STARTED);
    }

    @OnLifecycleEvent(Event.ON_ANY)
    void onStateChange() {
        if (this.lifecycleOwner.getLifecycle().getCurrentState() == State.DESTROYED) {
            dispose();
            lifecycleOwner.getLifecycle().removeObserver(this);
        } else {
            activeStateChanged(isActiveState(lifecycleOwner.getLifecycle().getCurrentState()));
        }
    }

    void activeStateChanged(boolean newActive) {
        if (newActive != mActive) {
            mActive = newActive;
            considerNotify();
        }
    }

    protected void considerNotify() {
        if (mActive) {
            if (isActiveState(lifecycleOwner.getLifecycle().getCurrentState())) {
                update();
            }
        }
    }

}
