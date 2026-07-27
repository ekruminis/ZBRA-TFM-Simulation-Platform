package com.ekruminis.txanalytics.indexer.elastic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

@Configuration
public class ElasticsearchWriteConfig {

    @Bean
    ElasticsearchTemplate elasticsearchTemplate(ElasticsearchClient client,
                                                ElasticsearchConverter converter) {
        ElasticsearchTemplate template = new ElasticsearchTemplate(client, converter);
        template.setRefreshPolicy(RefreshPolicy.NONE);
        return template;
    }
}
