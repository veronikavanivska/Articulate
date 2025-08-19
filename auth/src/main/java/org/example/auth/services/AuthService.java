package org.example.auth.services;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.example.auth.entities.RefreshTokens;
import org.example.auth.helpers.*;
import org.example.auth.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.example.auth.entities.Role;
import org.example.auth.entities.RoleName;
import org.example.auth.entities.User;
import org.example.auth.repositories.RoleRepository;

import org.example.auth.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;


@Service
public class AuthService extends AuthServiceGrpc.AuthServiceImplBase {

    @Value("${refresh.duration}")
    private long refreshDuration;

    private final StringRedisTemplate redisTemplate;

    private final CheckInput checkInput;
    private final BCrypt bcrypt;
    private final UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    public AuthService(StringRedisTemplate redisTemplate,CheckInput checkInput, BCrypt bcrypt, UserRepository userRepository, RoleRepository roleRepository, JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        this.checkInput = checkInput;
        this.bcrypt = bcrypt;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
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

        String accessToken = jwtUtil.generateToken(user.getId(), roles, user.getTokenVersion());

        String rawRefresh = TokenHelper.newOpaqueToken(64);
        String hashedRefresh = TokenHelper.sha256Base64Url(rawRefresh);

        RefreshTokens rt = new RefreshTokens();
        rt.setUser(user);
        rt.setTokenHash(hashedRefresh);
        rt.setExpiresAt(Instant.now().plus(Duration.ofDays(refreshDuration)));
        refreshTokenRepository.save(rt);

        redisTemplate.opsForValue().set("usr:ver:" + user.getId(), String.valueOf(user.getTokenVersion()));

        LoginResponse loginResponse = LoginResponse.newBuilder()
                .setAccessToken(accessToken)
                .setRefreshToken(rawRefresh)
                .setApiResponse(
                        ApiResponse.newBuilder()
                                .setCode(200)
                                .setMessage("Logged in!!!")
                                .build())
                .build();

        responseObserver.onNext(loginResponse);
        responseObserver.onCompleted();
    }
    private void OnStateChanged(User user){
        user.incrementTokenVersion();
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
        redisTemplate.opsForValue().set("usr:ver:" + user.getId(), String.valueOf(user.getTokenVersion()));
    }

    private void onUserDeleted(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
        redisTemplate.delete("usr:ver:" + userId);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest changePasswordRequest, StreamObserver<ApiResponse> responseObserver){
        Long userId = changePasswordRequest.getUserId();
        String oldPassword = changePasswordRequest.getPassword();
        String newPassword = changePasswordRequest.getNewPassword();
        User user = userRepository.findUsersById(userId).orElseThrow(null);

        if(user == null){
            responseObserver.onError(Status.NOT_FOUND.withDescription("User does not exist").asRuntimeException());
            return;
        }

        if(!bcrypt.checkPassword(oldPassword ,user.getPasswordHash())){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Password does not match").asRuntimeException());
            return;
        }


        if(!checkInput.isPasswordStrong(newPassword)){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Password should be strong").asRuntimeException());
            return;
        }

        if(newPassword.equals(oldPassword)){
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription("New password must be different").asRuntimeException());
            return;
        }

        user.setPasswordHash(bcrypt.hashPassword(newPassword));
        userRepository.save(user);

        ApiResponse apiResponse = ApiResponse.newBuilder().setCode(200).setMessage("Password changed").build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void changeEmail(ChangeEmailRequest request, StreamObserver<ApiResponse> responseObserver) {
        Long userId = request.getUserId();
        String email = request.getNewEmail();
        User user = userRepository.findUsersById(userId).orElseThrow(null);
        if(user == null){
            responseObserver.onError(Status.NOT_FOUND.withDescription("User does not exist").asRuntimeException());
            return;
        }

        if(!checkInput.isEmailValid(email)){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Email is not right(like the strong)").asRuntimeException());
            return;
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

    @Override
    @Transactional
    public void deleteUser(DeleteRequest request, StreamObserver<ApiResponse> responseObserver) {
            Long userId = request.getUserId();
            User user = userRepository.findUsersById(userId).orElseThrow(null);
            if(user == null){
                responseObserver.onError(Status.NOT_FOUND.withDescription("User does not exist").asRuntimeException());
                return;
            }

            refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
            redisTemplate.delete("usr:ver:" + userId);
            userRepository.delete(user);

            ApiResponse apiResponse = ApiResponse.newBuilder().setCode(200).setMessage("User deleted").build();
            responseObserver.onNext(apiResponse);
            responseObserver.onCompleted();

    }

    @Override
    @Transactional
    public void assignRole(AssignRoleRequest request, StreamObserver<ApiResponse> responseObserver){
        Long userId = request.getUserId();

        RoleName roleName;

        try{
            roleName = RoleMapper.map(request.getRoleName());
        }catch (IllegalArgumentException ex) {
            throw Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException();
        }

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(()->new StatusRuntimeException(
                        Status.NOT_FOUND.withDescription("User does not exist")));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new StatusRuntimeException(
                        Status.NOT_FOUND.withDescription("Role not found: " + roleName)));



        boolean added = user.getRoles().add(role);
        if (added) {
            user.incrementTokenVersion();
            userRepository.save(user);

            refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
            redisTemplate.opsForValue().set("usr:ver:" + user.getId(), String.valueOf(user.getTokenVersion()));
        }

        ApiResponse resp = ApiResponse.newBuilder()
                .setCode(200)
                .setMessage(added ? "Role assigned" : "User already has role")
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void revokeRole(RevokeRoleRequest request, StreamObserver<ApiResponse> responseObserver) {
        Long userId = request.getUserId();

        RoleName roleName;

        try{
            roleName = RoleMapper.map(request.getRoleName());
        }catch (IllegalArgumentException ex) {
            throw Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException();
        }

        if(roleName == RoleName.ROLE_USER){
            responseObserver.onError(Status.PERMISSION_DENIED
                    .withDescription("ROLE_USER cannot be revoked").asRuntimeException());
            return;

        }

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(()->new StatusRuntimeException(
                        Status.NOT_FOUND.withDescription("User does not exist")));


        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new StatusRuntimeException(
                        Status.NOT_FOUND.withDescription("Role not found: " + roleName)));


        boolean removed = user.getRoles().remove(role);
        if(removed) {
            user.incrementTokenVersion();
            userRepository.save(user);

            refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
            redisTemplate.opsForValue().set("usr:ver:" + user.getId(), String.valueOf(user.getTokenVersion()));
        }

        ApiResponse resp = ApiResponse.newBuilder()
                .setCode(200)
                .setMessage(removed ? "Role removed" : "User already has not this role")
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void enableDisableUser(DisableUserRequest request, StreamObserver<ApiResponse> responseObserver) {
        Long userId = request.getUserId();
        User user = userRepository.findUsersById(userId).orElseThrow(null);
        if(user == null){
            responseObserver.onError(Status.NOT_FOUND.withDescription("User does not exist").asRuntimeException());
            return;
        }

        boolean enabled = user.isEnabled();
        user.setEnabled(!enabled) ;
        user.incrementTokenVersion();
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
        redisTemplate.opsForValue().set("usr:ver:" + user.getId(), String.valueOf(user.getTokenVersion()));
        ApiResponse resp = ApiResponse.newBuilder()
                .setCode(200)
                .setMessage(enabled ? "User disabled" : "User enabled")
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();

    }

    @Override
    public void refresh(RefreshRequest request, StreamObserver<RefreshResponse> responseObserver) {
        String rawRefresh = request.getRefreshToken();

        if(rawRefresh == null || rawRefresh.isBlank()){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("refresh token required").asRuntimeException());
            return;
        }

        String hashToken = TokenHelper.sha256Base64Url(rawRefresh);
        RefreshTokens currentToken = refreshTokenRepository.findActiveByHash(hashToken,Instant.now())
                .orElseThrow(
                        () -> Status.UNAUTHENTICATED.withDescription("invalid refresh token").asRuntimeException());

        Long userId = currentToken.getUser().getId();
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(
                        () -> Status.UNAUTHENTICATED.withDescription("user not found").asRuntimeException());

        if(!user.isEnabled()){
            currentToken.setRevoked(true);
            currentToken.setLastUsedAt(Instant.now());
            refreshTokenRepository.save(currentToken);
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription("user disabled").asRuntimeException());
            return;
        }

        currentToken.setRevoked(true);
        currentToken.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(currentToken);


        String newRaw = TokenHelper.newOpaqueToken(64);
        String newHash = TokenHelper.sha256Base64Url(newRaw);

        RefreshTokens next = new RefreshTokens();
        next.setUser(user);
        next.setTokenHash(newHash);
        next.setExpiresAt(Instant.now().plus(Duration.ofDays(refreshDuration)));
        refreshTokenRepository.save(next);

        List<String> roles = user.getRoles().stream().map(r -> r.getName().name()).toList();
        String newAccess = jwtUtil.generateToken(user.getId(), roles, user.getTokenVersion());

        redisTemplate.opsForValue().set("usr:ver:" + user.getId(), String.valueOf(user.getTokenVersion()));

        RefreshResponse resp = RefreshResponse.newBuilder()
                .setAccessToken(newAccess)
                .setRefreshToken(newRaw)
                .setResponse(ApiResponse.newBuilder().setCode(200).setMessage("Refreshed and rotated").build())
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }







}

