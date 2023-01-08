package io.github.zckman.playground.messaging.SmartDevice.Sensor;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.*;

public class SensorImpl<T extends Number> extends Observable<Measurement<T>> implements Sensor<T> {

    Measurement<T> lastMeasurement;

    final Set<Observer<? super Measurement<T>>> observers = new HashSet<>();

    @Override
    public Measurement<T> getLastMeasurement() {
        return lastMeasurement;
    }

    @Override
    protected void subscribeActual(@NonNull Observer<? super Measurement<T>> observer) {

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
        if (getLastMeasurement() != null && !disposable.isDisposed()) {
            observer.onNext(getLastMeasurement());
        }
    }

    public void emitValue(Measurement<T> value) {
        lastMeasurement = value;
        for (Observer<? super Measurement<T>> observer : observers) {
            observer.onNext(value);
        }
    }
}
