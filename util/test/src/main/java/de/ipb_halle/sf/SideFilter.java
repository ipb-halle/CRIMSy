/*
 * SideFilter for SeleniumIDE
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
package de.ipb_halle.sf;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Iterator;
import java.util.UUID;

/**
 * Filter Selenium IDE files and introduce pauses
 */
public class SideFilter {

    public JsonArray handleCommands(JsonArray array) {
        JsonArray newArray = new JsonArray();
        Iterator<JsonElement> iter = array.iterator();
        while (iter.hasNext()) {
            JsonObject obj = iter.next().getAsJsonObject();
            obj.add("targets", new JsonArray());
            newArray.add(obj);
            newArray.add(pause());
        }
        return newArray;
    }

    public JsonArray handleTests(JsonArray array) {
        JsonArray newArray = new JsonArray();

        Iterator<JsonElement> iter = array.iterator();
        while (iter.hasNext()) {
            JsonObject obj = iter.next().getAsJsonObject();
            JsonArray commands = handleCommands(
                    obj.remove("commands").getAsJsonArray()); 
            obj.add("commands", commands);
            newArray.add(obj);
        }

        return newArray;
    }

    public String parse(String fileName) throws Exception {
        JsonElement jsonTree = JsonParser.parseReader(
            new FileReader(fileName));
        if (jsonTree.isJsonObject()) {
            JsonObject treeObj = jsonTree.getAsJsonObject();
            JsonElement tests = treeObj.remove("tests");
            if ((tests == null) || (! tests.isJsonArray())) {
                throw new Exception("Format error");
            }
              tests = handleTests(tests.getAsJsonArray());
              treeObj.add("tests", tests);

            return treeObj.toString();
        }
        throw new Exception("Input format error");
    }

    public JsonElement pause() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", UUID.randomUUID().toString());
        obj.addProperty("comment", "");
        obj.addProperty("command", "pause");
        obj.addProperty("target", "");
        obj.addProperty("value", "200");
        
        return obj;
    }

    public static void main(String[] argv) {
        if (argv.length != 2) {
            System.err.println("Side Filter usage: SideFilter INPUT OUTPUT");
            return;
        }
        SideFilter sf = new SideFilter();
        try {
            FileOutputStream out = new FileOutputStream(argv[1]);
            out.write(sf.parse(argv[0]).getBytes());
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

