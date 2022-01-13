package com.example.hub;

import com.example.hub.grpc.Hub.*;
import com.example.hub.grpc.HubServiceGrpc;
import com.example.hub.models.ICCManager;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import java.security.NoSuchAlgorithmException;

import static io.grpc.Status.*;

public class HubServiceImpl extends HubServiceGrpc.HubServiceImplBase {

	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(DEADLINE_EXCEEDED.withDescription("Deadline exceeded").asRuntimeException());
			return;
		}
		// Build Response
		PingResponse.Builder builder = PingResponse.newBuilder();
		String content = request.getContent();
		if (content.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Input cannot be empty!").asRuntimeException());
			return;
		}
		System.out.println("Received: " + content);
		builder.setContent("Hello " + content + "!");
		PingResponse response = builder.build();
		// Send Response
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getNewIcc(Empty request, StreamObserver<GetNewICCResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(DEADLINE_EXCEEDED.withDescription("Deadline exceeded").asRuntimeException());
			return;
		}

		String generatedICC;
		try {
			generatedICC = ICCManager.generateICC();
		} catch (NoSuchAlgorithmException e) {
			responseObserver.onError(UNAVAILABLE.withDescription("Internal Error: " + e.getMessage()).asRuntimeException());
			return;
		}
		GetNewICCResponse.Builder builder = GetNewICCResponse.newBuilder();
		builder.setIcc(generatedICC);
		GetNewICCResponse response = builder.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void claimInfection(ClaimInfectionRequest request, StreamObserver<Empty> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(DEADLINE_EXCEEDED.withDescription("Deadline exceeded").asRuntimeException());
			return;
		}
		System.out.println(request.getIcc());
		boolean isDummy = request.getIsDummy();
		Empty response = Empty.newBuilder().build();
		if (!isDummy) {
			// TODO: Check if ICC is valid
			// TODO: If valid, remove ICC from database, and add SKs to database
		}
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void queryInfectedSKs(
			QueryInfectedSKsRequest request, StreamObserver<QueryInfectedSKsResponse> responseObserver
	) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(DEADLINE_EXCEEDED.withDescription("Deadline exceeded").asRuntimeException());
			return;
		}
		QueryInfectedSKsResponse.Builder builder = QueryInfectedSKsResponse.newBuilder();

		QueryInfectedSKsResponse response = builder.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
