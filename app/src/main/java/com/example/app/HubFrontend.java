package com.example.app;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
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
	private static Resources _resources;

	public HubFrontend(Context context) {
		// Install Conscrypt provider
		Security.insertProviderAt(Conscrypt.newProvider(), Security.getProviders().length);
		_resources = context.getResources();
	}

	private static KeyStore getKeyStore()
			throws NoSuchAlgorithmException, KeyStoreException, CertificateException,
			IOException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		try (InputStream caCertInputStream = _resources.openRawResource(R.raw.ca_cert)) {
			Certificate cert = cf.generateCertificate(caCertInputStream);
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);
			keyStore.setCertificateEntry("CA", cert);
			return keyStore;
		}
	}

	private static SSLSocketFactory getSSLSocketFactory()
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException,
			IOException {
		KeyStore keyStore = getKeyStore();

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
		return context.getSocketFactory();
	}

	private static ManagedChannel buildChannel()
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException,
			IOException {
		return OkHttpChannelBuilder.forAddress("10.0.2.2", 29292)
				.useTransportSecurity()
				.sslSocketFactory(getSSLSocketFactory())
				.build();
	}

	public String ping(String content)
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException,
			IOException {
		ManagedChannel channel = buildChannel();
		HubServiceGrpc.HubServiceBlockingStub stub = HubServiceGrpc.newBlockingStub(channel);
		Hub.PingRequest pingRequest = Hub.PingRequest.newBuilder().setContent(content).build();
		Hub.PingResponse pingResponse = stub.withDeadlineAfter(5, TimeUnit.SECONDS).ping(pingRequest);
		channel.shutdown();
		return pingResponse.getContent();
	}

}
