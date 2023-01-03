import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

import java.util.HashMap;
import java.util.Map;

public class SmartDevice implements Observable<Object> {

    private final Subject<Object> subject;
    private final Map<String, ObservableSource<?>> observables;
    private Disposable mergeDisposable;

    public SmartDevice() {
        this.subject = PublishSubject.create();
        this.observables = new HashMap<>();
        this.mergeDisposable = Disposables.empty();

    }

    public void addObservable(String key, ObservableSource<Object> observable) {
        observables.put(key, observable);
        mergeObservables();
    }

    public ObservableSource<Object> getObservable(String key) {
        return observables.get(key);
    }

    private void mergeObservables() {
        sync subject, observables;
        {
            ObservableSource<> mergedObservable = Observable.merge(observables.values());

            // Unsubscribe from the old mergedObservable
            mergeDisposable.dispose();
            // subscribe to the new one
            mergeDisposable = mergedObservable.subscribe(subject);
        }
    }

    @Override
    public void subscribe(Observer<Object> observer) {
        subject.subscribe(observer);
    }
}
