package ZBRA.tfm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import ZBRA.blockchain.Block;
import ZBRA.blockchain.Data;
import ZBRA.blockchain.Miner;
import ZBRA.blockchain.Transaction;

public class Burning2ndPrice extends AbstractTFM {
    private final static String type = "Burning 2nd Price Auction";

    private static final Comparator<Transaction> FEE_THEN_AGE = Comparator
            .comparingDouble(Transaction::getWeightFee).reversed()
            .thenComparingLong(Transaction::getArrivalCycle)
            .thenComparing(Transaction::getHash);

    private final PriorityQueue<Transaction> pending = new PriorityQueue<>(FEE_THEN_AGE);

    private int absorbed = -1;

    public Burning2ndPrice() {
        super(type);
    }

    // Main Burning Second-Price Mechanism Implementation
    @Override
    public Data fetchValidTX(ArrayList<Transaction> mempool, double weightLimit, ArrayList<Block> blockchain, Miner miner, double weightTarget) {
        if (absorbed < 0) {
            pending.addAll(mempool);
        } else {
            for (int i = absorbed; i < mempool.size(); i++) {
                pending.add(mempool.get(i));
            }
        }

        double sizeUsedUp = 0;
        double weightUsedUp = 0;

        BigDecimal totalUserPay = new BigDecimal("0");
        BigDecimal minerRewards = new BigDecimal("0");
        BigDecimal burned = new BigDecimal("0");

        double effectiveFee = 0;

        ArrayList<Transaction> txList = new ArrayList<>();
        ArrayList<Transaction> confirmedTxList = new ArrayList<>();
        ArrayList<Transaction> unconfirmedTxList = new ArrayList<>();

        while (!pending.isEmpty()) {
            Transaction tx = pending.peek();
            double txWeight = tx.getWeight();
            double txSize = tx.getSize();

            if (txWeight > weightLimit) {
                pending.poll();
                continue;
            }

            // Only take it once it is known to fit — peeking leaves the transaction
            // in the mempool for a later block instead of discarding it.
            if ((weightUsedUp + txWeight) > weightLimit) {
                break;
            }

            pending.poll();
            txList.add(tx);
            sizeUsedUp += txSize;
            weightUsedUp += txWeight;
        }

        double totalWeight = 0;
        for (Transaction t : txList) {
            totalWeight += t.getWeight();
        }

        int split = 0;
        double confirmedWeight = 0;
        for (int i = 0; i < txList.size() - 1; i++) {
            confirmedWeight += txList.get(i).getWeight();
            split = i + 1;
            if (confirmedWeight * 2 >= totalWeight) {
                break;
            }
        }

        boolean validSplit = split > 0 && confirmedWeight * 2 >= totalWeight;

        if (validSplit) {
            confirmedTxList = new ArrayList<>(txList.subList(0, split));
            unconfirmedTxList = new ArrayList<>(txList.subList(split, txList.size()));
        } else {
            confirmedTxList = new ArrayList<>();
            unconfirmedTxList = new ArrayList<>(txList);
        }

        if (validSplit && !unconfirmedTxList.isEmpty()) {
            effectiveFee = unconfirmedTxList.get(0).getWeightFee();
        }

        for (Transaction t : confirmedTxList) {
            double feePaid = t.getWeight() * effectiveFee;
            totalUserPay = totalUserPay.add(BigDecimal.valueOf(feePaid));
        }

        for (Transaction t : unconfirmedTxList) {
            if (validSplit) {
                minerRewards = minerRewards.add(BigDecimal.valueOf(t.getTotalFee()));
            }
        }

        burned = totalUserPay.subtract(minerRewards);

        // unconfirmed transactions go back to await a later block
        pending.addAll(unconfirmedTxList);

        mempool.clear();
        mempool.addAll(pending);
        absorbed = mempool.size();

        return new Data(mempool, confirmedTxList, unconfirmedTxList, minerRewards, effectiveFee, burned, sizeUsedUp, weightUsedUp);
    }
}
