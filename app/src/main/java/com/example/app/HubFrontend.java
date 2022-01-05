package com.example.app;

import android.content.Context;
import com.example.hub.grpc.Hub;
import com.example.hub.grpc.HubServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import org.conscrypt.Conscrypt;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

public class HubFrontend {
	private static HubFrontend instance; // HubFrontend is a singleton
	private final SSLSocketFactory sslSocketFactory;

	private HubFrontend(Context context) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
			KeyManagementException {
		// Install Conscrypt provider
		Security.insertProviderAt(Conscrypt.newProvider(), Security.getProviders().length);

		// Create and configure a SSLSocketFactory
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Certificate cert;
		try (InputStream caCertInputStream = context.getResources().openRawResource(R.raw.hub_cert)) {
			cert = cf.generateCertificate(caCertInputStream);
		}
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null);
		keyStore.setCertificateEntry("CA", cert);

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
		sslSocketFactory = sslContext.getSocketFactory();
	}

	// Common usage in an Activity: "HubFrontend.getInstance(getApplicationContext());"
	public static HubFrontend getInstance(Context context)
			throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
			KeyManagementException {
		if (instance == null) {
			instance = new HubFrontend(context);
		}
		return instance;
	}

	private ManagedChannel buildChannel() {
		return OkHttpChannelBuilder.forAddress("10.0.2.2", 29292)
				.useTransportSecurity()
				.sslSocketFactory(sslSocketFactory)
				.hostnameVerifier((hostname, session) -> true) // Ignore hostname verification for now since the server is local
				.build();
	}

	public String ping(String content) {
		ManagedChannel channel = buildChannel();
		HubServiceGrpc.HubServiceBlockingStub stub = HubServiceGrpc.newBlockingStub(channel);
		Hub.PingRequest pingRequest = Hub.PingRequest.newBuilder().setContent(content).build();
		Hub.PingResponse pingResponse = stub.withDeadlineAfter(5, TimeUnit.SECONDS).ping(pingRequest);
		channel.shutdown();
		return pingResponse.getContent();
	}

}
