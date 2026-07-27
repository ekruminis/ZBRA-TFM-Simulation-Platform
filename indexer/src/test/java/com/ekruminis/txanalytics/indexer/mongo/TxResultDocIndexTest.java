package com.ekruminis.txanalytics.indexer.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;

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

        CompoundIndexes compound = TxResultDoc.class.getAnnotation(CompoundIndexes.class);
        assertThat(compound).isNotNull();
        assertThat(compound.value())
                .as("findByRunIdAndTxHash... serves the per-run transaction lookup")
                .extracting(CompoundIndex::def)
                .contains("{'runId': 1, 'txHash': 1}");
    }
}
