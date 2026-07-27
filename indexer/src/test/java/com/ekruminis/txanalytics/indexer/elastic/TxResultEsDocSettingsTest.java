package com.ekruminis.txanalytics.indexer.elastic;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.annotations.Setting;

class TxResultEsDocSettingsTest {

    @Test
    void indexIsShardedSoIndexingCanUseMoreThanOneCore() {
        Setting setting = TxResultEsDoc.class.getAnnotation(Setting.class);

        assertThat(setting).isNotNull();
        assertThat(setting.shards()).isGreaterThan((short) 1);
        assertThat(setting.replicas()).isZero();
    }
}
