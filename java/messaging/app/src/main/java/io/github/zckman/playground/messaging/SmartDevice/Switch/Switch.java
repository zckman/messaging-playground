package io.github.zckman.playground.messaging.SmartDevice.Switch;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class Switch {
    private final BehaviorSubject<Boolean> subject = BehaviorSubject.createDefault(false);

    public void on() {
        subject.onNext(true);
    }

    public void off() {
        subject.onNext(false);
    }
}

