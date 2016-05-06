package io.pivotal.controller;

import io.pivotal.domain.Project;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class DeliverizrController {

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String index() {
        return "{}";
    }

    @RequestMapping(path = "/", method = RequestMethod.POST)
    public String create(
            @RequestParam(value="project_name") String projectName
    ) throws IOException {
        Project project = new Project(projectName);
        project.generateCode();

//        GitRepository repo = GitHubClient.createRepository();
//        repo.push(project.getRoot());

        return "{\"project_name\":\"" + projectName + "\"}";
    }
}
