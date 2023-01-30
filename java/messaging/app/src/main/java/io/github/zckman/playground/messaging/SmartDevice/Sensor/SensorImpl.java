package io.github.zckman.playground.messaging.SmartDevice.Sensor;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.*;

public class SensorImpl<T extends Number> extends Observable<Reading<T>> implements Sensor<T> {

    Reading<T> lastReading;

    final Set<Observer<? super Reading<T>>> observers = new HashSet<>();

    @Override
    public Reading<T> getLastReading() {
        return lastReading;
    }

    @Override
    protected void subscribeActual(@NonNull Observer<? super Reading<T>> observer) {

        Disposable disposable = new Disposable() {
            private boolean disposed = false;

            @Override
            public void dispose() {
                observers.remove(observer);
                disposed = true;
            }

            @Override
            public boolean isDisposed() {
                return disposed;
            }
        };
        observers.add(observer);
        observer.onSubscribe(disposable);
        if (getLastReading() != null && !disposable.isDisposed()) {
            observer.onNext(getLastReading());
        }
    }

    public void emitValue(Reading<T> value) {
        lastReading = value;
        for (Observer<? super Reading<T>> observer : observers) {
            observer.onNext(value);
        }
    }
}
