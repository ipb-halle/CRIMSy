/*
 * Cloud Resource & Information Management System (CRIMSy)
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
package de.ipb_halle.migration;

import com.rtfparserkit.parser.RtfListenerAdaptor;
import com.rtfparserkit.parser.RtfStringSource;
import com.rtfparserkit.parser.raw.RawRtfParser;
import com.rtfparserkit.rtf.Command;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * RTF-Parser
 * This parser can extract text data from files exported from 
 * ChemFinder(tm) or MicroSoft(R) Access(tm). In these files 
 * plain text records (UTF-8) are mixed with records containing 
 * RTF. This class converts all text to UTF-8 HTML, taking 
 * special cases (like greek letters) into account. However,
 * some conversion (see below) (and manual curation) of the raw 
 * export is necessary.
 * 
 * Data preparation: conversion of 8bit characters
 * 
 *     iconv -f ISO8859-1 -t UTF-8 -o OUTPUT.txt ../INPUT.txt
 *
 * followed by manual curation.
 */
public class RTF extends RtfListenerAdaptor {

    public enum MODE { FONTDEF, IGNORE, TEXTOUT };
    public enum LANG { NORMAL, GREEK };

    private String                  currentFont;
    private int                     depth;
    private Map<String, LANG>       fontmap;
    private static Map<Character, String>   greekmap;
    private LANG                    lang;
    private MODE                    mode;
    private String                  nosupersub;
    private StringBuilder           outString;
        
    static {
        RTF.greekmap = new HashMap<Character, String> ();
        RTF.greekmap.put(Character.valueOf('a'), "&#945;");
        RTF.greekmap.put(Character.valueOf('b'), "&#946;");
        RTF.greekmap.put(Character.valueOf('g'), "&#947;");
        RTF.greekmap.put(Character.valueOf('d'), "&#948;");
        RTF.greekmap.put(Character.valueOf('D'), "&#916;");
        RTF.greekmap.put(Character.valueOf('0'), "0");
        RTF.greekmap.put(Character.valueOf('1'), "1");
        RTF.greekmap.put(Character.valueOf('2'), "2");
        RTF.greekmap.put(Character.valueOf('3'), "3");
        RTF.greekmap.put(Character.valueOf('4'), "4");
        RTF.greekmap.put(Character.valueOf('5'), "5");
        RTF.greekmap.put(Character.valueOf('6'), "6");
        RTF.greekmap.put(Character.valueOf('7'), "7");
        RTF.greekmap.put(Character.valueOf('8'), "8");
        RTF.greekmap.put(Character.valueOf('9'), "9");
        RTF.greekmap.put(Character.valueOf('('), "(");
        RTF.greekmap.put(Character.valueOf(')'), ")");
        RTF.greekmap.put(Character.valueOf('{'), "{");
        RTF.greekmap.put(Character.valueOf('}'), "}");
        RTF.greekmap.put(Character.valueOf('['), "[");
        RTF.greekmap.put(Character.valueOf(']'), "]");
        RTF.greekmap.put(Character.valueOf('.'), ".");
        RTF.greekmap.put(Character.valueOf(','), ",");
        RTF.greekmap.put(Character.valueOf(':'), ":");
        RTF.greekmap.put(Character.valueOf('-'), "-");
        RTF.greekmap.put(Character.valueOf('+'), "+");
        RTF.greekmap.put(Character.valueOf('/'), "/");
        RTF.greekmap.put(Character.valueOf('*'), "*");
        RTF.greekmap.put(Character.valueOf(';'), ";");
        RTF.greekmap.put(Character.valueOf('_'), "_");
    }

    public void readCompoundSynonym(String fileName) throws Exception {
/*
        SqlQuery.execute("DROP TABLE IF EXISTS tmp_names", null);
        SqlQuery.execute("CREATE TABLE tmp_names (rowid SERIAL, name_id INTEGER, legacy_molid INTEGER, "
          + "name VARCHAR, prio INTEGER, PRIMARY KEY(name, legacy_molid))", null);
*/
        int mode = 0;
        String line = "";
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header
        while(reader.ready()) {
            String st = reader.readLine(); 

            st = st.replaceAll("\u00b2", "<sup>2</sup>")
               .replaceAll("\u00b4", "'")
               .replaceAll("\\\\''e1", "a")
               .replaceAll("\\\\''e2", "b")
               .replaceAll("\\\\''c4", "D")
               .replaceAll("\\\\''b0", "&deg;")
               .replaceAll("\\\\''d7", "x")
               .replaceAll("\\\\f2\\\\''ec", "&micro;");

            line += st;
            if(st.matches("^[0-9]+;[0-9]+;'\\{\\\\rtf1.*")) {
                // System.out.println("RTF MODE");
                mode = 1;
            }
            if((mode == 1) && (st.matches("^';'Y'$") || st.matches("^';'N'$"))) {
                mode = 0;
                String a = line.replaceAll("^([0-9]+;[0-9]+;')(.*)(';'[NY]')$", "$1");
                String b = line.replaceAll("^([0-9]+;[0-9]+;')(.*)(';'[NY]')$", "$2");
                String c = line.replaceAll("^([0-9]+;[0-9]+;')(.*)(';'[NY]')$", "$3");
                b = b.replaceAll("''", "'");
                // System.out.printf("DEBUG: %s\n", b);
                b = readRTF(b).replaceAll("'","''");
                line = a + b + c; 

            }
            if(line.matches("[0-9]+;[0-9]+;.*;'[NY]'")) {
                int a = Integer.parseInt(line.replaceAll("^([0-9]+);([0-9]+);(.*);('[NY]')$", "$1"));
                int b = Integer.parseInt(line.replaceAll("^([0-9]+);([0-9]+);(.*);('[NY]')$", "$2"));
                String n = line.replaceAll("^([0-9]+);([0-9]+);(.*);('[NY]')$", "$3");
                boolean p = line.replaceAll("^([0-9]+);([0-9]+);(.*);('[NY]')$", "$4")
                         .matches("'Y'");

                if(n.equals("")) {
                        n = String.format("AUTO_molId%d", b);
                }
                update(a, b, n, p);
                line = "";
            }
        }
        reader.close();
    }

    private void update(int a, int b, String n, boolean p) throws Exception {
        List<Object> l = new ArrayList<> ();
        l.add(Integer.valueOf(a));
        l.add(Integer.valueOf(b));
        l.add(n.replaceAll("^'(.*)'$", "$1"));
        l.add(Integer.valueOf(p ? 1 : 10));
        try {
                // SqlQuery.execute("INSERT INTO tmp_names (name_id, legacy_molid, name, prio) VALUES (?,?,?,?)", l);
        } catch(Exception e) {
                // index violation, do nothing
        }
    }

    /*
     * read an RTF string
     */
    public String readRTF(String rfSrc) {
        RtfStringSource src = new RtfStringSource(rfSrc);
        RawRtfParser prs = new RawRtfParser();
        this.depth = 0;
        this.fontmap = new HashMap<String, LANG> ();
        this.lang = LANG.NORMAL;
        this.mode = MODE.TEXTOUT;
        this.outString = new StringBuilder();
        try {
            prs.parse(src, this);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        String out = this.outString.toString().replaceAll("\\s+"," ");
        return out;
    }

    @Override
    @SuppressWarnings( "fallthrough" )
    public void processCommand(Command command, int parameter, boolean hasParameter, boolean optional) {
        /*
        if(hasParameter) {
                System.out.printf("command %s %d\n", command.getCommandName(), parameter);
        } else {
                System.out.printf("command %s\n", command.getCommandName());
        }
        */
        switch(command.getCommandName()) {
            case "b" :
                if(this.mode == MODE.TEXTOUT) {
                /*
                 * bold is not needed. 
                 * some entries use all bold font, this is cleaned up 
                 *
                        if(hasParameter && (parameter == 0)) {
                                this.outString.append("</b>");
                        } else {
                                this.outString.append("<b>");
                        }
                */
                }
                break;
            case "colortbl" :
                this.mode = MODE.IGNORE;
                break;
            case "f" :
                this.currentFont = String.format("f%d", parameter);
                if(this.mode == MODE.TEXTOUT) {
                    this.lang = this.fontmap.get(this.currentFont);
                } else {
                    this.fontmap.put(this.currentFont, LANG.NORMAL);
                }
                break;
            case "fcharset":
                switch(parameter) {
                    case 0 :
                        this.fontmap.put(this.currentFont, LANG.NORMAL);
                        break;
                    case 2 :
                        this.fontmap.put(this.currentFont, LANG.GREEK);
                        break;
                    case 161 : 
                        this.fontmap.put(this.currentFont, LANG.GREEK);
                        break;
                    default :
                        System.out.printf("ERROR: illegal charset %d\n", parameter);
                }
                break;
            case "fonttbl" :
                this.mode = MODE.FONTDEF;
                break;
            case "i":
                if(this.mode == MODE.TEXTOUT) {
                    if(hasParameter && (parameter == 0)) {
                        this.outString.append("</i>");
                    } else {
                        this.outString.append("<i>");
                    }
                }
                break;
            case "lang" :
            /*
             * ignore lang parameter (not conclusive)
             *
                switch(parameter) {
                    case 1031 :
                    case 1033 :
                    case 1036 :
                    case 2057 :
                        this.lang = LANG.NORMAL;
                        break;
                    case 1040 :
                        this.lang = LANG.GREEK;
                        break;
                    default :
                        System.out.printf("ERROR: illegal lang %d\n", parameter);
                }
            */
                    break;
            case "nosupersub":
                this.outString.append(this.nosupersub);
                break;
            case "sub":
                this.outString.append("<sub>");
                this.nosupersub = "</sub>";
                break;
            case "super":
                this.outString.append("<sup>");
                this.nosupersub = "</sup>";
                break;
            case "u":
                this.outString.append(String.format("&#%d;", parameter));
                break;
        }
    }

    @Override
    public void processDocumentEnd() { 
        // System.out.println("DOCUMENT END");
    }

    @Override
    public void processDocumentStart() {
        // System.out.println("DOCUMENT START");
    }

    @Override
    public void processGroupStart() { 
        // System.out.printf("GROUP START %d\n", this.depth);
        this.depth++;
    }

    @Override
    public void processGroupEnd() { 
        this.depth--;
        // System.out.printf("GROUP END %d\n", this.depth);
        if(this.depth == 1) {
            this.mode = MODE.TEXTOUT;
        }
    }

    @Override
    public void processCharacterBytes(byte[] data) { 
        String st = new String(data, Charset.forName("UTF-8"));
        if(this.mode == MODE.TEXTOUT) {
            if(this.lang == LANG.NORMAL) {
                    this.outString.append(st);
            } else {
                for(int i=0; i<st.length(); i++) {
                    char c = st.charAt(i);
                    String x = RTF.greekmap.get(Character.valueOf(c));
                    if(x != null) {
                        this.outString.append(x);
                    } else {
                        System.out.printf("ERROR: unknown character mapping in String %s\n", st);
                    }
                }
            }
        }
    }

    @Override
    public void processBinaryBytes(byte[] data) { 
        this.outString.append("BINARY DATA"); 
    }

    @Override
    public void processString(String st) { 
        if(this.lang == LANG.NORMAL) {
            this.outString.append(st);
        } else {
            this.outString.append("<ERROR:GREEK>");
            this.outString.append(st);
            this.outString.append("</ERROR:GREEK>");
        }
    }
}
