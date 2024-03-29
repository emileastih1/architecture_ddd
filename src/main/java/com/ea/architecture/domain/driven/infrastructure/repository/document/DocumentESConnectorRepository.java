package com.ea.architecture.domain.driven.infrastructure.repository.document;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ea.architecture.domain.driven.domain.document.entity.DocumentResult;
import com.ea.architecture.domain.driven.domain.document.vo.DocumentStatus;
import com.ea.architecture.domain.driven.domain.exception.FunctionalException;
import com.ea.architecture.domain.driven.domain.exception.MessageCode;
import com.ea.architecture.domain.driven.infrastructure.persistance.document.model.DocumentElasticEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.ea.architecture.domain.driven.infrastructure.repository.document.QueryBuilderUtils.prepareQueryList;


@Repository
public class DocumentESConnectorRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentESConnectorRepository.class);

    @Value("${elastic.index.name}")
    private String index;
    private final ElasticsearchClient elasticsearchClient;

    public DocumentESConnectorRepository(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public DocumentResult addOrUpdateDocument(DocumentElasticEntity document) throws IOException {
        IndexRequest<DocumentElasticEntity> request = IndexRequest.of(i ->
                i.index(index)
                        .document(document));
        IndexResponse response = elasticsearchClient.index(request);

        return switch (response.result()) {
            case Created -> {
                LOGGER.debug("Document with id " + response.id() + " added successfully!");
                yield new DocumentResult(response.id(), DocumentStatus.CREATED.name());
            }
            case Updated -> {
                LOGGER.debug("Document with id " + response.id() + " updated successfully!");
                yield new DocumentResult(response.id(), DocumentStatus.UPDATED.name());
            }
            case NotFound -> {
                LOGGER.error("Document " + document.getDocumentId() + " - " + document.getDocumentName() + " not found!");
                yield new DocumentResult(StringUtils.EMPTY, DocumentStatus.NOT_FOUND.name());
            }
            case NoOp -> {
                LOGGER.debug("No operation performed on document " + document.getDocumentId() + " - " + document.getDocumentName() + " !");
                yield new DocumentResult(StringUtils.EMPTY, DocumentStatus.NO_OPERATION.name());
            }
            case Deleted -> {
                LOGGER.debug("Document with id " + response.id() + "  deleted successfully!");
                yield new DocumentResult(response.id(), DocumentStatus.DELETED.name());
            }
        };
    }

    public boolean bulkInsertDocuments(List<DocumentElasticEntity> documentList) throws IOException {
        BulkRequest.Builder builder = new BulkRequest.Builder();
        documentList.stream().forEach(document ->
                builder.operations(op ->
                        op.index(i ->
                                i.index(index)
                                        .id(String.valueOf(document.getDocumentId()))
                                        .document(document)))
        );
        BulkResponse bulkResponse = elasticsearchClient.bulk(builder.build());
        return !bulkResponse.errors();
    }

    public DocumentElasticEntity getDocumentById(String id) throws FunctionalException, IOException {
        GetResponse<DocumentElasticEntity> response = elasticsearchClient.get(req ->
                req.index(index)
                        .id(id), DocumentElasticEntity.class);
        if (!response.found())
            throw new FunctionalException(MessageCode.DOCUMENT_NOT_FOUND, "Document with ID " + id + " not found!");

        DocumentElasticEntity source = response.source();
        assert source != null;
        source.setElasticId(response.id());
        return source;
    }

    public List<DocumentElasticEntity> getDocumentsWithMustQuery(DocumentElasticEntity document) throws IOException {
        List<Query> queries = prepareQueryList(document);
        SearchResponse<DocumentElasticEntity> documentSearchResponse = elasticsearchClient.search(req ->
                        req.index(index)
                                .size(100)
                                .query(query ->
                                        query.bool(bool ->
                                                bool.must(queries))),
                DocumentElasticEntity.class);

        return documentSearchResponse.hits().hits().stream()
                .map(Hit::source).collect(Collectors.toList());
    }

    public List<DocumentElasticEntity> getDocumentsWithShouldQuery(DocumentElasticEntity document) throws IOException {
        List<Query> queries = prepareQueryList(document);
        SearchResponse<DocumentElasticEntity> documentSearchResponse = elasticsearchClient.search(req ->
                        req.index(index)
                                .size(100)
                                .query(query ->
                                        query.bool(bool ->
                                                bool.should(queries))),
                DocumentElasticEntity.class);

        return documentSearchResponse.hits().hits().stream()
                .map(Hit::source).collect(Collectors.toList());
    }

    public String deleteDocumentById(Long id) throws IOException {
        DeleteRequest request = DeleteRequest.of(req ->
                req.index(index).id(String.valueOf(id)));
        DeleteResponse response = elasticsearchClient.delete(request);
        return response.result().toString();
    }

    public String updateDocument(DocumentElasticEntity document) throws IOException {
        UpdateRequest<DocumentElasticEntity, DocumentElasticEntity> updateRequest = UpdateRequest.of(req ->
                req.index(index)
                        .id(String.valueOf(document.getDocumentId()))
                        .doc(document));
        UpdateResponse<DocumentElasticEntity> response = elasticsearchClient.update(updateRequest, DocumentElasticEntity.class);
        return response.result().toString();
    }

    public String uploadDocument(DocumentElasticEntity documentElasticEntity) {
        return "Document uploaded successfully!";
    }
}
