package ZBRA.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import ZBRA.blockchain.Transaction;

public class CycleAssembler implements AutoCloseable {

    private static final Comparator<Transaction> CANONICAL_ORDER =
            Comparator.comparing(Transaction::getHash);

    private final List<SimulationEngine> engines;
    private final int expectedPartitions;
    private final ExecutorService pool;

    private final Map<Long, List<Transaction>> reorderBuffer = new HashMap<>();
    private final Map<Integer, Long> partitionHeads = new HashMap<>();
    private long nextCycle = -1;
    private String datasetHash;
    private boolean enginesInitialised = false;

    public CycleAssembler(List<SimulationEngine> engines, int expectedPartitions) {
        this.engines = engines;
        this.expectedPartitions = expectedPartitions;
        this.pool = Executors.newFixedThreadPool(Math.max(1, engines.size()), engineThreadFactory());
    }

    public void accept(int partition, long cycle, String datasetHash, Transaction tx) {
        if (this.datasetHash == null) {
            this.datasetHash = datasetHash;
        }
        reorderBuffer.computeIfAbsent(cycle, k -> new ArrayList<>()).add(tx);
        partitionHeads.merge(partition, cycle, Math::max);
        drainComplete();
    }

    public void markPartitionEof(int partition) {
        partitionHeads.put(partition, Long.MAX_VALUE);
        drainComplete();
    }

    @Override
    public void close() {
        pool.shutdownNow();
    }

    private void drainComplete() {
        if (partitionHeads.size() < expectedPartitions) {
            return;
        }
        long watermark = Collections.min(partitionHeads.values());

        long limit;
        if (watermark == Long.MAX_VALUE) {
            if (reorderBuffer.isEmpty()) {
                return;
            }
            limit = Collections.max(reorderBuffer.keySet()) + 1;
        } else {
            limit = watermark;
        }

        if (nextCycle < 0) {
            nextCycle = reorderBuffer.isEmpty() ? limit : Collections.min(reorderBuffer.keySet());
        }

        while (nextCycle < limit) {
            List<Transaction> txs = reorderBuffer.remove(nextCycle);
            List<Transaction> cycleTxs;
            if (txs == null) {
                cycleTxs = List.of();
            } else {
                txs.sort(CANONICAL_ORDER);
                cycleTxs = txs;
            }
            initialiseEnginesOnce();
            mineAcrossEngines(nextCycle, cycleTxs);
            nextCycle++;
        }
    }

    private void initialiseEnginesOnce() {
        if (enginesInitialised) {
            return;
        }
        for (SimulationEngine engine : engines) {
            engine.ensureInitialised(datasetHash);
        }
        enginesInitialised = true;
    }

    private void mineAcrossEngines(long cycle, List<Transaction> cycleTxs) {
        if (engines.size() == 1) {
            engines.get(0).mineCycle(datasetHash, cycle, cycleTxs);
            return;
        }

        List<Callable<Void>> tasks = new ArrayList<>(engines.size());
        for (SimulationEngine engine : engines) {
            tasks.add(() -> {
                engine.mineCycle(datasetHash, cycle, cycleTxs);
                return null;
            });
        }

        try {
            for (Future<Void> future : pool.invokeAll(tasks)) {
                future.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while mining cycle " + cycle, e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("engine failed on cycle " + cycle, e.getCause());
        }
    }

    private static ThreadFactory engineThreadFactory() {
        AtomicInteger seq = new AtomicInteger();
        return r -> {
            Thread t = new Thread(r, "sim-engine-" + seq.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
    }
}
