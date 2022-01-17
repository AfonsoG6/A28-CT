package com.example.hub;

import com.example.hub.grpc.Hub.*;
import com.example.hub.grpc.HubServiceGrpc;
import com.example.hub.models.HealthServiceManager;
import com.example.hub.models.ICCManager;
import com.example.hub.models.InfectedSKManager;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

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
	public void getNewIcc(GetNewICCRequest request, StreamObserver<GetNewICCResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(DEADLINE_EXCEEDED.withDescription("Deadline exceeded").asRuntimeException());
			return;
		}

		try {
			boolean exists = HealthServiceManager.logHealthService(request.getEmail(), request.getPassword());
			if (!exists) {
				responseObserver.onError(
						PERMISSION_DENIED.withDescription("Health Service credentials invalid").asRuntimeException()
				);
				return;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
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
		boolean isDummy = request.getIsDummy();
		Empty response = Empty.newBuilder().build();
		if (isDummy) {
			responseObserver.onNext(response);
			responseObserver.onCompleted();
			return;
		}
		boolean isValid = ICCManager.existsICC(request.getIcc());
		if (!isValid) {
			responseObserver.onError(
					INVALID_ARGUMENT.withDescription("Icc is not valid").asRuntimeException()
			);
			return;
		}
		ICCManager.deleteICC(request.getIcc());
		try {
			InfectedSKManager.insertSKs(request.getSksList());
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (SQLException e) {
			responseObserver.onError(
					INTERNAL.withDescription("Something went wrong").asRuntimeException()
			);
		}

	}

	@Override
	public void queryInfectedSKs(
			QueryInfectedSKsRequest request, StreamObserver<QueryInfectedSKsResponse> responseObserver
	) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(DEADLINE_EXCEEDED.withDescription("Deadline exceeded").asRuntimeException());
			return;
		}

		try {
			long maxInsEpoch = InfectedSKManager.queryMaxInsEpoch();
			List<SKEpochDayPair> sks = InfectedSKManager.queryInfectedSKs(request.getLastQueryEpoch());
			QueryInfectedSKsResponse response = QueryInfectedSKsResponse.newBuilder()
					.addAllSks(sks)
					.setQueryEpoch(maxInsEpoch)
					.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (SQLException e) {
			responseObserver.onError(
					INTERNAL.withDescription("Something went wrong").asRuntimeException()
			);
		}
	}
}
