package com.ekruminis.txanalytics.indexer.mongo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexField;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.stereotype.Component;

@Component
public class TxResultIndexVerifier implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TxResultIndexVerifier.class);

    private static final List<String> NATURAL_KEY = List.of("runId", "txHash", "height");

    private final MongoTemplate mongo;

    public TxResultIndexVerifier(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<IndexInfo> indexes = mongo.indexOps(TxResultDoc.class).getIndexInfo();
        boolean guarded = indexes.stream()
                .filter(IndexInfo::isUnique)
                .anyMatch(index -> keyOf(index).equals(NATURAL_KEY));

        if (!guarded) {
            throw new IllegalStateException(
                    "tx_results has no unique index on " + NATURAL_KEY + " — a redelivered "
                            + "batch would insert duplicate transaction documents. Present indexes: "
                            + indexes.stream().map(IndexInfo::getName).toList());
        }
        log.info("tx_results duplicate protection verified: unique index on {}", NATURAL_KEY);
    }

    private static List<String> keyOf(IndexInfo index) {
        return index.getIndexFields().stream().map(IndexField::getKey).toList();
    }
}
