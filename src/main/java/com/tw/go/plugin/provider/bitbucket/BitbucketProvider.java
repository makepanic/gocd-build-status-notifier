package com.tw.go.plugin.provider.bitbucket;

import com.google.gson.GsonBuilder;
import com.tw.go.plugin.provider.DefaultProvider;
import com.tw.go.plugin.setting.DefaultPluginConfigurationView;
import com.tw.go.plugin.setting.PluginSettings;
import com.tw.go.plugin.util.AuthenticationType;
import com.tw.go.plugin.util.HTTPClient;
import com.tw.go.plugin.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BitbucketProvider extends DefaultProvider {
    public static final String PLUGIN_ID = "bitbucket.pr.status";
    // depend on git pull request material
    // @see https://github.com/ashwanthkumar/gocd-build-github-pull-requests/blob/master/src/main/java/in/ashwanthkumar/gocd/github/provider/git/GitProvider.java
    public static final String BITBUCKET_PR_POLLER_PLUGIN_ID = "git.fb";

    private HTTPClient httpClient;

    public BitbucketProvider() {
        super(new DefaultPluginConfigurationView());
        httpClient = new HTTPClient();
    }

    @Override
    public String pluginId() {
        return PLUGIN_ID;
    }

    @Override
    public String pollerPluginId() {
        return BITBUCKET_PR_POLLER_PLUGIN_ID;
    }

    @Override
    public void updateStatus(String url, PluginSettings pluginSettings, String branch, String revision, String pipelineStage, String result, String trackbackURL) throws Exception {
        String repository = getRepository(url);

        String endPointToUse = pluginSettings.getEndPoint();
        String usernameToUse = pluginSettings.getUsername();
        String passwordToUse = pluginSettings.getPassword();

        if (StringUtils.isEmpty(endPointToUse)) {
            endPointToUse = System.getProperty("go.plugin.build.status.bitbucket.endpoint");
        }
        if (StringUtils.isEmpty(usernameToUse)) {
            usernameToUse = System.getProperty("go.plugin.build.status.bitbucket.username");
        }
        if (StringUtils.isEmpty(passwordToUse)) {
            passwordToUse = System.getProperty("go.plugin.build.status.bitbucket.password");
        }

        String updateURL = endPointToUse + "/2.0/repositories/" + repository + "/commit/" + revision + "/statuses/build";

        Map<String, String> params = new HashMap<String, String>();
        params.put("state", getState(result).status());
        params.put("key", pipelineStage);
        params.put("url", trackbackURL);
        String requestBody = new GsonBuilder().create().toJson(params);

        httpClient.postRequest(updateURL, AuthenticationType.BASIC, usernameToUse, passwordToUse, requestBody);
    }

    @Override
    public List<Map<String, Object>> validateConfig(Map<String, Object> fields) {
        return new ArrayList<Map<String, Object>>();
    }

    String getRepository(String url) {
        String[] urlParts = url.split("/");
        String repo = urlParts[urlParts.length - 1];
        String usernameWithSSHPrefix = urlParts[urlParts.length - 2];
        int positionOfColon = usernameWithSSHPrefix.lastIndexOf(":");
        if (positionOfColon > 0) {
            usernameWithSSHPrefix = usernameWithSSHPrefix.substring(positionOfColon + 1);
        }

        String urlWithoutPrefix = String.format("%s/%s", usernameWithSSHPrefix, repo);
        if (urlWithoutPrefix.endsWith(".git")) return urlWithoutPrefix.substring(0, urlWithoutPrefix.length() - 4);
        else return urlWithoutPrefix;
    }

    BitbucketCommitState getState(String result) {
        result = result == null ? "" : result;
        BitbucketCommitState state = BitbucketCommitState.INPROGRESS;

        if (result.equalsIgnoreCase("Passed")) {
            state = BitbucketCommitState.SUCCESSFUL;
        } else if (result.equalsIgnoreCase("Failed")) {
            state = BitbucketCommitState.FAILED;
        } else if (result.equalsIgnoreCase("Cancelled")) {
            state = BitbucketCommitState.STOPPED;
        }

        return state;
    }
}
