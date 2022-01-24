package com.example.app;

import android.content.Context;
import android.util.Log;
import com.example.hub.grpc.Hub;
import com.example.hub.grpc.Hub.ClaimInfectionRequest;
import com.example.hub.grpc.Hub.PingRequest;
import com.example.hub.grpc.Hub.PingResponse;
import com.example.hub.grpc.Hub.SKEpochDayPair;
import com.example.hub.grpc.HubServiceGrpc;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HubFrontend {
	private static final String TAG = HubFrontend.class.getName();

	private static HubFrontend instance; // Singleton
	private final SSLSocketFactory sslSocketFactory;

	private HubFrontend(Context context) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
			KeyManagementException {
		Log.i(TAG, "Creating HubFrontend instance");
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
		Log.d(TAG, "Pinging hub with content: " + content);
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

	public void claimInfection(boolean isDummy, String icc, List<SKEpochDayPair> sks) throws StatusRuntimeException {
		Log.d(TAG, "Claiming infection to Hub with ICC: " + icc);
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

	public void sendDummyClaimInfection() throws NoSuchAlgorithmException, StatusRuntimeException {
		Log.d(TAG, "Sending dummy infection claim to Hub");
		SecureRandom random = SecureRandom.getInstanceStrong();
		String dummyIcc = generateDummyIcc();
		List<SKEpochDayPair> dummySks = new ArrayList<>();
		for (int i=0; i<14; i++) {
			int dummyEpochDay = random.nextInt();
			byte[] dummySk = new byte[256];
			random.nextBytes(dummySk);
			SKEpochDayPair pair = SKEpochDayPair.newBuilder()
					.setEpochDay(dummyEpochDay)
					.setSk(ByteString.copyFrom(dummySk))
					.build();
			dummySks.add(pair);
		}
		claimInfection(true, dummyIcc, dummySks);
	}

	private String generateDummyIcc() throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstanceStrong();
		char[] charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		char[] dummyIcc = new char[20];
		for (int i=0; i<dummyIcc.length; i++) {
			int idx = random.nextInt(charset.length);
			dummyIcc[i] = charset[idx];
		}
		Log.d(TAG, "Generated dummy ICC: " + Arrays.toString(dummyIcc) + "(length: " + dummyIcc.length + ")");
		return Arrays.toString(dummyIcc);
	}

	public Hub.QueryInfectedSKsResponse queryInfectedSKs(long lastQueryEpoch) throws StatusRuntimeException {
		Log.d(TAG, "Querying Hub for infected SKs since epoch: " + lastQueryEpoch);
		ManagedChannel channel = buildChannel();
		HubServiceGrpc.HubServiceBlockingStub stub = HubServiceGrpc.newBlockingStub(channel);
		Hub.QueryInfectedSKsRequest request = Hub.QueryInfectedSKsRequest.newBuilder().setLastQueryEpoch(lastQueryEpoch).build();
		Hub.QueryInfectedSKsResponse response = stub.withDeadlineAfter(5, TimeUnit.SECONDS).queryInfectedSKs(request);
		channel.shutdown();
		return response;
	}

}
