package com.ea.architecture.domain.driven.infrastructure.repository.document;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryVariant;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.ea.architecture.domain.driven.infrastructure.persistance.document.model.DocumentElasticEntity;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface QueryBuilderUtils {
    public static Query termQuery(String field, String value) {
        QueryVariant queryVariant = new TermQuery.Builder().caseInsensitive(true).field(field).value(value).build();
        return new Query(queryVariant);
    }

    public static List<Query> prepareQueryList(DocumentElasticEntity document) {
        Map<String, String> conditionMap = new HashMap<>();
        conditionMap.put("documentName.keyword", document.getDocumentName());
        conditionMap.put("documentType.keyword", document.getDocumentType());
        conditionMap.put("documentMetadata.keyword", document.getDocumentMetadata());
        conditionMap.put("documentContent.keyword", document.getDocumentContent());
        conditionMap.put("documentCreationDate.keyword", document.getCreationDate());

        return conditionMap.entrySet().stream()
                .filter(entry -> !ObjectUtils.isEmpty(entry.getValue()))
                .map(entry -> QueryBuilderUtils.termQuery(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
