package org.example.auth.services;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.transaction.annotation.Transactional;
import org.example.auth.entities.Role;
import org.example.auth.entities.RoleName;
import org.example.auth.entities.User;
import org.example.auth.helpers.BCrypt;
import org.example.auth.helpers.CheckInput;
import org.example.auth.helpers.JwtUtil;
import org.example.auth.repositories.RoleRepository;
import org.example.auth.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AuthService extends AuthServiceGrpc.AuthServiceImplBase {
    private final CheckInput checkInput;
    private final BCrypt bcrypt;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    public AuthService(CheckInput checkInput, BCrypt bcrypt, UserRepository userRepository, RoleRepository roleRepository, JwtUtil jwtUtil) {
        this.checkInput = checkInput;
        this.bcrypt = bcrypt;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(RegistrationRequest registrationRequest, StreamObserver<ApiResponse> responseObserver){
        String email = registrationRequest.getEmail();
        String password = registrationRequest.getPassword();

        if(!checkInput.isPasswordStrong(password)){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Password should be strong").asException());
            return;
        }

        if(!checkInput.isEmailValid(email)){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Email is not right").asException());
            return;
        }

        if(userRepository.existsByEmail(email)){
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Email already exists").asException());
            return;
        }

        String hashedPassword = bcrypt.hashPassword(password);

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setEnabled(true);

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(()-> Status.INTERNAL.withDescription("SMTH wrong ROLE_USER in register").asRuntimeException());

        user.getRoles().add(userRole);

        userRepository.save(user);

        ApiResponse apiResponse = ApiResponse.newBuilder().setCode(200).setMessage("User registered and created(standard role user)").build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void login(LoginRequest loginRequest, StreamObserver<LoginResponse> responseObserver){
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        if(email == null || email.isBlank() ){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Email is required").asRuntimeException());
            return;
        }
        if(password == null || password.isBlank() ){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Password is required").asRuntimeException());
            return;
        }

        User user = userRepository.findByEmailWithRoles(email)
                .orElse(null);
        if (user == null) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("User does not exist").asRuntimeException());
            return;
        }

        if (!user.isEnabled()) {
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription("User is disabled").asRuntimeException());
            return;
        }

        if(!bcrypt.checkPassword(password,user.getPasswordHash())){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Password does not match").asException());
            return;
        }

        List<String> roles = user.getRoles().stream().map(role -> role.getName().name()).toList();

        String token = jwtUtil.generateToken(user.getId(), roles );

        LoginResponse loginResponse = LoginResponse.newBuilder()
                .setToken(token)
                .setApiResponse(
                        ApiResponse.newBuilder()
                                .setCode(200)
                                .setMessage("Logged in!!!")
                                .build())
                .build();

        responseObserver.onNext(loginResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest, StreamObserver<ApiResponse> responseObserver){
        Long userId = (long) changePasswordRequest.getUserId();
        String oldPassword = changePasswordRequest.getPassword();
        String newPassword = changePasswordRequest.getNewPassword();
        User user = userRepository.findUsersById(userId).orElseThrow(null);

        if(user == null){
            responseObserver.onError(Status.NOT_FOUND.withDescription("User does not exist").asRuntimeException());
        }

        if(!bcrypt.checkPassword(oldPassword ,user.getPasswordHash())){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Password does not match").asRuntimeException());
        }


        if(!checkInput.isPasswordStrong(newPassword)){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Password should be strong").asRuntimeException());
        }

        if(newPassword.equals(oldPassword)){
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription("New password must be different").asRuntimeException());
        }

        user.setPasswordHash(bcrypt.hashPassword(newPassword));
        userRepository.save(user);

        ApiResponse apiResponse = ApiResponse.newBuilder().setCode(200).setMessage("Password changed").build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void changeEmail(ChangeEmailRequest request, StreamObserver<ApiResponse> responseObserver) {
        Long userId = (long) request.getUserId();
        String email = request.getNewEmail();
        User user = userRepository.findUsersById(userId).orElseThrow(null);
        if(user == null){
            responseObserver.onError(Status.NOT_FOUND.withDescription("User does not exist").asRuntimeException());
        }

        if(!checkInput.isEmailValid(email)){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Email is not right(like the strong)").asRuntimeException());
        }

        if(userRepository.existsByEmail(email)){
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Email already exists").asException());
            return;
        }

        user.setEmail(email);
        userRepository.save(user);

        ApiResponse apiResponse = ApiResponse.newBuilder().setCode(200).setMessage("Email changed").build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();
    }
}
