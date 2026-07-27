package com.ekruminis.txanalytics.indexer.elastic;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.RefreshPolicy;

class ElasticsearchWriteConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ElasticsearchRestClientAutoConfiguration.class,
                    ElasticsearchClientAutoConfiguration.class,
                    ElasticsearchDataAutoConfiguration.class))
            .withUserConfiguration(ElasticsearchWriteConfig.class);

    @Test
    void writesDoNotForceARefresh() {
        runner.run(context -> assertThat(context.getBean(ElasticsearchTemplate.class).getRefreshPolicy())
                .isEqualTo(RefreshPolicy.NONE));
    }

    @Test
    void replacesTheAutoConfiguredTemplateRatherThanSittingBesideIt() {
        runner.run(context -> assertThat(context).hasSingleBean(ElasticsearchTemplate.class));
    }
}
