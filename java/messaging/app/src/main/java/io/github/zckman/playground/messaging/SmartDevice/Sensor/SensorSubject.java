package io.github.zckman.playground.messaging.SmartDevice.Sensor;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class SensorSubject<T extends Number> extends SensorImpl<T> implements Observer<Measurement<T>> {

    /**
     * Provides the {@link Observer} with the means of cancelling (disposing) the
     * connection (channel) with the {@link Observable} in both
     * synchronous (from within {@link Observer#onNext(Object)}) and asynchronous manner.
     *
     * @param d the {@link Disposable} instance whose {@link Disposable#dispose()} can
     *          be called anytime to cancel the connection
     * @since 2.0
     */
    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    /**
     * Provides the {@link Observer} with a new item to observe.
     * <p>
     * The {@link Observable} may call this method 0 or more times.
     * <p>
     * The {@code Observable} will not call this method again after it calls either {@link #onComplete} or
     * {@link #onError}.
     *
     * @param measurement the item emitted by the Observable
     */
    @Override
    public void onNext(@NonNull Measurement<T> measurement) {
        emitValue(measurement);
    }

    /**
     * Notifies the {@link Observer} that the {@link Observable} has experienced an error condition.
     * <p>
     * If the {@code Observable} calls this method, it will not thereafter call {@link #onNext} or
     * {@link #onComplete}.
     *
     * @param e the exception encountered by the Observable
     */
    @Override
    public void onError(@NonNull Throwable e) {
        for (Observer<? super Measurement<T>> observer : observers) {
            observer.onError(e);
        }
    }

    /**
     * Notifies the {@link Observer} that the {@link Observable} has finished sending push-based notifications.
     * <p>
     * The {@code Observable} will not call this method if it calls {@link #onError}.
     */
    @Override
    public void onComplete() {
        for (Observer<? super Measurement<T>> observer : observers) {
            observer.onComplete();
        }
    }
}
