package io.pivotal.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ResourceUtils {
    public static String getResourceString(final String resourceName) throws IOException {
        // thanks, http://stackoverflow.com/a/14029464
        Resource resource = new ClassPathResource(resourceName);
        BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()),1024);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            stringBuilder.append(line).append('\n');
        }
        br.close();
        return stringBuilder.toString();
    }

    private ResourceUtils(){
        throw new UnsupportedOperationException();
    }
}
