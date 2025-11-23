package org.example.apigateway.controllers;

import org.example.apigateway.clients.ETLArticleClient;
import org.example.apigateway.clients.ETLMonoClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.responses.MEiNResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/etl")
public class ETLMonoController {

    @PostMapping("/admin/importPDF")
    public MEiNResponse importPDF(@RequestParam("file") MultipartFile file,
                                   @RequestParam("label") String label){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
        var response = ETLMonoClient.importFile(file, file.getName(), label, userId);

        MEiNResponse meinResponse = new MEiNResponse();
        meinResponse.setVersion_id(response.getVersionId());
        meinResponse.setAlreadyImported(response.getAlreadyImported());

        return meinResponse;
    }

}
