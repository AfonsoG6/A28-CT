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

	private static SSLSocketFactory getSSLSocketFactory()
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException,
			IOException {
		System.out.println("KeyStore Type: " + KeyStore.getDefaultType());
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Certificate cert = cf.generateCertificate(_resources.openRawResource(R.raw.hub_cert));
		ks.setCertificateEntry("customca", cert);

		System.out.println("TrustManagerFactory Type: " + TrustManagerFactory.getDefaultAlgorithm());
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(ks);
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, trustManagerFactory.getTrustManagers(), null);
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
