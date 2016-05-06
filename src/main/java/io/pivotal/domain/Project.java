package io.pivotal.domain;

import com.google.common.collect.ImmutableMap;
import io.pivotal.utils.ResourceUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Project {
    private final Path root;
    private final String name;
    private Path codeRoot = null;

    @Autowired
    ResourceLoader resourceLoader;

    public Project(String name) throws IOException {
        this.root = Files.createTempDirectory("deliverizr");
        System.out.println(root.toAbsolutePath().toString());
        this.name = name;
    }

    public void generateCode() throws IOException {
        final Path script = Files.createTempFile("deliverizrsh", "");
        script.toFile().setExecutable(true, true);
        System.out.println(script.toAbsolutePath().toString());

        final String basePath = name;

        String template = ResourceUtils.getResourceString("scriptTemplate.sh");
        template = substitute(template, new ImmutableMap.Builder<String,String>( )
                        .put("tempdir", this.root.toAbsolutePath().toString())
                        .put("basepath", basePath)
                        .build()
        );
        FileUtils.writeStringToFile(script.toFile(), template, StandardCharsets.UTF_8);

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
}
