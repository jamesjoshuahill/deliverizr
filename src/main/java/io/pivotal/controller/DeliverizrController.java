package io.pivotal.controller;

import io.pivotal.domain.Project;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
public class DeliverizrController {

    @Value("${github.username}")
    private String gitHubUsername;

    @Value("${github.password}")
    private String gitHubPassword;

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String index() {
        return "{}";
    }

    @RequestMapping(path = "/", method = RequestMethod.POST)
    public String create(
            @RequestParam(value="project_name") String projectName
    ) throws IOException, GitAPIException, URISyntaxException {
        Project project = new Project(projectName);
        project.generateCode();

        GitHubClient client = new GitHubClient();
        client.setCredentials(gitHubUsername, gitHubPassword);

        RepositoryService service = new RepositoryService(client);
        Repository remoteRepo = service.createRepository(new Repository()
                .setName(projectName)
                .setOwner(new User().setLogin(client.getUser()))
        );

        Git git = Git.init().setDirectory(project.getCodeRoot().toFile()).call();
        git.add().addFilepattern(".").call();
        git.commit()
                .setAuthor("Deliverizr", "jhill+deliverizr@pivotal.io")
                .setCommitter("Deliverizr", "jhill+deliverizr@pivotal.io")
                .setMessage("Project deliverized")
                .call();

        RemoteAddCommand adder = git.remoteAdd();
        adder.setName("origin");
        adder.setUri(new URIish(remoteRepo.getHtmlUrl())
                .setUser(gitHubUsername)
                .setPass(gitHubPassword));
        adder.call();

        git.push()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                        gitHubUsername, gitHubPassword))
                .setRemote("origin").call();

        return "{\"project_name\":\"" + projectName + "\", \"git_https_url\":\"" + remoteRepo.getHtmlUrl() + "\", \"git_ssh_url\":\"" + remoteRepo.getSshUrl() + "\"}";
    }
}
