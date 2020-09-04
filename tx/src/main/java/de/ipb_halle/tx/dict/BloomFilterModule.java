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
package de.ipb_halle.tx.dict;

import de.ipb_halle.tx.TxModule;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.IOError;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class BloomFilterModule implements TxModule {

    @SuppressWarnings("static-access")
    private static final Option createOpt = Option.builder("c")
      .longOpt("create")
      .desc("create a Bloom filter and fill it from stdin; write filter to stdout")
      .build();

    @SuppressWarnings("static-access")
    private static final Option filterOpt = Option.builder("f")
      .longOpt("filter")
      .desc("input or output file for the filter")
      .hasArg()
      .argName("path")
      .build();

    @SuppressWarnings("static-access")
    private static final Option nKeysOpt = Option.builder("k")
      .longOpt("nKeys")
      .desc("number of hash functions to apply")
      .hasArg()
      .argName("num")
      .build();

    @SuppressWarnings("static-access")
    private static final Option sizeOpt = Option.builder("s")
      .longOpt("size")
      .desc("size of the Bloom filter (2^num bits)")
      .hasArg()
      .argName("num")
      .build();

    @SuppressWarnings("static-access")
    private static final Option testOpt = Option.builder("t")
      .longOpt("test")
      .desc("reads lines from stdin and checks if the value is present in the filter. Returns 'true' or 'false' on stdout.")
      .build();
    
    /**
     * create a BloomFilter and fill it from System.in
     */
    private void createBloomFilter(int size, int nKeys, OutputStream os) throws IOException {
        BloomFilter bf = BloomFilter.getBloomFilter(size, nKeys);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        while ((line != null) && (line.length() > 0)) {
            bf.addValue(line);
            line = reader.readLine();
        }
        os.write(bf.toString().getBytes());
        os.close();
    }

    /**
     * test the Bloom filter with values from stdin
     */
    private void testBloomFilter(InputStream is) throws IOException {
        BloomFilter bf = BloomFilter.fromInputStream(is);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String line = reader.readLine();
        while ((line != null) && (line.length() > 0)) {
            System.out.println(bf.checkValue(line) ? "true" : "false");
            line = reader.readLine();
        }
    }

    /**
     * register commandline options
     */
    public void registerOptions(Options options) {
        options.addOption(createOpt);
        options.addOption(filterOpt);
        options.addOption(nKeysOpt);
        options.addOption(sizeOpt);
        options.addOption(testOpt);
    }

    /**
     * process the commandline options
     */
    public void processCommandLine(String[] argv, Options options) {
        int nKeys = 0;
        int size = 0;
        InputStream inputStream = null;
        OutputStream outputStream = System.out;

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdline = parser.parse(options, argv);

            if (cmdline.hasOption(nKeysOpt.getOpt())) {
                nKeys = Integer.parseInt(cmdline.getOptionValue(nKeysOpt.getOpt()));
            }

            if (cmdline.hasOption(sizeOpt.getOpt())) {
                size = Integer.parseInt(cmdline.getOptionValue(sizeOpt.getOpt()));
            }

            if (cmdline.hasOption(createOpt.getOpt())) {
                if (cmdline.hasOption(testOpt.getOpt())) {
                    System.err.println("--create and --test are mutually exclusive");
                    return;
                }
                if ((size == 0) || (nKeys == 0)) {
                    System.err.println("must specify --size and --nKeys with --create");
                    return;
                }
                if (cmdline.hasOption(filterOpt.getOpt())) {
                    outputStream = new FileOutputStream(cmdline.getOptionValue(filterOpt.getOpt()));
                } 
                createBloomFilter(size, nKeys, outputStream);
                return;
            }

            if (cmdline.hasOption(testOpt.getOpt())) {
                if (cmdline.hasOption(filterOpt.getOpt())) {
                    inputStream = new FileInputStream(cmdline.getOptionValue(filterOpt.getOpt()));
                } else {
                    System.err.println("must provide --filter option");
                    return;
                }
                testBloomFilter(inputStream);
                return;
            }

        } catch(ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter writer = new HelpFormatter();
            writer.printHelp("BloomFilterModule", options);
        } catch(IOException e) {
            throw new IOError(e);
        }
    }
}
