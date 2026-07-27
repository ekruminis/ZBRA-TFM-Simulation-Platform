package ZBRA.blockchain;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Data {
    ArrayList<Transaction> mempool;
    ArrayList<Transaction> confirmed;
    ArrayList<Transaction> unconfirmed = new ArrayList<>();
    double size;
    double weight;
    double baseFee;
    BigDecimal sharedPool = new BigDecimal(0);
    BigDecimal rewards = new BigDecimal(0);
    BigDecimal burned = new BigDecimal(0);
    long mempoolSize;
    BigDecimal poolEffect;
    boolean takeFromPublic;
    boolean takeFromPrivate;
    BigDecimal blockTotalReward;
    BigDecimal blockTipsTotal;
    long txCount;

    public ArrayList<Transaction> getUnconfirmed() {
        return unconfirmed;
    }

    public ArrayList<Transaction> getMempool() {
        return mempool;
    }

    public ArrayList<Transaction> getConfirmed() {
        return confirmed;
    }

    public double getSize() {
        return size;
    }

    public BigDecimal getRewards() { return rewards; }

    public BigDecimal getBurned() { return burned; }

    public double getBaseFee() { return baseFee; }

    public BigDecimal getPool() { return sharedPool; }

    public BigDecimal getSharedPool() {
        return sharedPool;
    }

    public long getMempoolSize() {
        return mempoolSize;
    }

    public BigDecimal getPoolEffect() {
        return poolEffect;
    }

    public boolean isTakeFromPublic() {
        return takeFromPublic;
    }

    public boolean isTakeFromPrivate() {
        return takeFromPrivate;
    }

    public BigDecimal getBlockTotalReward() {
        return blockTotalReward;
    }

    public BigDecimal getBlockTipsTotal() {
        return blockTipsTotal;
    }

    public long getTxCount() {
        return txCount;
    }

    public double getWeight() {
        return weight;
    }

    // First-Price Data Style
    public Data(ArrayList<Transaction> mempool,
            ArrayList<Transaction> confirmedTX,
            BigDecimal minerRewards, 
            double bytesUsed,
            double weightUsed) {

        this.mempool = mempool;
        this.confirmed = confirmedTX;

        this.size = bytesUsed;
        this.weight = weightUsed;
        this.rewards = minerRewards;
        this.baseFee = -1;

        this.txCount = confirmedTX.size();
        this.mempoolSize = mempool.size();

    }

    // Second-Price Data Style
    public Data(ArrayList<Transaction> mempool,
            ArrayList<Transaction> confirmedTX,
            BigDecimal minerRewards,
            double effectiveFee,
            double bytesUsed,
            double weightUsed) {

        this.mempool = mempool;
        this.confirmed = confirmedTX;

        this.size = bytesUsed;
        this.weight = weightUsed;
        this.rewards = minerRewards;
        this.baseFee = effectiveFee;

        this.txCount = confirmedTX.size();
        this.mempoolSize = mempool.size();

    }

    // Burning 2nd Price Data Style
    public Data(ArrayList<Transaction> mempool,
            ArrayList<Transaction> confirmedTX, 
            ArrayList<Transaction> unconfirmedTX, 
            BigDecimal minerRewards, 
            double effectiveFee, BigDecimal burnedAmount, 
            double bytesUsed, double weightUsed) {

        this.mempool = mempool;
        this.confirmed = confirmedTX;
        this.unconfirmed = unconfirmedTX;

        this.size = bytesUsed;
        this.weight = weightUsed;
        this.rewards = minerRewards;
        this.burned = burnedAmount;
        this.baseFee = effectiveFee;

        this.txCount = confirmedTX.size();
        this.mempoolSize = mempool.size();

    }

    // EIP-1559 Data Style
    public Data(ArrayList<Transaction> mempool, 
            ArrayList<Transaction> confirmedTX, 
            BigDecimal minerRewards, 
            BigDecimal burnedAmount, 
            double baseFee,
            double bytesUsed, double weightUsed) {

        this.mempool = mempool;
        this.confirmed = confirmedTX;

        this.size = bytesUsed;
        this.weight = weightUsed;
        this.rewards = minerRewards;
        this.burned = burnedAmount;
        this.baseFee = baseFee;

        this.txCount = confirmedTX.size();
        this.mempoolSize = mempool.size();

    }

    // Pool Data Style
    public Data(ArrayList<Transaction> mempool, 
            ArrayList<Transaction> confirmedTX, 
            BigDecimal minerRewards, 
            double baseFee, 
            BigDecimal poolTotal, 
            double bytesUsed, double weightUsed, 
            BigDecimal poolEffect, 
            boolean takenFromPublicPool, 
            boolean takenFromPrivatePool, 
            BigDecimal blockTotalReward, 
            BigDecimal blockTipsTotal) {

        this.mempool = mempool;
        this.confirmed = confirmedTX;

        this.size = bytesUsed;
        this.weight = weightUsed;
        this.rewards = minerRewards;
        this.sharedPool = poolTotal;
        this.baseFee = baseFee;

        this.txCount = confirmedTX.size();
        this.mempoolSize = mempool.size();
        this.poolEffect = poolEffect;
        this.takeFromPublic = takenFromPublicPool;
        this.takeFromPrivate = takenFromPrivatePool;
        this.blockTotalReward = blockTotalReward;
        this.blockTipsTotal = blockTipsTotal;

    }
}
