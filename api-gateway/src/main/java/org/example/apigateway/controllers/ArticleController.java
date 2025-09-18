package org.example.apigateway.controllers;

import org.example.apigateway.clients.ArticleClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.responses.MEiNResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/article")
public class ArticleController {


    @PostMapping("/admin/import")
    public MEiNResponse importMEiN(  @RequestParam("file") MultipartFile file,
                                     @RequestParam("label") String label,
                                     @RequestParam(value = "activateAfter", defaultValue = "true") boolean activateAfter
    ){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
        var response = ArticleClient.importFile(file, file.getName(), label,userId,activateAfter);

        MEiNResponse meinResponse = new MEiNResponse();
        meinResponse.setVersion_id(response.getVersionId());
        meinResponse.setAlreadyImported(response.getAlreadyImported());

        return meinResponse;
    }
}
