package org.elasticsearch.plugin.example;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

import java.util.List;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

/**
 * Example action with a plugin.
 */
public class TestAction extends BaseRestHandler {

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public List<Route> routes() {
        return unmodifiableList(asList(
              new Route(POST, "/_test")
            , new Route(GET, "/_test")
            )
        );
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) {
        System.out.println("Call prepareRequest method");
        return channel -> channel.sendResponse(new BytesRestResponse(RestStatus.OK, "application/json","{ \"returnStatus\" : 200, \"returnCode\" : \"SUCCESS\", \"returnMessage\" : \"\" }"));
    }
}