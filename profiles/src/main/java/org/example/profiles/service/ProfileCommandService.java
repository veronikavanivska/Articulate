package org.example.profiles.service;

import com.example.generated.*;
import io.grpc.stub.StreamObserver;
import org.example.profiles.helper.ProfileCommandsHandler;
import org.springframework.stereotype.Service;

@Service
public class ProfileCommandService extends ProfilesCommandsServiceGrpc.ProfilesCommandsServiceImplBase {

    private final ProfileCommandsHandler handler;

    public ProfileCommandService(ProfileCommandsHandler handler) {
        this.handler = handler;
    }
    @Override
    public void ensureUserProfile(EnsureUserProfileRequest request, StreamObserver<ApiResponse> responseObserver) {
        try {
            handler.ensureUserProfile(request.getUserId());
            responseObserver.onNext(ok("OK"));
        } catch (Exception e) {
            responseObserver.onNext(err("EnsureUserProfile failed: " + safeMsg(e)));
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void ensureWorkerProfile(EnsureWorkerProfileRequest request, StreamObserver<ApiResponse> responseObserver) {
        try {
            handler.ensureWorkerProfile(request.getUserId());
            responseObserver.onNext(ok("OK"));
        } catch (Exception e) {
            responseObserver.onNext(err("EnsureWorkerProfile failed: " + safeMsg(e)));
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void ensureAdminProfile(EnsureAdminProfileRequest request, StreamObserver<ApiResponse> responseObserver) {
        try {
            handler.ensureAdminProfile(request.getUserId());
            responseObserver.onNext(ok("OK"));
        } catch (Exception e) {
            responseObserver.onNext(err("EnsureAdminProfile failed: " + safeMsg(e)));
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteWorkerProfile(UserId request, StreamObserver<ApiResponse> responseObserver) {
        try {
            handler.deleteWorkerProfile(request.getUserId());
            responseObserver.onNext(ok("OK"));
        } catch (Exception e) {
            responseObserver.onNext(err("DeleteWorkerProfile failed: " + safeMsg(e)));
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteAdminProfile(UserId request, StreamObserver<ApiResponse> responseObserver) {
        try {
            handler.deleteAdminProfile(request.getUserId());
            responseObserver.onNext(ok("OK"));
        } catch (Exception e) {
            responseObserver.onNext(err("DeleteAdminProfile failed: " + safeMsg(e)));
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteAllProfiles(UserId request, StreamObserver<ApiResponse> responseObserver) {
        try {
            handler.deleteAllProfiles(request.getUserId());
            responseObserver.onNext(ok("OK"));
        } catch (Exception e) {
            responseObserver.onNext(err("DeleteAllProfiles failed: " + safeMsg(e)));
        } finally {
            responseObserver.onCompleted();
        }
    }


    private static ApiResponse ok(String msg) {

        return ApiResponse.newBuilder()
                .setCode(200)
                .setMessage(msg)
                .build();
    }

    private static ApiResponse err(String msg) {
        return ApiResponse.newBuilder()
                .setCode(200)
                .setMessage(msg)
                .build();
    }

    private static String safeMsg(Exception e) {
        String m = e.getMessage();
        return (m == null || m.isBlank()) ? e.getClass().getSimpleName() : m;
    }
}
