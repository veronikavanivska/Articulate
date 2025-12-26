package org.example.apigateway.clients;

import com.example.generated.ArticleServiceGrpc;
import com.example.generated.ListCyclesResponse;
import com.example.generated.ListDisciplinesResponse;
import com.example.generated.ListTypesResponse;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import org.example.apigateway.Client;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
public class ArticleClient {

    private static ArticleServiceGrpc.ArticleServiceBlockingStub stub;

    // dla dropdown
    public static ListTypesResponse listPublicationTypes(){
        return stub.listPublicationTypes(Empty.getDefaultInstance());
    }

    public static ListDisciplinesResponse listDisciplines(){

        return stub.listDisciplines(Empty.getDefaultInstance());
    }

    public static ListCyclesResponse listCycles(){
        return stub.listEvalCycles(Empty.getDefaultInstance());
    }


    public static void init(Channel channel) {
        stub = ArticleServiceGrpc.newBlockingStub(channel);
    }
}
