/**
 * 
 */
package com.flyover.kube.tools.connector;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.junit.Test;

/**
 * @author mramach
 *
 */
public class KubernetesClientCertificateAuthTest {
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	private String cert = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURRakNDQWlxZ0F3SUJBZ0lHQVduS2kvaFNNQTBHQ1NxR1NJYjNEUUVCQlFVQU1CZ3hGakFVQmdOVkJBTU0KRFRFNU1pNHhOamd1TWpVekxqRXdIaGNOTVRrd016STVNVGMwTWpRMFdoY05ORFF3TXpJNU1UYzBNalEwV2pBcApNUTR3REFZRFZRUUREQVZoWkcxcGJqRVhNQlVHQTFVRUNnd09jM2x6ZEdWdE9tMWhjM1JsY25Nd2dnRWlNQTBHCkNTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDaE8yUXVKWVdEQld5YmVHa1d6R0FLTmc0Q005ZUgKbWV6SlBadE85Wmc0M2hUcC9uV2c5MDhoejc5dUpJZ0c2Ykl4SXRlZzNZTUtlOUpVRHJheUg3VHBpL05IS2tmZwpqZm8wd2ZNRHU0Yy9yWWtzUzdOSUtIR3ZLWlZhQjJqTG9NWW5vVmNBRVpzdEo2K1kyQnl0N0g0ODFWVzdmZUV6CmlsZmZUaldsK2lMNlF1cXdVRThmMGJpVFF2RDRBQ1FQeWNvZ0R4QnNKdVdEc2xEVVJMV2VqMzFLWTdOOCt4TGIKRnFacFB0NU5LTUNHMlF3VUtTSEhQUHhTNEZlSURDdHVRZTc4K0ZSQkhzNzFYcVpBQ3Q5WEFVYnU2b3NuU2xGLwplYjJENjljam50MC9LY1lDeWY0YjFaMWdNbWFpaUNQYUVjY2IydWlqRFp2d2RGZ212Z1hIeHM1ekFnTUJBQUdqCmdZQXdmakJGQmdOVkhTTUVQakE4Z0JTY08wZTBrT05tdUFaWEkrWE9SS2JBL3h2UklxRWNwQm93R0RFV01CUUcKQTFVRUF4TU5NVGt5TGpFMk9DNHlOVE11TVlJR0FXbktpL1pjTUFrR0ExVWRFd1FDTUFBd0N3WURWUjBQQkFRRApBZ1F3TUIwR0ExVWRKUVFXTUJRR0NDc0dBUVVGQndNQkJnZ3JCZ0VGQlFjREFqQU5CZ2txaGtpRzl3MEJBUVVGCkFBT0NBUUVBU1FFbTlmNHkxMHFXOUJuNFRDcy90Y05lanJISTIxTm5qYWtIYVJRbFBDZEZ0OHJmYTJtdWlxZUEKakd6MlN4SzV1THhKYThkZVo5VVQ0NXZTQVVkOUEzS2JlaXRpdncrRTdldXhRK1BzQnlIaDA5U0xXbHBrdngvQQpGRE9jdjh1RStaQ1o2VVp2S1UxRWc1Q01Td2s5cExrSFczVGh1RkJBeTRoUEh2d0xGUWZFaHhkY0dTZTBUazZPCmVuRDdTdjVmT1ZBem1jNmxRaTVaa1FNeG5PUlZSNEFTVjlGTUFVQ2pJSWZSckZhTk9xT1V6Y1hUczhxb3NkV1IKNE84TTR5emZ3NytZYUtrYWg2MGRFaVR1Zk9KTTMvNXBVSUZNT0pxMHNjemVLa0xPS09mUDVCUzRRdHFzVUU1RApxQ2pyNUdVQ2VUVWhlUTlYcllqc0ZxQmpEV200N0E9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCi0tLS0tQkVHSU4gQ0VSVElGSUNBVEUtLS0tLQpNSUlDcVRDQ0FaRUNCZ0ZweW92MlhEQU5CZ2txaGtpRzl3MEJBUXNGQURBWU1SWXdGQVlEVlFRRERBMHhPVEl1Ck1UWTRMakkxTXk0eE1CNFhEVEU1TURNeU9URTNOREkwTkZvWERUUTBNRE15T1RFM05ESTBORm93R0RFV01CUUcKQTFVRUF3d05NVGt5TGpFMk9DNHlOVE11TVRDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQwpnZ0VCQUtOTFc5NVhuWnJEdDI3MDJ1Z1d5bEFBeUd2bUlDd1FRMVpUUmc5WU5kMmJRak9FWmRScjcxTE9sV1E2CkNyby9ybE40Y0hlMCtzY3VHKzU3ZjZMSUdwVHpPOHJDTXhsWHFGUTcrMEF1bU9KUDRCQkttY05iQjhuSXU1Z2YKajZjSy9PcHgvWXMvOU90M2tKUnhEbFc4ak1rZnovaEJ3T2VCenNuMnZpLzJIa2dnNjY2QmRGOEZ4YXd3bWxUNQpSSVVpOXlyRjZKMFNkWG5STHBEN2RPdFBFSm1zVEp0TVdFQSt3ejNRZCtJcVlxaUUwOWx6a1QzcVZLTGFGVE5LClRKdFl2OVMyU3JQUWdpZkFtZEtxNTBmSFBSWXRkTWlsc2NHeG93UGw5VmZQaUx1VnlLWHFQNEg2TStvYW9yUS8KSGs4b0pyd1BqOWZtdU5CNzZMR0duQVJWdGswQ0F3RUFBVEFOQmdrcWhraUc5dzBCQVFzRkFBT0NBUUVBQm1CdQpkbHBMV05xTnFyVWpqWFlkT0hsaS9hc2Jrejl2bnlDYWFidm5Vc3J0UENIZnR6ZDV2WkdnRTJydUJsbGVCRi8wCnovMEZ2Q0hvVGJKSzhTeHd1Ykc1S2xVb1N6ZW1NQThXRDJqcnZpVUFWNHNHZU5iUlVqeWdoQnE5WUZOYksvb2YKTm1TZ0pMQ2Fab0NZaWtycEsvcS9JU1J0OWpPbmYwQ2g5QVRuYXdWMThkblNOWVZrZTlwRlBaVnNDR0tVTGZTNwpxTmM4VWxYbytIVUR4eXBrT05IN0JZWVFqc2ZwR0hMbEZMZnRvbWc0d3kwL2ZYQmdXU2k3bnlNZm5EbkFEMFJ6CmJmdVhCc0V4OGtpYmNzVXRuanh5dFFreEJ6T3ptTC94bXRVRndVRmd5Nkg4d1k4WndZWVVHT01xakhYZVYybnMKZTRlTk9sNGZ1dkhERHlDaGl3PT0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=";
	private String key = "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcEFJQkFBS0NBUUVBb1R0a0xpV0Znd1ZzbTNocEZzeGdDallPQWpQWGg1bnN5VDJiVHZXWU9ONFU2ZjUxCm9QZFBJYysvYmlTSUJ1bXlNU0xYb04yRENudlNWQTYyc2grMDZZdnpSeXBINEkzNk5NSHpBN3VIUDYySkxFdXoKU0NoeHJ5bVZXZ2RveTZER0o2RlhBQkdiTFNldm1OZ2NyZXgrUE5WVnUzM2hNNHBYMzA0MXBmb2kra0xxc0ZCUApIOUc0azBMdytBQWtEOG5LSUE4UWJDYmxnN0pRMUVTMW5vOTlTbU96ZlBzUzJ4YW1hVDdlVFNqQWh0a01GQ2toCnh6ejhVdUJYaUF3cmJrSHUvUGhVUVI3TzlWNm1RQXJmVndGRzd1cUxKMHBSZjNtOWcrdlhJNTdkUHluR0FzbisKRzlXZFlESm1vb2dqMmhISEc5cm9vdzJiOEhSWUpyNEZ4OGJPY3dJREFRQUJBb0lCQUJ2R2VYbWhxU0JySE15agpyNmJueWx1MytDM1U5SG85d0JiaThCQm9Lck1SUHhVWWF2Y3g0NFgxbmdyQmJGV1VpTWdDTXBkWjBhdEtyVzhoCm9jT0p4c2E4LzBueE5MU3dnTHY1OVgxZEh1MlBYYVF0M2xLOTlPZXlDREtjT0Nkc0tQYkd5cEFPZUdjTkFTa1oKZ2NkTWlHcStiYWlCQlB5MkJuRkhyZUpMOXg2WmhtUWtQQjZoSUNBbzJlRWxtaGJxY015cVBFaldieXBtUUg2UQpIZFdSZWdJZmN1RzFEcjV4aFpVSzU2MDI2SVBnWEYrTjRLQnVKcFU5OHpjdXFZak1YTkRKeThWdlR4d3dYaUk4Cjc1ajlGNjR5aVp6NHpCY3VsdTB3WmtOOURnSGdZT2M2djJaV0lSRTM0Rzk2L2RtNDBxdG5ydUtZTnREd2ZodWcKSllCVjVURUNnWUVBMkp5UUdpMG5GTURVNHBVZTU3eGlPOXlNRUp6Q2JCWU9walBIanAvWnk5SldXM3JrZUliMApFRittbVJxU2NlTHRMZytmbGV3cU5IK0JYbTYzSUpaY0Vsb0JBKzdIYlV1SXEza0grMVpEN3F0T0RwbkNtbUxVCkd5NVhBNCtYS2x5cnV2VVBxNzB3UjVZVHltUEl0UzhSRUd6Z2tLWFNDZFMrSktWNDViR25ZWlVDZ1lFQXZvemQKdVVKZEIzakw0bG9BeFdnZURNOWtnS3VNdnJRM2lEdTFKb0RWaWRlZDJkMHN4TW1WMGN1NS9vb3dsL1FJNHJEUQoxUXNnYUd4ZWpmVDI0VGx5b3ZpSEE4bmI3VUwrZzlzMmpFQ3EwNjFjdXBwRG52eDZnUUhWM3Q5MlhTY0IyNGt0CmxqdmZpRkwyelE1ZldlR3Q4UFB5Y2oyUEtmVnp2V2xHTUN5NGZlY0NnWUVBaFVnSWJ1TGJpaXlBa1E2YVhwS08KaXpEUVNpUjZpTkVsb0dSNTVLczg4dW53VXdlUjd3dHl2MlJyZVFkZGxvKy95cmk0UkNKMWNhb0c2eGtLdXhWcApmNjA3dExUR3B0eDBNcllkRUN4clRqNi9uOEpDZUlaWTVvU2o2Ny85am9aSThKZDdWZnZwNmhKUkNSWk8yQlVtCjFjbDRmK2hZRGM2R1ZMd3dZSHpvZGVVQ2dZRUFqRE5oUUZvUDBOdVVSK3NvL2hacFpjdUNpeG10NlBMNG1RZzEKZFNyOTh2bnVic3BKa2xFd0pyamJGT25nYnkxakRFaEVuendja0RpTUthNi9wMTk1ZFdlZkQ2NktYcndZUURRZQpvbnRDTlpVczBTK0g2WWFqTENDSmwvNmJQRmJqQ3dDWHhoNHZqa0pjek90eVJOUEVBN0c2QlV3OTlnVmZMMUl5CkRtZ1pKcWtDZ1lCWDRXYnJMRmhrQWFPb1BpR25pNkRMNFFGODVuWE1uMlJ6L2I2U21vK2kzSEkrMmtYWEtGdjUKM0poaVl5T1BVbzVrV0hCTlZZcXp3dG13T3F3c09zMWFXYVBEK1l0cFJjaWJkZjRxeVlmaDEwTjhneFMrSGt5QQpkOE10S1pjSHowWVNUS2tFa2tLTWlqTGNGcWMyVHJZbjBScXk1Q2NJRDZjem1acFJHOSt1L0E9PQotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQo=";
	
	@Test
	public void test() throws Exception {
		
		X509Certificate certificate = certificate(cert);
		PrivateKey privateKey = privateKey(key);
		
		KubernetesConfig config = new KubernetesConfig();
		config.setEndpoint("https://cluster-001-markramach.mastr.ctl.io:30644");
		config.setAuthenticator((rt) -> {});
		config.setSslPolicy(new ClientCertificateSslPolicy(certificate, privateKey));
		
		Kubernetes kube = new Kubernetes(config);
		
		Namespace ns = kube.namespace("default").findOrCreate();
		
		System.out.println(ns.metadata().any());
		
	}

	private PrivateKey privateKey(String data) {
		
		try {
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			StringReader sr = new StringReader(new String(Base64.getDecoder().decode(key)));
			
			try(PemReader reader = new PemReader(sr)) {

				PemObject object = reader.readPemObject();
				
				PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(object.getContent());
				
				return keyFactory.generatePrivate(privKeySpec);
				
			}
			
		} catch (Exception e) {
			throw new RuntimeException("failed to load private key", e);
		}
		
	}
	
	private X509Certificate certificate(String data) {
		
		try {
			
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			
			return (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(data)));
			
		} catch (CertificateException e) {
			throw new RuntimeException("failed to load certificate", e);
		}
		
	}
	
}
