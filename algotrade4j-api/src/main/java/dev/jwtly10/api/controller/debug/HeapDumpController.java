package dev.jwtly10.api.controller.debug;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

@RestController
@Profile("dev")  // Add this annotation
public class HeapDumpController {

    @GetMapping("/generate-heapdump")
    public void generateHeapDump() throws IOException, MalformedObjectNameException, ReflectionException, InstanceNotFoundException, MBeanException {
        String fileName = "heapdump_" + System.currentTimeMillis() + ".hprof";
        File file = new File(fileName);
        ManagementFactory.getPlatformMBeanServer().invoke(
                new ObjectName("com.sun.management:type=HotSpotDiagnostic"),
                "dumpHeap",
                new Object[]{fileName, Boolean.TRUE},
                new String[]{"java.lang.String", "boolean"}
        );
        System.out.println("Heap dump generated: " + file.getAbsolutePath());
    }
}