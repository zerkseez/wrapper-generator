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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * usage: java -cp CLASSPATH
 *          com.github.zerkseez.codegen.wrappergenerator.Main
 *  --classMappings WRAPPEE1:WRAPPER1 WRAPPEE2:WRAPPER2 ...
 *  --outputDirectory OUTPUT_DIRECTORY
 *  
 * @author xerxes
 *
 */
public class Main {
    public static void main(final String[] args) throws Exception {
        final Options options = new Options();
        options.addOption(Option.builder().longOpt("outputDirectory").hasArg().required().build());
        options.addOption(Option.builder().longOpt("classMappings").hasArgs().required().build());

        final CommandLineParser parser = new DefaultParser();

        try {
            final CommandLine line = parser.parse(options, args);
            final String outputDirectory = line.getOptionValue("outputDirectory");
            final String[] classMappings = line.getOptionValues("classMappings");
            for (String classMapping : classMappings) {
                final String[] tokens = classMapping.split(":");
                if (tokens.length != 2) {
                    throw new IllegalArgumentException(
                            String.format("Invalid class mapping format \"%s\"", classMapping));
                }
                final Class<?> wrappeeClass = Class.forName(tokens[0]);
                final String fullWrapperClassName = tokens[1];
                final int indexOfLastDot = fullWrapperClassName.lastIndexOf('.');
                final String wrapperPackageName = (indexOfLastDot == -1) ? ""
                        : fullWrapperClassName.substring(0, indexOfLastDot);
                final String simpleWrapperClassName = (indexOfLastDot == -1) ? fullWrapperClassName
                        : fullWrapperClassName.substring(indexOfLastDot + 1);
                
                System.out.println(String.format("Generating wrapper class for %s...", wrappeeClass));
                final WrapperGenerator generator = new WrapperGenerator(wrappeeClass, wrapperPackageName,
                        simpleWrapperClassName);
                generator.writeTo(outputDirectory, true);
            }
            System.out.println("Done");
        }
        catch (MissingOptionException e) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(String.format("java -cp CLASSPATH %s", Main.class.getName()), options);
        }
    }
}
