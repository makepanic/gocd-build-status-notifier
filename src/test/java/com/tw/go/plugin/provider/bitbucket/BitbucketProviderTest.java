package com.tw.go.plugin.provider.bitbucket;

import com.tw.go.plugin.setting.DefaultPluginSettings;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BitbucketProviderTest {
    DefaultPluginSettings pluginSettings;
    BitbucketProvider provider;

    @Before
    public void setUp() throws Exception {
        pluginSettings = new DefaultPluginSettings();
        provider = new BitbucketProvider();
    }

    @Test
    public void shouldGetRepositoryFromURL() {
        assertThat(provider.getRepository("http://bitbucket.org/foo/bar-repo"), is("foo/bar-repo"));
        assertThat(provider.getRepository("http://bitbucket.org/foo/bar-repo.git"), is("foo/bar-repo"));
        assertThat(provider.getRepository("http://bitbucket.org/foo/bar-repo/"), is("foo/bar-repo"));
        assertThat(provider.getRepository("http://bitbucket.org/foo/bar-repo.git/"), is("foo/bar-repo"));
        assertThat(provider.getRepository("https://bitbucket.org/foo/bar-repo"), is("foo/bar-repo"));
        assertThat(provider.getRepository("https://bitbucket.org/foo/bar-repo.git"), is("foo/bar-repo"));
        assertThat(provider.getRepository("git@code.corp.yourcompany.com:foo/bar-repo"), is("foo/bar-repo"));
        assertThat(provider.getRepository("git@code.corp.yourcompany.com:foo/bar-repo.git"), is("foo/bar-repo"));
        assertThat(provider.getRepository("git@bitbucket.org:foo/bar-repo.git"), is("foo/bar-repo"));
    }

    @Test
    public void shouldGetStateFromResult() {
        assertThat(provider.getState("Unknown"), is(BitbucketCommitState.INPROGRESS));
        assertThat(provider.getState("Passed"), is(BitbucketCommitState.SUCCESSFUL));
        assertThat(provider.getState("Failed"), is(BitbucketCommitState.FAILED));
        assertThat(provider.getState("Cancelled"), is(BitbucketCommitState.STOPPED));
    }

    @Ignore("for local runs")
    @Test
    public void shouldUpdateStatusForPR() throws Exception {
        provider.updateStatus(
                "git@bitbucket.org:munumnu/gocd-build-status-notifier-dummy.git",
                pluginSettings,
                "1",
                "9e2c581baccced3244acd1ce6c6a289dd7bac78a",
                "pipeline-name/stage-name",
                "Passed",
                "http://localhost:8153/go/pipelines/pipeline-name/1/stage-name/1"
        );
    }

    @Test
    public void shouldReturnCorrectTemplate() {
        assertThat(provider.configurationView().templateName(), is("plugin-settings.template.html"));
    }

    @Test
    public void shouldReturnCorrectConfigFields() throws Exception {
        Map<String, Object> configuration = provider.configurationView().fields();

        assertThat(configuration.containsKey("server_base_url"), Is.is(true));
        assertThat(configuration.containsKey("end_point"), Is.is(true));
        assertThat(configuration.containsKey("username"), Is.is(true));
        assertThat(configuration.containsKey("password"), Is.is(true));
        assertThat(configuration.containsKey("oauth_token"), Is.is(true));
    }
}
