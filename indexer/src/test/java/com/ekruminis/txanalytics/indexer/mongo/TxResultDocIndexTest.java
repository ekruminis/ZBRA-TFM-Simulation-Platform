package com.ekruminis.txanalytics.indexer.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;

import com.ekruminis.txanalytics.wire.TxResult;

class TxResultDocIndexTest {

    @Test
    void doesNotIndexTfm() throws NoSuchFieldException {
        assertThat(TxResultDoc.class.getDeclaredField("tfm").getAnnotation(Indexed.class))
                .isNull();
    }

    @Test
    void stillIndexesWhatTheTransactionLookupsQueryOn() throws NoSuchFieldException {
        assertThat(TxResultDoc.class.getDeclaredField("txHash").getAnnotation(Indexed.class))
                .as("findByTxHash... serves the cross-mechanism transaction lookup")
                .isNotNull();

        assertThat(naturalKeyIndex().def())
                .as("per-run transaction lookup needs runId and txHash leading the index")
                .startsWith("{'runId': 1, 'txHash': 1");
    }

    @Test
    void guardsAgainstDuplicatesWithAUniqueIndexOnTheNaturalKey() {
        assertThat(naturalKeyIndex().unique())
                .as("unique constraint on {runId, txHash, height}")
                .isTrue();
        assertThat(naturalKeyIndex().def()).contains("height");
    }

    @Test
    void leavesTheIdForMongoToGenerate() {
        TxResultDoc doc = TxResultDoc.from(new TxResult(
                "run-1", "first_price", 7, 0L, "abc", 250.0, 1.0, 1.0, true, null));

        assertThat(doc.getId()).isNull();
        assertThat(doc.getRunId()).isEqualTo("run-1");
        assertThat(doc.getTxHash()).isEqualTo("abc");
        assertThat(doc.getHeight()).isEqualTo(7);
    }

    private static CompoundIndex naturalKeyIndex() {
        CompoundIndexes compound = TxResultDoc.class.getAnnotation(CompoundIndexes.class);
        assertThat(compound).isNotNull();
        return compound.value()[0];
    }
}
