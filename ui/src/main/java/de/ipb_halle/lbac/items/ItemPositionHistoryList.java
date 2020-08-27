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
package de.ipb_halle.lbac.items;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class ItemPositionHistoryList implements ItemDifference {

    private List<ItemPositionsHistory> positionAdds = new ArrayList<>();
    private List<ItemPositionsHistory> positionRemoves = new ArrayList<>();

    @Override
    public Date getMdate() {
        if (positionAdds.size() > 0) {
            return positionAdds.get(0).getmDate();
        } else {
            return positionRemoves.get(0).getmDate();
        }
    }

    public List<ItemPositionsHistory> getPositionAdds() {
        return positionAdds;
    }

    public void addHistory(ItemPositionsHistory history) {
        if (Objects.equals(history.getRowNew(), null)) {
            positionRemoves.add(history);

        } else {
            positionAdds.add(history);
        }
    }

    public void setPositionAdds(List<ItemPositionsHistory> positionAdds) {
        this.positionAdds = positionAdds;
    }

    public List<ItemPositionsHistory> getPositionRemoves() {
        return positionRemoves;
    }

    public void setPositionRemoves(List<ItemPositionsHistory> positionRemoves) {
        this.positionRemoves = positionRemoves;
    }

}
