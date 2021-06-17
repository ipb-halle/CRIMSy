/*
 * CRIMSy Agency
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
package de.ipb_halle.lbac.device.job;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/** 
 * CRIMSy Agency is the job scheduler for CRIMSy. As CRIMSy 
 * usually runs in a DMZ, it has no access to internal 
 * resources of the institution where it is installed. 
 * Therefore, an agent is needed, which polls CRIMSy in 
 * regular intervalls to process various jobs outside 
 * the CRIMSy universe. 
 */

public class Agency {

    private final static String DEFAULT_PASSWORD_FILE = "secret.txt";
    private final static String DEFAULT_SCRIPT = "agent.sh";

    @SuppressWarnings("static-access")
    private static final Option helpOpt = Option.builder("h")
      .longOpt("help")
      .desc("Display the help")
      .build();

    @SuppressWarnings("static-access")
    private static final Option passwordOpt = Option.builder("p")
      .longOpt("password")
      .desc("Path to a file containing the credential for accessing CRIMSY. The password is read from the first line not beginning with '#'. File name defaults to '" + DEFAULT_PASSWORD_FILE + "'")
      .hasArg()
      .argName("FILE")
      .build();

    @SuppressWarnings("static-access")
    private static final Option queuesOpt = Option.builder("q")
      .longOpt("queues")
      .desc("List of queues this instance of CRMSy Agency should handle. Defaults to all existing queues. Mutually exclusive with --types.")
      .hasArgs()
      .argName("QUEUE ...")
      .build();

    @SuppressWarnings("static-access")
    private static final Option scriptOpt = Option.builder("s")
      .longOpt("script")
      .desc("Path to the shell script for job handling, defaults to '" + DEFAULT_SCRIPT + "'")
      .hasArg()
      .argName("SCRIPT")
      .build();

    @SuppressWarnings("static-access")
    private static final Option typesOpt = Option.builder("t")
      .longOpt("types")
      .desc("List of job types, this instance of CRIMSy Agency should handle. Defaults to all existing job types. Mutually exclusive with --queues.")
      .hasArgs()
      .argName("TYPE ...")
      .build();

    @SuppressWarnings("static-access")
    private static final Option urlOpt = Option.builder("u")
    .longOpt("url")
    .desc("URL of the CRIMSy REST endpoint")
    .hasArg()
    .argName("URL")
    .build();

    private String script;
    private String secret;
    private String url;
    private List<String> queues;
    private List<JobType> jobtypes;

    /**
     * default constructor
     */
    public Agency() { 
        this.script = DEFAULT_SCRIPT;
        this.jobtypes = new ArrayList<JobType> ();
        this.queues = new ArrayList<String> ();
    }

    /**
     * read a secret from file
     */
    private void readSecret(String fileName) throws IOException {
        BufferedReader rd = new BufferedReader(new FileReader(fileName));
        String line = rd.readLine();
        while ((line != null) && (line.startsWith("#"))) {
            line = rd.readLine();
        }
        if ((line == null) || (line.length() < 8)) {
            throw new RuntimeException("Could not read password (>= 8 chars)");
        } 
        this.secret = line;
        rd.close();
    }

    /**
     * print some help text
     */
    public static void printHelp(String module, Options options) {
        HelpFormatter writer = new HelpFormatter();
        writer.printHelp(module, options);
    }

    /**
     * process the command line. If a "module" is specified,
     * register the "modules" options and return a instance of 
     * the "module" class.
     */
    public void processCommandLine(String[] argv, Options options) throws Exception {

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdline = parser.parse(options, argv);

            if (cmdline.hasOption(helpOpt.getOpt())) {
                printHelp("CRIMSY Agency", options);
                return;
            }

            if (cmdline.hasOption(passwordOpt.getOpt())) {
                readSecret(cmdline.getOptionValue(passwordOpt.getOpt()));
            } else {
                readSecret(DEFAULT_PASSWORD_FILE);
            }

            if (cmdline.hasOption(queuesOpt.getOpt())) {
                if (cmdline.hasOption(typesOpt.getOpt())) {
                    printHelp("ERROR: Mutually exclusive options.", options);
                    return;
                }
                this.queues = Arrays.asList(cmdline.getOptionValues(queuesOpt.getOpt()));
            }

            if (cmdline.hasOption(scriptOpt.getOpt())) {
                this.script =  cmdline.getOptionValue(scriptOpt.getOpt());
            }

            if (cmdline.hasOption(typesOpt.getOpt())) {
                if (cmdline.hasOption(queuesOpt.getOpt())) {
                    printHelp("ERROR: Mutually exclusive options.", options);
                    return;
                }
                for(String t : cmdline.getOptionValues(typesOpt.getOpt())) {
                    JobType jt = JobType.valueOf(t);
                    if (jt != null) {
                        this.jobtypes.add(jt);
                    }
                }
            }

            if (cmdline.hasOption(urlOpt.getOpt())) {
                this.url = cmdline.getOptionValue(urlOpt.getOpt());
            }

            new Handler()
                .setURL(this.url)
                .setSecret(this.secret)
                .setScript(this.script)
                .setJobTypes(this.jobtypes)
                .setQueues(this.queues)
                .start();

        } catch(ParseException e) {
            e.printStackTrace();
            printHelp("Parse error.", options);
        }
    }

    /**
     * quick'n'dirty main method
     */
    public static void main(String[] argv) {

        Options options = new Options();
        options.addOption(helpOpt);
        options.addOption(passwordOpt);
        options.addOption(queuesOpt);
        options.addOption(scriptOpt);
        options.addOption(typesOpt);
        options.addOption(urlOpt);

        try {
            Agency agency = new Agency();
            agency.processCommandLine(argv, options);
        } catch(Exception e) { 
            e.printStackTrace();
        } 
    }
}
