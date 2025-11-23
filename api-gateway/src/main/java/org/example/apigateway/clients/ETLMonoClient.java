package org.example.apigateway.clients;

import com.example.generated.ETLArticleServiceGrpc;
import com.example.generated.ETLMonoServiceGrpc;
import com.example.generated.ImportMEiNReply;
import com.example.generated.ImportMEiNRequest;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
public class ETLMonoClient {

    private static ETLMonoServiceGrpc.ETLMonoServiceBlockingStub stub;

    public static ImportMEiNReply importFile(MultipartFile file, String fileName, String label, long importedBy){

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
                .build();

        return stub.importFile(request);
    }
    public static void init(Channel channel) {
        stub = ETLMonoServiceGrpc.newBlockingStub(channel);
    }

}
