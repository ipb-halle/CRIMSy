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


public class Adjustment {

    private int start;
    private int end;
    private int delta;
    private int accumulatedDelta;


    public Adjustment(int start, int end, int delta, int accumulatedDelta) {
        this.start = start;
        this.end = end;
        this.delta = delta;
        this.accumulatedDelta = accumulatedDelta;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getDelta() {
        return delta;
    }

    public int getAccumulatedDelta() {
        return accumulatedDelta;
    }
}
