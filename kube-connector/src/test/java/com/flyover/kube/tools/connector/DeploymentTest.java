/**
 * 
 */
package com.flyover.kube.tools.connector;

import static com.flyover.kube.tools.connector.MockUtils.*;
import static com.flyover.kube.tools.connector.MockUtils.RequestMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.client.ExpectedCount.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.flyover.kube.tools.connector.model.DeploymentModel;
import com.flyover.kube.tools.connector.model.GenericKubeItemsModel;
import com.flyover.kube.tools.connector.model.NamespaceModel;
import com.flyover.kube.tools.connector.model.PathsModel;
import com.flyover.kube.tools.connector.model.PodModel;

/**
 * @author mramach
 *
 */
public class DeploymentTest {
	
	private static final String NAMESPACE = "test";

	private Kubernetes kube;
	
	@Before
	public void before() {
	
		KubernetesConfig config = new KubernetesConfig();
		config.setAuthenticator(new KubernetesAuthenticator() {
			
			@Override
			public void configure(RestTemplate restTemplate) {}
			
		});
		
		this.kube = new Kubernetes(config);
		
	}
	
	@Test
	public void testCreateOrUpdate_NotFound() {
		
		MockRestServiceServer mockServer = 
			MockRestServiceServer.createServer(kube.getRestTemplate());
		
		setupNamespace(mockServer);
		
		ThreadLocal<byte[]> capture = new ThreadLocal<>();
		
		mockServer
			.expect(once(), requestTo(
					localhost().path("/apis/extensions/v1beta1/namespaces/{namespace}/deployments/{name}")
						.buildAndExpand(NAMESPACE, "nginx").toUri()))
			.andExpect(method(GET))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.NOT_FOUND);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		mockServer
			.expect(requestTo(
					localhost().path("/apis/extensions/v1beta1/namespaces/{namespace}/deployments")
						.buildAndExpand(NAMESPACE).toUri(), capture))
			.andExpect(method(POST))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(capture.get(), HttpStatus.OK);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		Namespace ns = kube.namespace(NAMESPACE).findOrCreate();
		
		Deployment deployment = ns.deployment("nginx", "nginx:latest", 80);
		
		deployment.merge();
		
		mockServer.verify();
		
		assertNotNull(deployment);
		assertEquals(NAMESPACE, deployment.metadata().getNamespace());
		assertEquals("nginx", deployment.metadata().getName());
		
	}
	
	@Test
	public void testCreateOrUpdate_Found() {
		
		MockRestServiceServer mockServer = 
			MockRestServiceServer.createServer(kube.getRestTemplate());
		
		setupNamespace(mockServer);
		
		ThreadLocal<byte[]> capture = new ThreadLocal<>();
		
		DeploymentModel dm = new DeploymentModel();
		dm.getMetadata().setNamespace(NAMESPACE);
		dm.getMetadata().setName("nginx");
		
		mockServer
			.expect(once(), requestTo(
					localhost().path("/apis/extensions/v1beta1/namespaces/{namespace}/deployments/{name}")
						.buildAndExpand(NAMESPACE, "nginx").toUri()))
			.andExpect(method(GET))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(write(dm), HttpStatus.OK);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		mockServer
			.expect(requestTo(
					localhost().path("/apis/extensions/v1beta1/namespaces/{namespace}/deployments/{name}")
						.buildAndExpand(NAMESPACE, "nginx").toUri(), capture))
			.andExpect(method(PUT))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(capture.get(), HttpStatus.OK);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		Namespace ns = kube.namespace(NAMESPACE).findOrCreate();
		
		Deployment deployment = ns.deployment("nginx", "nginx:latest", 80);
			
		deployment.merge();
		
		mockServer.verify();
		
		assertNotNull(deployment);
		assertEquals(NAMESPACE, deployment.metadata().getNamespace());
		assertEquals("nginx", deployment.metadata().getName());
		
	}
	
	@Test
	public void testReady() {
		
		MockRestServiceServer mockServer = 
			MockRestServiceServer.createServer(kube.getRestTemplate());
		
		setupNamespace(mockServer);
		
		DeploymentModel d = new DeploymentModel();
		d.getMetadata().setNamespace(NAMESPACE);
		d.getMetadata().setName("nginx");
		d.getMetadata().setGeneration(1);
		d.getStatus().setObservedGeneration(1);
		d.getStatus().setAvailableReplicas(1);
		d.getStatus().setUnavailableReplicas(0);
		
		mockServer
		.expect(manyTimes(), requestTo(
				localhost().path("/apis/extensions/v1beta1/namespaces/{namespace}/deployments/{name}")
					.buildAndExpand(NAMESPACE, "nginx").toUri()))
		.andExpect(method(GET))
		.andRespond((res) -> {
			
			MockClientHttpResponse response = new MockClientHttpResponse(write(d), HttpStatus.OK);
			response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
			
			return response;
			
		});
		
		Deployment deployment = kube.namespace(NAMESPACE).findOrCreate()
			.deployment("nginx").find();
		
		deployment.ready(5, TimeUnit.SECONDS);
		
	}
	
	@Test(expected = RuntimeException.class)
	public void testReady_Timeout() {
		
		MockRestServiceServer mockServer = 
			MockRestServiceServer.createServer(kube.getRestTemplate());
		
		setupNamespace(mockServer);
		
		DeploymentModel d = new DeploymentModel();
		d.getMetadata().setNamespace(NAMESPACE);
		d.getMetadata().setName("nginx");
		d.getMetadata().setGeneration(1);
		d.getStatus().setObservedGeneration(2);
		d.getStatus().setAvailableReplicas(1);
		d.getStatus().setUnavailableReplicas(0);
		
		mockServer
			.expect(manyTimes(), requestTo(
					localhost().path("/apis/extensions/v1beta1/namespaces/{namespace}/deployments/{name}")
						.buildAndExpand(NAMESPACE, "nginx").toUri()))
			.andExpect(method(GET))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(write(d), HttpStatus.OK);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		Deployment deployment = kube.namespace(NAMESPACE).findOrCreate()
			.deployment("nginx").find();
		
		deployment.ready(100, TimeUnit.MILLISECONDS);
		
		fail("this should result in a timeout");
		
	}
	
	@Test(expected = RuntimeException.class)
	public void testReady_ProcessingError() {
		
		MockRestServiceServer mockServer = 
			MockRestServiceServer.createServer(kube.getRestTemplate());
		
		setupNamespace(mockServer);
		
		DeploymentModel d = new DeploymentModel();
		d.getMetadata().setNamespace(NAMESPACE);
		d.getMetadata().setName("nginx");
		d.getMetadata().setGeneration(1);
		d.getStatus().setObservedGeneration(2);
		d.getStatus().setAvailableReplicas(1);
		d.getStatus().setUnavailableReplicas(0);
		
		Queue<MockClientHttpResponse> responses = new LinkedBlockingQueue<>();
		responses.add(new MockClientHttpResponse(write(d), HttpStatus.OK));
		responses.add(new MockClientHttpResponse(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR));
		
		mockServer
			.expect(once(), requestTo(
					localhost().path("/apis/extensions/v1beta1/namespaces/{namespace}/deployments/{name}")
						.buildAndExpand(NAMESPACE, "nginx").toUri()))
			.andExpect(method(GET))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = responses.poll();
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		Deployment deployment = kube.namespace(NAMESPACE).findOrCreate()
			.deployment("nginx").find();
		
		deployment.ready(5, TimeUnit.SECONDS);
		
		fail("this should result in processing error");
		
	}
	
	@Test
	public void testExpose() {
		
		MockRestServiceServer mockServer = 
			MockRestServiceServer.createServer(kube.getRestTemplate());
		
		setupNamespace(mockServer);
		
		DeploymentModel d = new DeploymentModel();
		d.getMetadata().setNamespace(NAMESPACE);
		d.getMetadata().setName("nginx");
		d.getMetadata().setGeneration(1);
		d.getSpec().getSelector().getMatchLabels().put("foo", "bar");
		
		mockServer
			.expect(manyTimes(), requestTo(
					localhost().path("/apis/extensions/v1beta1/namespaces/{namespace}/deployments/{name}")
						.buildAndExpand(NAMESPACE, "nginx").toUri()))
			.andExpect(method(GET))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(write(d), HttpStatus.OK);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		mockServer
			.expect(requestTo(
					localhost().path("/api/v1/namespaces/{namespace}/services/{name}")
						.buildAndExpand(NAMESPACE, "nginx-8080").toUri()))
			.andExpect(method(GET))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.NOT_FOUND);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		ThreadLocal<byte[]> capture = new ThreadLocal<>();
		
		mockServer
			.expect(requestTo(
					localhost().path("/api/v1/namespaces/{namespace}/services")
						.buildAndExpand(NAMESPACE).toUri(), capture))
			.andExpect(method(POST))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(capture.get(), HttpStatus.OK);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		Deployment deployment = kube.namespace(NAMESPACE).findOrCreate()
			.deployment("nginx").find();
		
		Service service = deployment.expose(8080);
		
		assertNotNull(service);
		assertEquals(Collections.singletonMap("foo", "bar"), service.spec().selectors());
		
	}
	
	@Test
	public void testDelete() {
		
		MockRestServiceServer mockServer = 
			MockRestServiceServer.createServer(kube.getRestTemplate());
		
		PathsModel paths = new PathsModel();
		paths.setPaths(Arrays.asList(
			"/api/v1", 
			"/apis/extensions/v1beta1"));
		
		mockServer
			.expect(manyTimes(), requestTo(localhost().build().toUri()))
			.andExpect(method(GET))
			.andRespond(withSuccess(write(paths), APPLICATION_JSON));
		
		mockServer
			.expect(manyTimes(), requestTo(localhost().path("/apis/extensions/v1beta1").build().toUri()))
			.andExpect(method(GET))
			.andRespond(withSuccess(write(resourceList(resource("deployments", "Deployment", true))), APPLICATION_JSON));
		
		Deployment deployment = new Deployment(kube);
		deployment.metadata().setNamespace(NAMESPACE);
		deployment.metadata().setName("nginx");
		
		mockServer
			.expect(once(), requestTo(
					localhost().path("/apis/extensions/v1beta1/namespaces/{namespace}/deployments/{name}")
						.buildAndExpand(NAMESPACE, "nginx").toUri()))
			.andExpect(method(DELETE))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(write(deployment.model()), HttpStatus.OK);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		deployment.delete();
		
		mockServer.verify();
		
	}
	
	@Test
	public void testDelete_NotFound() {
		
		MockRestServiceServer mockServer = 
			MockRestServiceServer.createServer(kube.getRestTemplate());
		
		PathsModel paths = new PathsModel();
		paths.setPaths(Arrays.asList(
			"/api/v1", 
			"/apis/extensions/v1beta1"));
		
		mockServer
			.expect(manyTimes(), requestTo(localhost().build().toUri()))
			.andExpect(method(GET))
			.andRespond(withSuccess(write(paths), APPLICATION_JSON));
		
		mockServer
			.expect(manyTimes(), requestTo(localhost().path("/apis/extensions/v1beta1").build().toUri()))
			.andExpect(method(GET))
			.andRespond(withSuccess(write(resourceList(resource("deployments", "Deployment", true))), APPLICATION_JSON));
		
		Deployment deployment = new Deployment(kube);
		deployment.metadata().setNamespace(NAMESPACE);
		deployment.metadata().setName("nginx");
		
		mockServer
			.expect(once(), requestTo(
					localhost().path("/apis/extensions/v1beta1/namespaces/{namespace}/deployments/{name}")
						.buildAndExpand(NAMESPACE, "nginx").toUri()))
			.andExpect(method(DELETE))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.NOT_FOUND);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		deployment.delete();
		
		mockServer.verify();
		
	}
	
	@Test
	public void testPods() {
		
		MockRestServiceServer mockServer = 
			MockRestServiceServer.createServer(kube.getRestTemplate());
		
		setupNamespace(mockServer);
		
		DeploymentModel d = new DeploymentModel();
		d.getMetadata().setNamespace(NAMESPACE);
		d.getMetadata().setName("nginx");
		d.getSpec().getSelector().getMatchLabels().put("key", "foo");
		
		PodModel p = new PodModel();
		p.getMetadata().setNamespace(NAMESPACE);
		p.getMetadata().setName("nginx-pod");
		
		GenericKubeItemsModel list = new GenericKubeItemsModel();
		list.setItems(Arrays.asList(p));
		
		mockServer
			.expect(manyTimes(), requestTo(
					localhost().path("/apis/extensions/v1beta1/namespaces/{namespace}/deployments/{name}")
						.buildAndExpand(NAMESPACE, "nginx").toUri()))
			.andExpect(method(GET))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(write(d), HttpStatus.OK);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		mockServer
			.expect(manyTimes(), requestTo(
					localhost().path("/api/v1/namespaces/{namespace}/pods")
						.queryParam("labelSelector", "key=foo")
						.buildAndExpand(NAMESPACE, "nginx").toUri()))
			.andExpect(method(GET))
			.andRespond((res) -> {
				
				MockClientHttpResponse response = new MockClientHttpResponse(write(list), HttpStatus.OK);
				response.getHeaders().put("Content-Type", Arrays.asList(APPLICATION_JSON_VALUE));
				
				return response;
				
			});
		
		Deployment deployment = kube.namespace(NAMESPACE).findOrCreate()
			.deployment("nginx").find();
		
		List<Pod> pods = deployment.pods();

		mockServer.verify();
		
		assertNotNull("The result is null.", pods);
		assertFalse("The result is empty", pods.isEmpty());
		
	}
	
	private void setupNamespace(MockRestServiceServer mockServer) {
		
		PathsModel paths = new PathsModel();
		paths.setPaths(Arrays.asList(
			"/api/v1", 
			"/apis/extensions/v1beta1"));
		
		mockServer
			.expect(manyTimes(), requestTo(localhost().build().toUri()))
			.andExpect(method(GET))
			.andRespond(withSuccess(write(paths), APPLICATION_JSON));
		
		mockServer
			.expect(manyTimes(), requestTo(localhost().path("/api/v1").build().toUri()))
			.andExpect(method(GET))
			.andRespond(withSuccess(write(resourceList(
					resource("namespaces", "Namespace", false),
					resource("services", "Service", true),
					resource("pods", "Pod", true))), APPLICATION_JSON));

		NamespaceModel model = new NamespaceModel();
		model.getMetadata().setName(NAMESPACE);
		
		mockServer
			.expect(manyTimes(), requestTo(
					localhost().path("/api/v1/namespaces/{namespace}")
						.buildAndExpand(NAMESPACE).toUri()))
			.andExpect(method(GET))
			.andRespond(withSuccess(write(model), APPLICATION_JSON));
		
		mockServer
			.expect(manyTimes(), requestTo(localhost().path("/apis/extensions/v1beta1").build().toUri()))
			.andExpect(method(GET))
			.andRespond(withSuccess(write(resourceList(resource("deployments", "Deployment", true))), APPLICATION_JSON));

	}

}
