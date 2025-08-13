/*
 *
 * Text eXtractor
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.tx;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ExtendedDefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/** Text eXtractor
 * This class is just a dispatcher class for all the 
 * modules dealing with deep learning, language processing, 
 * knowledge generation, etc.
 */

public class Tx {

    @SuppressWarnings("static-access")
    private static final Option moduleOpt = Option.builder("module")
    .desc("module to call")
    .hasArg()
    .argName("module name")
    .build();

    @SuppressWarnings("static-access")
    private static final Option helpOpt = Option.builder("h")
      .longOpt("help")
      .desc("Display the help")
      .build();

    private Properties      moduleProperties;

    public Tx() throws IOException {
        this.moduleProperties = new Properties();
        this.moduleProperties.loadFromXML(this.getClass().getResourceAsStream("txmodules.properties"));
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
    public TxModule processCommandLine(String[] argv, Options options) {

        TxModule module = null;

        // ExtendedDefaultParser just has an empty 
        // handleUnknownToken() method
        CommandLineParser parser = new ExtendedDefaultParser();
        try {
            CommandLine cmdline = parser.parse(options, argv);

            if(cmdline.hasOption(moduleOpt.getOpt())) {
                try {
                    String modName = cmdline.getOptionValue(moduleOpt.getOpt());
                    String clazz = this.moduleProperties.getProperty(modName);
                    if(clazz == null) {
                        System.out.println("Unknown module name provided");
                        return null;
                    }
                    module = (TxModule) Class.forName(clazz).getConstructor().newInstance();
                    module.registerOptions(options);

                } catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            if(cmdline.hasOption(helpOpt.getOpt())) {
                printHelp("Tx", options);
                return null;
            }

        } catch(ParseException e) {
            e.printStackTrace();
            printHelp("Tx", options);
        }
        return module;
    }

        /**
         * quick'n'dirty main method
         */
    public static void main(String[] argv) {

        Options options = new Options();
        options.addOption(moduleOpt);
        options.addOption(helpOpt);

        try {
            Tx tx = new Tx();
            TxModule module = tx.processCommandLine(argv, options);
            if(module != null) {
                module.processCommandLine(argv, options);
            }
        } catch(IOException e) { 
            e.printStackTrace();
        } 
    }
}
