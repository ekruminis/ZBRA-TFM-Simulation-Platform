package ZBRA.blockchain;

public class Transaction {
    String hash;
    double size; // in bytes
    double weight; // SegWit weight
    double byteFee; // fee per byte
    double weightFee; // fee per weight unit
    double totalFee; // total offered in fees
    long arrivalCycle; // cycle this tx reached the mempool in; older sorts first on equal fees

    public Transaction(String h, double s, double w, double f) {
        this(h, s, w, f, 0L);
    }

    public Transaction(String h, double s, double w, double f, long arrivalCycle) {
        this.hash = h;
        this.size = s;
        this.weight = w;
        this.totalFee = f;
        this.byteFee = totalFee / size;
        this.weightFee = totalFee / weight;
        this.arrivalCycle = arrivalCycle;
    }

    public String getHash() {
        return hash;
    }

    public double getSize() {
        return size;
    }

    public double getWeight() {
        return weight;
    }

    public double getByteFee() {
        return byteFee;
    }

    public double getWeightFee() {
        return weightFee;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public long getArrivalCycle() {
        return arrivalCycle;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction{");
        sb.append("hash=").append(hash);
        sb.append(", size=").append(size);
        sb.append(", weight=").append(weight);
        sb.append(", byteFee=").append(byteFee);
        sb.append(", weightFee=").append(weightFee);
        sb.append(", totalFee=").append(totalFee);
        sb.append('}');
        return sb.toString();
    }
}
