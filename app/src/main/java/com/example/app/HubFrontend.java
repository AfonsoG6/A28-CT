package com.example.app;

import android.annotation.SuppressLint;
import android.content.Context;
import com.example.hub.grpc.Hub.*;
import com.example.hub.grpc.HubServiceGrpc;
import com.squareup.okhttp.internal.framed.Ping;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.okhttp.OkHttpChannelBuilder;
import org.conscrypt.Conscrypt;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

	public boolean ping(String content) {
		ManagedChannel channel = buildChannel();
		HubServiceGrpc.HubServiceBlockingStub stub = HubServiceGrpc.newBlockingStub(channel);
		PingRequest request = PingRequest.newBuilder().setContent(content).build();
		PingResponse response;
		try {
			response = stub.withDeadlineAfter(5, TimeUnit.SECONDS).ping(request);
		}
		catch (StatusRuntimeException e) { return false; }
		finally { channel.shutdown(); }

		return response != null && !response.getContent().isEmpty();
	}

	public void claimInfection(boolean isDummy, String icc, List<SKEpochDayPair> sks) {
		ManagedChannel channel = buildChannel();
		HubServiceGrpc.HubServiceBlockingStub stub = HubServiceGrpc.newBlockingStub(channel);
		ClaimInfectionRequest request = ClaimInfectionRequest.newBuilder()
				.setIsDummy(isDummy)
				.setIcc(icc)
				.addAllSks(sks)
				.build();
		stub.withDeadlineAfter(5, TimeUnit.SECONDS).claimInfection(request);
		channel.shutdown();
	}

	public void sendDummyClaimInfection() throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstanceStrong();
		byte[] dummyIccBytes = new byte[40];
		random.nextBytes(dummyIccBytes);
		String dummyIcc = new String(dummyIccBytes, StandardCharsets.US_ASCII);
		List<SKEpochDayPair> dummySks = new ArrayList<>();
		for (int i=0; i<14; i++) {
			long dummyEpochDay = random.nextLong();
			byte[] dummySkBytes = new byte[512];
			random.nextBytes(dummySkBytes);
			String dummySk = new String(dummySkBytes, StandardCharsets.US_ASCII);
			SKEpochDayPair e = SKEpochDayPair.newBuilder().setEpochDay(dummyEpochDay).setSk(dummySk).build();
			dummySks.add(e);
		}
		claimInfection(true, dummyIcc, dummySks);
	}

	public void queryInfectedSKs() {

	}

}
