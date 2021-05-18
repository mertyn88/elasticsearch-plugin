package org.elasticsearch.plugin.analysis.example;

import static java.util.Collections.singletonList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.index.analysis.filter.TestTokenFilterFactory;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;

public class TestPlugin extends Plugin implements ActionPlugin, AnalysisPlugin {
    @Override
    public List<RestHandler> getRestHandlers(final Settings settings,
                                             final RestController restController,
                                             final ClusterSettings clusterSettings,
                                             final IndexScopedSettings indexScopedSettings,
                                             final SettingsFilter settingsFilter,
                                             final IndexNameExpressionResolver indexNameExpressionResolver,
                                             final Supplier<DiscoveryNodes> nodesInCluster) {

        return singletonList(new TestAction());
    }

    /*
        {
          "tokenizer": "standard",
          "filter": [
            "test_filter"
          ],
          "text": "단어1 단어2"
        }
     */

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> filters = new HashMap<>();
        filters.put("test_filter", TestTokenFilterFactory::new);
        return filters;
    }

}