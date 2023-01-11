package io.github.zckman.playground.messaging.SmartDevice.Switch;

import io.reactivex.rxjava3.core.Observable;

public class SwitchableObservable<T> {

    private static class SwitchedValue<T> {
        private Boolean on;
        private T value;

        public SwitchedValue(Boolean on, T value) {
            this.on = on;
            this.value = value;
        }

        public Boolean isOn() {
            return on;
        }

        public T getValue() {
            return value;
        }
    }

    public static <T> Observable<T> create(Observable<T> source, Switch s) {
        return Observable.combineLatest(source, s.asObservable(), (x, on) -> new SwitchedValue<T>(on, x))
                .filter(sw -> sw.isOn())
                .map(sw -> sw.getValue());
    }
}