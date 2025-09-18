package org.example.apigateway.clients;

import com.example.generated.ImportMEiNReply;
import com.example.generated.ImportMEiNRequest;
import com.example.generated.MEiNImportGrpc;
import com.example.generated.ProfilesServiceGrpc;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
public class ArticleClient {

    private static MEiNImportGrpc.MEiNImportBlockingStub stub;

    public static ImportMEiNReply importFile(MultipartFile file, String fileName, String label, long importedBy, boolean activateAfter){
        ByteString fileBytes = null;
        try {
            fileBytes = ByteString.copyFrom(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ImportMEiNRequest request = ImportMEiNRequest.newBuilder()
                .setFile(fileBytes)
                .setFilename(fileName)
                .setLabel(label == null ? "" : label)
                .setImportedBy(importedBy)
                .setActivateAfter(activateAfter)
                .build();

        return stub.importFile(request);
    }

    public static void init(Channel channel) {
        stub = MEiNImportGrpc.newBlockingStub(channel);
    }

}
