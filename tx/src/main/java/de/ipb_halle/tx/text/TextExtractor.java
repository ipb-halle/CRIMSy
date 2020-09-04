/*
 * Text eXtractor
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
 *
 */
package de.ipb_halle.tx.text;

import de.ipb_halle.tx.TxModule;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class TextExtractor implements TxModule {

    @SuppressWarnings("static-access")
    private static final Option dumpTermVectorOpt = Option.builder("dtv")
      .longOpt("dumpTermVector")
      .desc("dump term vector")
      .build();

    @SuppressWarnings("static-access")
    private static final Option filterOpt = Option.builder("f")
      .longOpt("filter")
      .desc("filter definistion (JSON serialized)")
      .hasArg()
      .argName("path")
      .build();

    @SuppressWarnings("static-access")
    private static final Option inputOpt = Option.builder("i")
      .longOpt("input")
      .desc("input file")
      .hasArg()
      .argName("path")
      .build();

    @SuppressWarnings("static-access")
    private static final Option outputOpt = Option.builder("o")
      .longOpt("output")
      .desc("output file")
      .hasArg()
      .argName("path")
      .build();

    private ParseTool           parseTool;

    /**
     * default constructor
     */
    public TextExtractor() {
        this.parseTool = new ParseTool();
        this.parseTool.setOutputStream(System.out);
    }

    /**
     * Dump the TermVector from filterData
     */
    @SuppressWarnings("unchecked")
    private void dumpTermVector() {
        for(Map.Entry<String, Integer> e 
          : ((Map<String, Integer>) this.parseTool
                .getFilterData()
                .getValue(TermVectorFilter.TERM_VECTOR))
                .entrySet()) {

            System.out.printf("%6d\t%s\n", e.getValue(), e.getKey());
        }

        for(Map.Entry<String, String> e
          : ((Map<String, String>) this.parseTool
                .getFilterData()
                .getValue(TermVectorFilter.STEM_DICT))
                .entrySet()) {
            System.out.printf("%24s\t %s\n", e.getValue(), e.getKey());
        }
    }

    /**
     * register commandline options
     */
    public void registerOptions(Options options) {
        options.addOption(dumpTermVectorOpt);
        options.addOption(filterOpt);
        options.addOption(inputOpt);
        options.addOption(outputOpt);
    }

    /**
     * process the commandline options
     */
    public void processCommandLine(String[] argv, Options options) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdline = parser.parse(options, argv);

            if (cmdline.hasOption(inputOpt.getOpt())) {
                this.parseTool.setInputStream( 
                        new FileInputStream(cmdline.getOptionValue(inputOpt.getOpt())));
            } else {
                System.out.println("Please specifiy an input file: --index PATH");
                return;
            }

            if (cmdline.hasOption(filterOpt.getOpt())) {
                this.parseTool.setFilterDefinition(
                        new FileInputStream(cmdline.getOptionValue(filterOpt.getOpt()))); 
            } else {
                System.out.println("Please specify a JSON filter description: --filter PATH");
                return;
            }

            if (cmdline.hasOption(outputOpt.getOpt())) {
                this.parseTool.setOutputStream(
                        new FileOutputStream(cmdline.getOptionValue(outputOpt.getOpt())));
            } 

            this.parseTool.initFilter();
            this.parseTool.parse();

            if (cmdline.hasOption(dumpTermVectorOpt.getOpt())) {
                dumpTermVector();
            }

        } catch(ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter writer = new HelpFormatter();
            writer.printHelp("TextExtractor", options);
        } catch(IOException e) {
            throw new IOError(e);
        }
    }
}
