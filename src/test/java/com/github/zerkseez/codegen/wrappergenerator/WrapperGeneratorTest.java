/*******************************************************************************
 * Copyright 2016 Xerxes Tsang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.github.zerkseez.codegen.wrappergenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class WrapperGeneratorTest {
    @Test
    public void testWrapMap() throws Exception {
        runTest(Map.class);
    }
    
    @Test
    public void testWrapConnection() throws Exception {
        runTest(Connection.class);
    }
    
    @Test
    public void testWrapInputStream() throws Exception {
        runTest(InputStream.class);
    }

    protected void runTest(final Class<?> wrappee) throws Exception {
        final File tempDir = new File("/home/xerxes/tmp"); //Files.createTempDir();
        final File outputFile = new File(tempDir, String.format("Wrapped%s.java", wrappee.getSimpleName()));
        final WrapperGenerator generator = new WrapperGenerator(wrappee, "");
        generator.writeTo(tempDir.getAbsolutePath(), true);

        final Process process = Runtime.getRuntime().exec(new String[] {
                findJavac(),
                "-source", "1.8",
                "-target", "1.8",
                "-d", tempDir.getAbsolutePath(),
                outputFile.getAbsolutePath()
        });
        try (
                InputStream inputStream = process.getErrorStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.err.println(line);
            }
        }
        process.waitFor();

        Assert.assertTrue(new File(tempDir, String.format("Wrapped%s.class", wrappee.getSimpleName())).exists());
    }
    
    protected String findJavac() {
        final File javaHome = new File(System.getProperty("java.home"));
        if (javaHome.getName().equals("jre")) {
            final File javacDir = new File(javaHome.getParentFile(), "bin");
            final File javacBin = new File(javacDir, "javac");
            if (javacBin.exists()) {
                return javacBin.getAbsolutePath();
            }
        }
        throw new RuntimeException("Unable to locate javac");
    }
}
