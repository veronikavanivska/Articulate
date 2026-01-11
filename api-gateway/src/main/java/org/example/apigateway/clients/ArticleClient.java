package org.example.apigateway.clients;

import com.example.generated.ArticleServiceGrpc;
import com.example.generated.ListCyclesResponse;
import com.example.generated.ListDisciplinesResponse;
import com.example.generated.ListTypesResponse;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.springframework.stereotype.Component;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
@Component
public class ArticleClient {

    private  ArticleServiceGrpc.ArticleServiceBlockingStub stub;

    // dla dropdown
    public  ListTypesResponse listPublicationTypes(){
        return stub.listPublicationTypes(Empty.getDefaultInstance());
    }

    public  ListDisciplinesResponse listDisciplines(){

        return stub.listDisciplines(Empty.getDefaultInstance());
    }

    public  ListCyclesResponse listCycles(){
        return stub.listEvalCycles(Empty.getDefaultInstance());
    }


    public  void init(Channel channel) {
        stub = ArticleServiceGrpc.newBlockingStub(channel);
    }
}
