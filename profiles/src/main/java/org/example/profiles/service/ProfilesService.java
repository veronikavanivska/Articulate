package org.example.profiles.service;

import com.example.generated.*;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.example.profiles.entities.ProfileUser;
import org.example.profiles.helper.Mapper;
import org.example.profiles.repositories.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


@Service

//TODO: add discipline
public class ProfilesService extends ProfilesServiceGrpc.ProfilesServiceImplBase {

    private final ProfileUserRepository profileUserRepository;
    private final ProfileWorkerRepository profileWorkerRepository;
    private final ProfileAdminRepository profileAdminRepository;
    private final ProfileWorkerDisciplineRepository profileWorkerDisciplineRepository;
    private final DisciplineRepository disciplineRepository;
    private final ProfileWorkerStatementRepository profileWorkerStatementRepository;
    private final Mapper mapper;

    public ProfilesService( ProfileUserRepository profileUserRepository, Mapper mapper,ProfileWorkerRepository profileWorkerRepository, ProfileAdminRepository profileAdminRepository, ProfileWorkerDisciplineRepository profileWorkerDisciplineRepository, DisciplineRepository disciplineRepository, ProfileWorkerStatementRepository profileWorkerStatementRepository) {
        super();
        this.profileUserRepository = profileUserRepository;
        this.profileWorkerRepository = profileWorkerRepository;
        this.profileAdminRepository = profileAdminRepository;
        this.profileWorkerDisciplineRepository = profileWorkerDisciplineRepository;
        this.disciplineRepository = disciplineRepository;
        this.profileWorkerStatementRepository = profileWorkerStatementRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public void getMyProfile(GetProfileRequest request, StreamObserver<GetProfileResponse> responseObserver) {
        Long userId = request.getUserId();

        ProfileUser user = profileUserRepository.findByUserId(userId) .orElseThrow(() -> new StatusRuntimeException(
                Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
        ));


        com.example.generated.ProfileUser protoUser = com.example.generated.ProfileUser.newBuilder()
                .setFullname(user.getFullname() == null ? "" : user.getFullname())
                .setBio(user.getBio() == null ? "" : user.getBio())
                .build();

        ProfileWorker protoWorker = null;

        if(user.getWorker() != null) {
            org.example.profiles.entities.ProfileWorker worker = profileWorkerRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));

            protoWorker = mapper.buildProtoWorker(userId, worker);
        }


        ProfileAdmin protoAdmin = null;
        if (user.getAdmin() != null) {
            org.example.profiles.entities.ProfileAdmin admin = profileAdminRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));

            protoAdmin = ProfileAdmin.newBuilder().setUnitName(admin.getUnitName()).build();
        }

        ProfileView.Builder viewBuilder = ProfileView.newBuilder()
                .setUser(protoUser);

        if (protoWorker != null) viewBuilder.setWorker(protoWorker);
        if (protoAdmin != null) viewBuilder.setAdmin(protoAdmin);

        ApiResponse apiResponse = ApiResponse.newBuilder().setCode(200).setMessage("Get this!!").build();

        GetProfileResponse response = GetProfileResponse.newBuilder().setProfile(viewBuilder.build()).setResponse(apiResponse).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void updateMyProfile(UpdateMyProfileRequest request, StreamObserver<UpdateMyProfileResponse> responseObserver) {
        Long userId = request.getUserId();


        var userProfile = profileUserRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                Status.NOT_FOUND.withDescription("Profile not found for user " + userId)));

        var user = request.getUser();
        var entity = profileUserRepository.findByUserId(userId).orElseThrow();

        entity.setFullname(user.getFullname() == null ? "" : user.getFullname());
        entity.setBio(user.getBio() == null ? "" : user.getBio());
        profileUserRepository.save(entity);

        com.example.generated.ProfileUser protoUser = com.example.generated.ProfileUser.newBuilder()
                    .setFullname(entity.getFullname())
                    .setBio(entity.getBio())
                    .build();



        ProfileWorker protoWorker = null;

        if(userProfile.getWorker() != null) {
            var worker = request.getWorker();
            var entity2 = profileWorkerRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));

            entity2.setDegreeTitle(worker.getDegreeTitle() == null ? "" : worker.getDegreeTitle());
            entity2.setUnitName(worker.getUnitName() == null ? "" : worker.getUnitName());
            profileWorkerRepository.save(entity2);

            protoWorker = mapper.buildProtoWorker(userId, entity2);

        }


        ProfileAdmin protoAdmin = null;

        if(userProfile.getAdmin() != null) {
            var admin = request.getAdmin();
            var entity3 = profileAdminRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));

            entity3.setUnitName(admin.getUnitName() == null ? "" : admin.getUnitName());
            profileAdminRepository.save(entity3);

            protoAdmin = ProfileAdmin.newBuilder().setUnitName(entity3.getUnitName()).build();
        }

        ProfileView.Builder viewBuilder = ProfileView.newBuilder()
                .setUser(protoUser);

        if (protoWorker != null) viewBuilder.setWorker(protoWorker);
        if (protoAdmin != null) viewBuilder.setAdmin(protoAdmin);

        ApiResponse apiResponse = ApiResponse.newBuilder().setCode(200).setMessage("Update this!!").build();

        UpdateMyProfileResponse response = UpdateMyProfileResponse.newBuilder().setResponse(apiResponse).setProfile(viewBuilder.build()).build();


        responseObserver.onNext(response);
        responseObserver.onCompleted();


    }

    @Override
    public void seeSomeoneProfile(SeeSomeoneProfileRequest request, StreamObserver<GetProfileResponse> responseObserver) {
        Long userId = request.getUserId();

        ProfileUser user = profileUserRepository.findByUserId(userId).orElseThrow();

        com.example.generated.ProfileUser protoUser = com.example.generated.ProfileUser.newBuilder()
                .setFullname(user.getFullname())
                .setBio(user.getBio())
                .build();


        ProfileWorker protoWorker = null;

        if(user.getWorker() != null) {
            org.example.profiles.entities.ProfileWorker worker = profileWorkerRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));


            protoWorker = mapper.buildProtoWorker(userId, worker);
        }


        ProfileAdmin protoAdmin = null;
        if (user.getAdmin() != null) {
            org.example.profiles.entities.ProfileAdmin admin = profileAdminRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));

            protoAdmin = ProfileAdmin.newBuilder().setUnitName(admin.getUnitName()).build();
        }

        ProfileView.Builder viewBuilder = ProfileView.newBuilder()
                .setUser(protoUser);

        if (protoWorker != null) viewBuilder.setWorker(protoWorker);
        if (protoAdmin != null) viewBuilder.setAdmin(protoAdmin);

        ApiResponse apiResponse = ApiResponse.newBuilder().setCode(200).setMessage("Get this!!").build();

        GetProfileResponse response = GetProfileResponse.newBuilder().setProfile(viewBuilder.build()).setResponse(apiResponse).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional(readOnly = true)
    public void listWorkerDisciplines(ListWorkerDisciplinesRequest request,
                                      StreamObserver<ListWorkerDisciplinesResponse> responseObserver) {

        Long userId = request.getUserId();

        var links = profileWorkerDisciplineRepository.findAllByUserIdWithDiscipline(userId);

        ListWorkerDisciplinesResponse.Builder resp = ListWorkerDisciplinesResponse.newBuilder();
        for (var link : links) {
            var d = link.getDiscipline();
            if (d != null) {
                resp.addDisciplines(DisciplineRef.newBuilder()
                        .setId(d.getId())
                        .setName(d.getName())
                        .build());
            }
        }

        resp.setResponse(ApiResponse.newBuilder().setCode(200).setMessage("OK").build());
        responseObserver.onNext(resp.build());
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void addWorkerDiscipline(AddWorkerDisciplineRequest request,
                                    StreamObserver<ListWorkerDisciplinesResponse> responseObserver) {

        Long userId = request.getUserId();
        Long disciplineId = request.getDisciplineId();

        // worker musi istnieć
        profileWorkerRepository.findByUserId(userId).orElseThrow(() ->
                Status.NOT_FOUND.withDescription("Worker not found: " + userId).asRuntimeException());

        // discipline musi istnieć
        var discipline = disciplineRepository.findById(disciplineId).orElseThrow(() ->
                Status.NOT_FOUND.withDescription("Discipline not found: " + disciplineId).asRuntimeException());

        if (!profileWorkerDisciplineRepository.existsByIdUserIdAndIdDisciplineId(userId, disciplineId)) {
            var link = new org.example.profiles.entities.ProfileWorkerDiscipline();
            link.setId(new org.example.profiles.entities.ProfileWorkerDisciplineId(userId, disciplineId));
            link.setDiscipline(discipline);
            profileWorkerDisciplineRepository.save(link);
        }

        int year = java.time.Year.now().getValue(); // w tej chwili 2025
        profileWorkerStatementRepository.initStatementForUserDisciplineYear(userId, disciplineId, year);

        // zwróć aktualną listę
        listWorkerDisciplines(ListWorkerDisciplinesRequest.newBuilder().setUserId(userId).build(), responseObserver);
    }

    @Override
    @Transactional
    public void removeWorkerDiscipline(RemoveWorkerDisciplineRequest request,
                                       StreamObserver<ListWorkerDisciplinesResponse> responseObserver) {

        Long userId = request.getUserId();
        Long disciplineId = request.getDisciplineId();

        profileWorkerDisciplineRepository.deleteByIdUserIdAndIdDisciplineId(userId, disciplineId);

        listWorkerDisciplines(ListWorkerDisciplinesRequest.newBuilder().setUserId(userId).build(), responseObserver);
    }


    @Override
    @Transactional
    public void getOrCreateStatement(GetOrCreateStatementRequest request,
                                     StreamObserver<GetOrCreateStatementResponse> responseObserver) {

        Long userId = request.getUserId();
        Long disciplineId = request.getDisciplineId();
        int year = request.getEvalYear();

        if (year < 1900 || year > 2100) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid evalYear: " + year)
                    .asRuntimeException());
            return;
        }

        // user musi być przypisany do dyscypliny
        if (!profileWorkerDisciplineRepository.existsByIdUserIdAndIdDisciplineId(userId, disciplineId)) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("Worker is not assigned to discipline: " + disciplineId)
                    .asRuntimeException());
            return;
        }

        var st = profileWorkerStatementRepository
                .findByIdUserIdAndIdDisciplineIdAndIdEvalYear(userId, disciplineId, year)
                .orElseGet(() -> {
                    var created = new org.example.profiles.entities.ProfileWorkerStatement();
                    created.setId(new org.example.profiles.entities.ProfileWorkerStatementId(userId, disciplineId, year));


                    return profileWorkerStatementRepository.save(created);
                });

        WorkerStatement proto = WorkerStatement.newBuilder()
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .setEvalYear(year)
                .setFte(st.getFte().doubleValue())
                .setSharePercent(st.getSharePercent().doubleValue())
                .setSlotInDiscipline(st.getSlotInDiscipline().doubleValue())
                .setMaxSlots(st.getMaxSlots().doubleValue())
                .setMaxMonoSlots(st.getMaxMonoSlots().doubleValue())
                .build();

        GetOrCreateStatementResponse resp = GetOrCreateStatementResponse.newBuilder()
                .setStatement(proto)
                .setResponse(ApiResponse.newBuilder().setCode(200).setMessage("OK").build())
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void adminInitStatementsForYear(
            AdminInitStatementsForYearRequest request,
            StreamObserver<AdminInitStatementsForYearResponse> responseObserver) {

        int year = request.getEvalYear();

        if (year < 1900 || year > 2100) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid evalYear: " + year)
                    .asRuntimeException());
            return;
        }

        int created = profileWorkerStatementRepository.initStatementsForYear(year);

        AdminInitStatementsForYearResponse resp = AdminInitStatementsForYearResponse.newBuilder()
                .setEvalYear(year)
                .setCreatedCount(created)
                .setResponse(ApiResponse.newBuilder()
                        .setCode(200)
                        .setMessage("Initialized statements for year " + year + ". Created: " + created)
                        .build())
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional(readOnly = true)
    public void listAllProfiles(ListAllProfilesRequest request,
                                StreamObserver<ListAllProfilesResponse> responseObserver) {

        String q = request.getFullname() == null ? "" : request.getFullname().trim();

        int page = Math.max(request.getPage(), 0);
        int sizeRaw = request.getSize();
        int size = sizeRaw <= 0 ? 20 : Math.min(sizeRaw, 100);

        Sort sort = buildSort(request.getSortBy(), request.getSortDir());
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProfileUser> result = profileUserRepository.searchByFullname(q, pageable);

        ListAllProfilesResponse.Builder resp = ListAllProfilesResponse.newBuilder()
                .setTotalElements(result.getTotalElements())
                .setTotalPages(result.getTotalPages())
                .setResponse(ApiResponse.newBuilder().setCode(200).setMessage("OK").build());

        for (ProfileUser pu : result.getContent()) {
            AdminProfileListItem.Builder item = AdminProfileListItem.newBuilder()
                    .setUserId(pu.getUserId())
                    .setFullname(nvl(pu.getFullname()));

            if (pu.getWorker() != null) {
                item.setHasWorker(true)
                        .setWorkerDegreeTitle(nvl(pu.getWorker().getDegreeTitle()))
                        .setWorkerUnitName(nvl(pu.getWorker().getUnitName()));
            } else {
                item.setHasWorker(false);
            }

            if (pu.getAdmin() != null) {
                item.setHasAdmin(true)
                        .setAdminUnitName(nvl(pu.getAdmin().getUnitName()));
            } else {
                item.setHasAdmin(false);
            }

            resp.addItems(item);
        }

        responseObserver.onNext(resp.build());
        responseObserver.onCompleted();
    }



    @Override
    @Transactional(readOnly = true)
    public void listDisciplines(Empty request, StreamObserver<ListDisciplineResponse> responseObserver) {

        var disciplines = disciplineRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        ListDisciplineResponse.Builder resp = ListDisciplineResponse.newBuilder()
                .setResponse(ApiResponse.newBuilder().setCode(200).setMessage("OK").build());

        for (var d : disciplines) {
            resp.addDisciplines(DisciplineRef.newBuilder()
                    .setId(d.getId())
                    .setName(d.getName() == null ? "" : d.getName())
                    .build());
        }

        responseObserver.onNext(resp.build());
        responseObserver.onCompleted();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static Sort buildSort(String sortBy, String sortDir) {
        // Dopasuj do NAZW PÓL w encji ProfileUser (nie do kolumn SQL)
        // Najczęściej: "userId", "fullname", "createdAt", "updatedAt"
        Set<String> allowed = Set.of( "fullname", "createdAt", "updatedAt");

        String by = (sortBy == null) ? "fullname" : sortBy.trim();
        if (!allowed.contains(by)) by = "fullname";

        Sort.Direction dir = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, by);
    }




}
