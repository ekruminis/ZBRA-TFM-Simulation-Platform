package ZBRA.tfm;

import java.math.BigDecimal;
import java.util.ArrayList;

import ZBRA.blockchain.Block;
import ZBRA.blockchain.Data;
import ZBRA.blockchain.Miner;
import ZBRA.blockchain.Transaction;

public class EIP1559 extends AbstractTFM {
    private final static String type = "EIP-1559";

    public EIP1559() {
        super(type);
    }

    // used for logging each tx data


    @Override
    public Data fetchValidTX(ArrayList<Transaction> mempool, double weightLimit, ArrayList<Block> blockchain, Miner miner, double weightTarget) {
        // Sort mempool by highest fee per byte
        mempool.sort((tx1, tx2) -> Double.compare(tx2.getWeightFee(), tx1.getWeightFee()));
        Block block = blockchain.get(blockchain.size() - 1);

        double sizeUsedUp = 0;
        double weightUsedUp = 0;

        BigDecimal totalUserPay = BigDecimal.ZERO;
        BigDecimal minerRewards = BigDecimal.ZERO;
        BigDecimal burned = BigDecimal.ZERO;

        int index = 1;

        ArrayList<Transaction> txList = new ArrayList<>();

        double baseFee = block.getBaseFee() * (1.0 + 0.125 * ((block.getWeight() - weightTarget) / weightTarget));

        int i = 0;
        int mempoolSize = mempool.size();

        while (i < mempoolSize) {
            Transaction tx = mempool.get(i);
            double txWeight = tx.getWeight();
            double txSize = tx.getSize();

            if (txSize > weightLimit) {
                i++;
                continue;
            }

            if ((weightUsedUp + txWeight) > weightLimit) {
                break;
            }

            if (tx.getWeightFee() < baseFee) {
                break;
            }

            txList.add(tx);
            sizeUsedUp += txSize;
            weightUsedUp += txWeight;

            BigDecimal feeBurned = BigDecimal.valueOf(baseFee * txWeight);
            burned = burned.add(feeBurned);

            BigDecimal feeTip = BigDecimal.valueOf(tx.getTotalFee()).subtract(feeBurned);
            minerRewards = minerRewards.add(feeTip);


            i++;
            index++;
        }

        mempool.subList(0, txList.size()).clear();

        return new Data(mempool, txList, minerRewards, burned, baseFee, sizeUsedUp, weightUsedUp);
    }
}
