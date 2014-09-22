package javaconcurrency;

import java.util.Map;
import java.util.concurrent.*;

interface Computable<A, V> {
    V compute(A toCompute) throws InterruptedException;
}

/**
 * @param <A> to compute
 * @param <V> maybe A not cached, maybe A cached and in progress, maybe A cached and computed
 */
class Memorizer<A, V> implements Computable<A, V> {
    private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<A, Future<V>>();
    private final Computable<A, V> computator;

    public Memorizer(Computable<A, V> computator) {
        this.computator = computator;
    }

    @Override
    public V compute(final A toCompute) throws InterruptedException {
        while (true) {
            Future<V> maybeComputed = cache.get(toCompute);
            if (maybeComputed == null) {
                Callable<V> eval = new Callable<V>() {
                    @Override
                    public V call() throws InterruptedException {
                        return computator.compute(toCompute);
                    }
                };
                FutureTask<V> maybeComputedFutureTask = new FutureTask<V>(eval);
                // atomic opp
                cache.putIfAbsent(toCompute, maybeComputedFutureTask);
                if (maybeComputed == null) {
                    maybeComputed = maybeComputedFutureTask;
                    maybeComputedFutureTask.run();
                }
            }
            try {
                return maybeComputed.get();
            } catch (ExecutionException ex) {
                throw new RuntimeException();
            }
        }
    }
}

/**
 * The problem with Memorizer2 is that if one thread starts an expensive computation, other threads are not aware that
 * the computation is in progress and so may start the same computation.
 * <p/>
 * check-then-act race condition.
 *
 * @param <A> to compute
 * @param <V> A not cached, A cached
 */
class Memorizer2<A, V> implements Computable<A, V> {
    private final Map<A, V> cache = new ConcurrentHashMap<A, V>();
    private final Computable<A, V> computator;

    public Memorizer2(Computable<A, V> computator) {
        this.computator = computator;
    }

    @Override
    public V compute(A toCompute) throws InterruptedException {
        V computed = cache.get(toCompute);
        if (computed == null) {
            computed = computator.compute(toCompute);
            cache.put(toCompute, computed);
        }
        return computed;
    }
}

/**
 * It is possible for two threads to call compute with the same value roughly at the same time.
 * <p/>
 * check-then-act, specifically put-if-absent race condition.
 *
 * @param <A> to compute
 * @param <V> maybe A not cached, maybe A cached and in progress, maybe A cached and computed
 */
class Memorizer3<A, V> implements Computable<A, V> {
    private final Map<A, Future<V>> cache = new ConcurrentHashMap<A, Future<V>>();
    private final Computable<A, V> computator;

    public Memorizer3(Computable<A, V> computator) {
        this.computator = computator;
    }

    @Override
    public V compute(final A toCompute) throws InterruptedException {
        Future<V> maybeComputedFuture = cache.get(toCompute);
        if (maybeComputedFuture == null) {
            Callable<V> eval = new Callable<V>() {
                @Override
                public V call() throws InterruptedException {
                    return computator.compute(toCompute);
                }
            };
            FutureTask<V> maybeComputedFutureTask = new FutureTask<V>(eval);
            maybeComputedFuture = maybeComputedFutureTask;
            cache.put(toCompute, maybeComputedFuture);
            maybeComputedFutureTask.run();
        }
        try {
            return maybeComputedFuture.get();
        } catch (ExecutionException ex) {
            throw new RuntimeException();
        }
    }
}






