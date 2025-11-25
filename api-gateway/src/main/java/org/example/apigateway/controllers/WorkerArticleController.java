package org.example.apigateway.controllers;

import com.example.generated.DeletePublicationRequest;
import com.example.generated.PublicationView;
import org.example.apigateway.clients.WorkerArticleClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.requests.articles.ListPublicationRequest;
import org.example.apigateway.requests.articles.UpdatePublicationRequest;
import org.example.apigateway.responses.ApiResponse;
import org.example.apigateway.responses.articles.*;
import org.springframework.web.bind.annotation.*;
import org.example.apigateway.requests.articles.CreatePublicationRequest;

import java.util.ArrayList;
import java.util.List;

//TODO: need to replace the authors and need to add the mapper for response
@RestController
@RequestMapping("/article")
public class WorkerArticleController {

    @PostMapping("/worker/createPublication")
    public PublicationViewResponse createPublication(@RequestBody CreatePublicationRequest request) {
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerArticleClient.createPublication(userId, request.getTypeId(), request.getDisciplineId(), request.getTitle(), request.getDoi(), request.getIssn(), request.getEissn(), request.getJournalTitle(), request.getPublicationYear(), request.getCoauthors() );

        PublicationViewResponse publicationResponse = new PublicationViewResponse();

        RefItem discipline = new RefItem();
        discipline.setId(response.getDiscipline().getId());
        discipline.setName(response.getDiscipline().getName());

        RefItem type = new RefItem();
        type.setId(response.getType().getId());
        type.setName(response.getType().getName());

        CycleItem cycle = new CycleItem();
        cycle.setId(response.getCycle().getId());
        cycle.setName(response.getCycle().getName());
        cycle.setActive(response.getCycle().getIsActive());
        cycle.setYearTo(response.getCycle().getYearTo());
        cycle.setYearFrom(response.getCycle().getYearFrom());
        cycle.setMeinVersionId(response.getCycle().getMeinVersionId());
        List<Coauthor> coauthors = new ArrayList<>();

        for (com.example.generated.Coauthor c : response.getCoauthorsList()) {
            Coauthor coauthor = new Coauthor();
            coauthor.setPosition(c.getPosition());
            coauthor.setFullName(c.getFullName());
            coauthors.add(coauthor);
        }

        publicationResponse.setId(response.getId());
        publicationResponse.setDiscipline(discipline);
        publicationResponse.setType(type);
        publicationResponse.setCycle(cycle);
        publicationResponse.setPublicationYear(response.getPublicationYear());
        publicationResponse.setIssn(response.getIssn());
        publicationResponse.setJournalTitle(response.getJournalTitle());
        publicationResponse.setEissn(response.getEissn());
        publicationResponse.setDoi(response.getDoi());
        publicationResponse.setCoauthors(coauthors);
        publicationResponse.setMeinVersionId(response.getMeinVersionId());
        publicationResponse.setMeinJournalId(response.getMeinJournalId());
        publicationResponse.setMeinPoints(response.getMeinPoints());
        publicationResponse.setOwnerId(response.getOwnerId());
        publicationResponse.setTitle(response.getTitle());

        return publicationResponse;
    }

    @GetMapping("/worker/getPublication")
    public PublicationViewResponse getPublication(@RequestParam  long publicationId) {
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerArticleClient.getPublication(userId, publicationId);

        PublicationViewResponse publicationResponse = new PublicationViewResponse();

        RefItem discipline = new RefItem();
        discipline.setId(response.getDiscipline().getId());
        discipline.setName(response.getDiscipline().getName());

        RefItem type = new RefItem();
        type.setId(response.getType().getId());
        type.setName(response.getType().getName());

        CycleItem cycle = new CycleItem();
        cycle.setId(response.getCycle().getId());
        cycle.setName(response.getCycle().getName());
        cycle.setActive(response.getCycle().getIsActive());
        cycle.setYearTo(response.getCycle().getYearTo());
        cycle.setYearFrom(response.getCycle().getYearFrom());
        cycle.setMeinVersionId(response.getCycle().getMeinVersionId());
        List<Coauthor> coauthors = new ArrayList<>();

        for (com.example.generated.Coauthor c : response.getCoauthorsList()) {
            Coauthor coauthor = new Coauthor();
            coauthor.setPosition(c.getPosition());
            coauthor.setFullName(c.getFullName());
            coauthors.add(coauthor);
        }

        publicationResponse.setId(response.getId());
        publicationResponse.setDiscipline(discipline);
        publicationResponse.setType(type);
        publicationResponse.setCycle(cycle);
        publicationResponse.setPublicationYear(response.getPublicationYear());
        publicationResponse.setIssn(response.getIssn());
        publicationResponse.setJournalTitle(response.getJournalTitle());
        publicationResponse.setEissn(response.getEissn());
        publicationResponse.setDoi(response.getDoi());
        publicationResponse.setCoauthors(coauthors);
        publicationResponse.setMeinVersionId(response.getMeinVersionId());
        publicationResponse.setMeinJournalId(response.getMeinJournalId());
        publicationResponse.setMeinPoints(response.getMeinPoints());
        publicationResponse.setOwnerId(response.getOwnerId());
        publicationResponse.setTitle(response.getTitle());

        return publicationResponse;

    }

    @GetMapping("/worker/listMyPublication")
    public ListPublicationResponse listMyPublications(@RequestBody ListPublicationRequest request){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerArticleClient.listMyPublications(userId, request.getTypeId(), request.getDisciplineId(),request.getCycleId(), request.getPage(), request.getSize(), request.getSortBy(), request.getSortDir());

        ListPublicationResponse publicationResponse = new ListPublicationResponse();

        List<PublicationViewResponse> publicationView = new ArrayList<>();
        for(PublicationView view : response.getItemsList()){

            PublicationViewResponse publicationViewResponse = new PublicationViewResponse();

            RefItem discipline = new RefItem();
            discipline.setId(view.getDiscipline().getId());
            discipline.setName(view.getDiscipline().getName());

            RefItem type = new RefItem();
            type.setId(view.getType().getId());
            type.setName(view.getType().getName());

            CycleItem cycle = new CycleItem();
            cycle.setId(view.getCycle().getId());
            cycle.setName(view.getCycle().getName());
            cycle.setActive(view.getCycle().getIsActive());
            cycle.setYearTo(view.getCycle().getYearTo());
            cycle.setYearFrom(view.getCycle().getYearFrom());
            cycle.setMeinVersionId(view.getCycle().getMeinVersionId());
            List<Coauthor> coauthors = new ArrayList<>();

            for (com.example.generated.Coauthor c : view.getCoauthorsList()) {
                Coauthor coauthor = new Coauthor();
                coauthor.setPosition(c.getPosition());
                coauthor.setFullName(c.getFullName());
                coauthors.add(coauthor);
            }

            publicationViewResponse.setId(view.getId());
            publicationViewResponse.setDiscipline(discipline);
            publicationViewResponse.setType(type);
            publicationViewResponse.setCycle(cycle);
            publicationViewResponse.setPublicationYear(view.getPublicationYear());
            publicationViewResponse.setIssn(view.getIssn());
            publicationViewResponse.setJournalTitle(view.getJournalTitle());
            publicationViewResponse.setEissn(view.getEissn());
            publicationViewResponse.setDoi(view.getDoi());
            publicationViewResponse.setCoauthors(coauthors);
            publicationViewResponse.setMeinVersionId(view.getMeinVersionId());
            publicationViewResponse.setMeinJournalId(view.getMeinJournalId());
            publicationViewResponse.setMeinPoints(view.getMeinPoints());
            publicationViewResponse.setOwnerId(view.getOwnerId());
            publicationViewResponse.setTitle(view.getTitle());

            publicationView.add(publicationViewResponse);
        }

        PageMeta pageMeta = new PageMeta();
        pageMeta.setPage(response.getPage().getPage());
        pageMeta.setSize(response.getPage().getSize());
        pageMeta.setTotalPages(response.getPage().getTotalPages());
        pageMeta.setTotalItems(response.getPage().getTotalItems());

        publicationResponse.setPublications(publicationView);
        publicationResponse.setPageMeta(pageMeta);

        return publicationResponse;
    }

    @PatchMapping("/worker/updatePublication")
    public PublicationViewResponse updatePublication(@RequestBody UpdatePublicationRequest request){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
        var response = WorkerArticleClient.updatePublication(request.getId(), userId , request.getTypeId(), request.getDisciplineId(), request.getTitle(), request.getDoi(), request.getIssn(), request.getEissn(), request.getJournalTitle(), request.getPublicationYear(),request.getReplaceCoauthors());

        PublicationViewResponse publicationResponse = new PublicationViewResponse();

        RefItem discipline = new RefItem();
        discipline.setId(response.getDiscipline().getId());
        discipline.setName(response.getDiscipline().getName());

        RefItem type = new RefItem();
        type.setId(response.getType().getId());
        type.setName(response.getType().getName());

        CycleItem cycle = new CycleItem();
        cycle.setId(response.getCycle().getId());
        cycle.setName(response.getCycle().getName());
        cycle.setActive(response.getCycle().getIsActive());
        cycle.setYearTo(response.getCycle().getYearTo());
        cycle.setYearFrom(response.getCycle().getYearFrom());
        cycle.setMeinVersionId(response.getCycle().getMeinVersionId());
        List<Coauthor> coauthors = new ArrayList<>();

        for (com.example.generated.Coauthor c : response.getCoauthorsList()) {
            Coauthor coauthor = new Coauthor();
            coauthor.setPosition(c.getPosition());
            coauthor.setFullName(c.getFullName());
            coauthors.add(coauthor);
        }

        publicationResponse.setId(response.getId());
        publicationResponse.setDiscipline(discipline);
        publicationResponse.setType(type);
        publicationResponse.setCycle(cycle);
        publicationResponse.setPublicationYear(response.getPublicationYear());
        publicationResponse.setIssn(response.getIssn());
        publicationResponse.setJournalTitle(response.getJournalTitle());
        publicationResponse.setEissn(response.getEissn());
        publicationResponse.setDoi(response.getDoi());
        publicationResponse.setCoauthors(coauthors);
        publicationResponse.setMeinVersionId(response.getMeinVersionId());
        publicationResponse.setMeinJournalId(response.getMeinJournalId());
        publicationResponse.setMeinPoints(response.getMeinPoints());
        publicationResponse.setOwnerId(response.getOwnerId());
        publicationResponse.setTitle(response.getTitle());

        return publicationResponse;
    }

    @DeleteMapping("/worker/deletePublication")
    public ApiResponse deletePublication(@RequestParam long publicationId){

        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerArticleClient.deletePublication( publicationId, userId);

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());

        return apiResponse;
    }



}
