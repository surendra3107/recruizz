package com.bbytes.recruiz.search;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.domain.CandidateSearch;
import com.bbytes.recruiz.search.domain.ClientSearch;
import com.bbytes.recruiz.search.domain.PositionRequestSearch;
import com.bbytes.recruiz.search.domain.PositionSearch;
import com.bbytes.recruiz.search.domain.ProspectSearch;
import com.bbytes.recruiz.search.domain.SuggestSearch;
import com.bbytes.recruiz.search.domain.UserSearch;

/**
 * Elasticsearch config class
 * 
 * @author Thanneer
 *
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.bbytes.recruiz.search.domain")
@ComponentScan(basePackages = { "com.bbytes.recruiz.search.domain" })
@Profile({ "dev", "prod" })
public class ElasticsearchConfig {

	private static Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);

	@Resource
	private Environment environment;

	private Node node;

	@Bean
	public NodeBuilder nodeBuilder() {
		return new NodeBuilder();
	}

	@Bean
	public Client client() throws IOException, RecruizException {
		Settings.Builder elasticsearchSettings = Settings.settingsBuilder().put("cluster.name",
				environment.getProperty("elasticsearch.cluster.name"));

		Client client = TransportClient.builder().settings(elasticsearchSettings).build()
				.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(environment.getProperty("elasticsearch.host"),
						Integer.parseInt(environment.getProperty("elasticsearch.port")))));
		
		try {
			ActionFuture<ClusterHealthResponse> healthResponseFuture = client.admin().cluster().health(new ClusterHealthRequest());

			if (ClusterHealthStatus.RED.equals(healthResponseFuture.get().getStatus().value())) {
				throw new Exception("Elastic Search health status RED");
			}
		} catch (Throwable e) {
			throw new RecruizException("Elastic Search cluster not running ..exiting app...", e);
		}
		// if reindex is true then drop all index using the below snippet
		String reindexOn = environment.getProperty("elasticsearch.reindex");
		if ("true".equalsIgnoreCase(reindexOn)) {
			logger.info("Deleting all the index using Delete Index GET request");
			try {
				client.admin().indices().delete(new DeleteIndexRequest(CandidateSearch.INDEX_NAME)).actionGet();
				client.admin().indices().delete(new DeleteIndexRequest(PositionSearch.INDEX_NAME)).actionGet();
				client.admin().indices().delete(new DeleteIndexRequest(ProspectSearch.INDEX_NAME)).actionGet();
				client.admin().indices().delete(new DeleteIndexRequest(ClientSearch.INDEX_NAME)).actionGet();
				client.admin().indices().delete(new DeleteIndexRequest(PositionRequestSearch.INDEX_NAME)).actionGet();
				client.admin().indices().delete(new DeleteIndexRequest(UserSearch.INDEX_NAME)).actionGet();
				client.admin().indices().delete(new DeleteIndexRequest(SuggestSearch.INDEX_NAME)).actionGet();
			} catch (IndexNotFoundException ex) {
				// do nothing if index not found
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

		}

		return client;

	}

	@Bean
	public ElasticsearchOperations elasticsearchTemplate() throws IOException, RecruizException {
		return new ElasticsearchTemplate(client());
	}

	@PreDestroy
	void destroy() {
		if (node != null) {
			logger.info("Stopping elastic search server..");
			try {
				node.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			logger.info("Stopping elastic search Stopped");
		}
	}
}