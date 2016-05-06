package io.pivotal.domain;

import com.google.common.collect.ImmutableMap;
import io.pivotal.utils.ResourceUtils;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Project {
    private Cf cf;
    private Path root;
    private String name;
    private Path codeRoot = null;

    public Project(String name, Cf cf) throws IOException {
        this.root = Files.createTempDirectory("deliverizr");
        System.out.println(root.toAbsolutePath().toString());
        this.name = name;
        this.cf = cf;
    }

    public void generateCode() throws IOException {
        final Path script = Files.createTempFile("deliverizrsh", "");
        script.toFile().setExecutable(true, true);
        System.out.println(script.toAbsolutePath().toString());

        final String basePath = name;

        applyTemplate("scriptTemplate.sh", script, new ImmutableMap.Builder<String,String>( )
                .put("tempdir", this.root.toAbsolutePath().toString())
                .put("basepath", basePath)
                .build()
        );

        final Path stdout = Files.createTempFile("deliverizrout", "");
        final Path stderr = Files.createTempFile("deliverizrerr", "");
        final Process proc = new ProcessBuilder("sh", script.toAbsolutePath().toString())
                .redirectError(stderr.toFile())
                .redirectOutput(stdout.toFile())
                .start();
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        System.out.println("stdout: " + FileUtils.readFileToString(stdout.toFile(), StandardCharsets.UTF_8));
        System.out.println("stderr: " + FileUtils.readFileToString(stderr.toFile(), StandardCharsets.UTF_8));
        stdout.toFile().delete();
        stderr.toFile().delete();
        script.toFile().delete();

        this.codeRoot = root.resolve(basePath);
    }

    protected Path applyTemplate( final String template, final Path target, final Map<String,String> substitutions ) throws IOException {
        final String content = substitute(ResourceUtils.getResourceString(template), substitutions );
        Files.createDirectories(target.getParent());
        FileUtils.writeStringToFile(target.toFile(), content, StandardCharsets.UTF_8);
        return target;
    }

    public void generateManifest() throws IOException {
        String template = ResourceUtils.getResourceString("manifest.yml.template");
        template = substitute(template, new ImmutableMap.Builder<String,String>( )
                .put("project_name", name)
                .build()
        );
        FileUtils.writeStringToFile(codeRoot.resolve("manifest.yml").toFile(), template, StandardCharsets.UTF_8);
    }

    public Path getCodeRoot() {
        return codeRoot;
    }

    private String substitute( final String template, final Map<String,String> substitutions ) {
        String result = template;
        for( final Map.Entry<String,String> i : substitutions.entrySet() ) {
            result = result.replace("{{" + i.getKey() + "}}", i.getValue());
        }
        return result;
    }

    public void generatePipeline(String gitUrl) throws IOException {
        Map<String,String> subs = new ImmutableMap.Builder<String, String>()
                .put("git_url", gitUrl)
                .put("cf_api", cf.getApi())
                .put("cf_username", cf.getUsername())
                .put("cf_org", cf.getOrg())
                .put("cf_space", cf.getSpace())
                .build();

        applyTemplate("pipeline.yml.template", codeRoot.resolve("ci/pipeline.yml"), subs);
        applyTemplate("package.yml.template", codeRoot.resolve("ci/tasks/package.yml"), subs);
        applyTemplate("package.sh.template", codeRoot.resolve("ci/tasks/package.sh"), subs).toFile().setExecutable(true, true);
        applyTemplate("gitignore", codeRoot.resolve(".gitignore"), subs);
        applyTemplate("credentials.yml.example", codeRoot.resolve("ci/credentials.yml.example"), subs);

        Map<String, String> passwordSub = new ImmutableMap.Builder<String, String>()
                .put("cf_password", cf.getPassword())
                .build();

        applyTemplate("credentials.yml.template", codeRoot.resolve("ci/credentials.yml"), passwordSub);
    }

    public void startPipeline() throws IOException {
        final Path script = Files.createTempFile("setpipelinesh", "");
        script.toFile().setExecutable(true, true);
        System.out.println(script.toAbsolutePath().toString());

        applyTemplate("set_pipeline.sh", script, new ImmutableMap.Builder<String, String>()
                .put("tempdir", this.root.toAbsolutePath().toString())
                .put("project_name", name)
                .build()
        );

        final Path stdout = Files.createTempFile("deliverizrout", "");
        final Path stderr = Files.createTempFile("deliverizrerr", "");
        final Process proc = new ProcessBuilder("sh", script.toAbsolutePath().toString())
                .redirectError(stderr.toFile())
                .redirectOutput(stdout.toFile())
                .start();
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        System.out.println("stdout: " + FileUtils.readFileToString(stdout.toFile(), StandardCharsets.UTF_8));
        System.out.println("stderr: " + FileUtils.readFileToString(stderr.toFile(), StandardCharsets.UTF_8));
        stdout.toFile().delete();
        stderr.toFile().delete();
        script.toFile().delete();
    }
}
