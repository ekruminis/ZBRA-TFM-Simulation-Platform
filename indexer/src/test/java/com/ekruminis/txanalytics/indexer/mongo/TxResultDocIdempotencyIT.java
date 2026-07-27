package com.ekruminis.txanalytics.indexer.mongo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexField;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.ekruminis.txanalytics.wire.TxResult;

@DataMongoTest
@Testcontainers
class TxResultDocIdempotencyIT {

    @Container
    static final MongoDBContainer MONGO =
            new MongoDBContainer(DockerImageName.parse("mongo:7"));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("spring.data.mongodb.auto-index-creation", () -> true);
    }

    @Autowired
    MongoTemplate mongo;

    @BeforeEach
    void clear() {
        mongo.remove(new Query(), TxResultDoc.class);
    }

    @Test
    void mongoGeneratesAnObjectIdRatherThanADerivedKey() {
        TxResultDoc saved = mongo.save(doc("abc", 7));

        assertThat(saved.getId()).isNotNull();
        assertThat(ObjectId.isValid(saved.getId()))
                .as("a twelve byte ascending id, not the old runId:txHash:height string")
                .isTrue();
    }

    @Test
    void refusesTheSameTransactionTwiceForOneRunAndHeight() {
        mongo.insert(doc("abc", 7));

        assertThatThrownBy(() -> mongo.insert(doc("abc", 7)))
                .isInstanceOf(DuplicateKeyException.class);

        assertThat(mongo.findAll(TxResultDoc.class)).hasSize(1);
    }

    @Test
    void stillAcceptsTheSameTransactionAtADifferentHeight() {
        mongo.insert(doc("abc", 7));
        mongo.insert(doc("abc", 8));

        assertThat(mongo.findAll(TxResultDoc.class)).hasSize(2);
    }

    @Test
    void createsTheUniqueIndexTheVerifierRequires() {
        List<IndexInfo> indexes = mongo.indexOps(TxResultDoc.class).getIndexInfo();

        assertThat(indexes)
                .filteredOn(IndexInfo::isUnique)
                .anyMatch(i -> i.getIndexFields().stream().map(IndexField::getKey).toList()
                        .equals(List.of("runId", "txHash", "height")));
    }

    private static TxResultDoc doc(String txHash, int height) {
        return TxResultDoc.from(new TxResult(
                "run-1", "first_price", height, 0L, txHash, 250.0, 1.0, 1.0, true, null));
    }
}
