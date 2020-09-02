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
package de.ipb_halle.lbac.exp.assay;

import com.corejsf.util.Messages;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordType;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.util.Unit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 * Assays provide information about effects which can be induced in a 
 * certain target by e.g. compounds. The target and the whole test 
 * system is specified by a standard operation procedure (which is 
 * stored in the experiment template). Assay stores a the target,
 * additional conditions and remarks, acceptable units etc. and a 
 * collection of tupels <code>material, outcome</code>).
 * 
 * The outcome will be diverse (boolean, single numbers, numbers with error, 
 * (multi-dimensional) arrays). In a first stage, only single point
 * results (numbers) will be implemented.
 *
 * @author fbroda
 */
public class Assay extends ExpRecord implements DTO {

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * remarks and conditions
     */
    private String remarks;

    /*
     * the target material (an enzyme, organ, organism etc.)
     */
    private Material target;

    /**
     * comma separated list of acceptable units
     */
    private String units;

    /**
     * outcometype limits the type of outcome this assay object accepts.
     * This is done becaus rendering multiple outcome types in the same 
     * table might prove difficult.
     */
    private AssayOutcomeType    outcomeType;

    /**
     * the tupels (material, outcome)
     */
    private List<AssayRecord>   records;

    /**
     * default constructor
     */
    public Assay() {
        super();
        setType(ExpRecordType.ASSAY);
        this.remarks = "";
        this.units = "mM, µM, nM";
        this.records = new ArrayList<AssayRecord> ();
        this.outcomeType = AssayOutcomeType.SINGLE_POINT;
    }

    public Assay(AssayEntity entity, Material target) {
        super();
        setType(ExpRecordType.ASSAY);
        this.remarks = entity.getRemarks();
        this.target = target;
        this.units = entity.getUnits();
        this.records = new ArrayList<AssayRecord> ();
        if (entity != null) {
            this.outcomeType = entity.getOutcomeType();
        } 
    }

    @Override
    public void copy() {
        for (AssayRecord rec : this.records) {
            rec.setAssay(this);
            rec.setRecordId(null);
        }
    }

    public AssayEntity createEntity() {
        return new AssayEntity()
            .setExpRecordId(getExpRecordId())
            .setOutcomeType(this.outcomeType)
            .setRemarks(this.remarks)
            .setTargetId(this.target.getId())
            .setUnits(this.units);
    }

    public BarChartModel computeSinglePointBarChart() {
        double min = 0.0;
        double max = 0.0;
        double logMin = 1000.0;
        double logMax = -1000.0;
        List<Double> values = new ArrayList<Double> ();
        List<Double> logValues = new ArrayList<Double> ();

        for (AssayRecord r : this.records) {
            double v = ((SinglePointOutcome) r.getOutcome()).getValue();
            Unit u = Unit.getUnit(((SinglePointOutcome) r.getOutcome()).getUnit());
            v *= u.getFactor();

            min = Double.min(min, v);
            max = Double.max(max, v);
            values.add(Double.valueOf(v));

            v = (v > 0.0) ? Math.log10(v) : 0.0;
            logMin = Double.min(logMin, v);
            logMax = Double.max(logMax, v);
            logValues.add(Double.valueOf(v));

        }

        String axisLabel = "Linear";
        double faktor = 1.0;
        if ((logMax - logMin) > 2.2) {
            values = logValues;
            min = logMin;
            max = logMax;
            axisLabel = "Log10";
            if ((min < -1) && (max < 1)) {
                faktor = -1.0;
                double x = min * faktor;
                min = max * faktor;
                max = x;
                axisLabel = "Neg. Log10";
            }
        }

        int i = 1;
        BarChartModel model = new BarChartModel();
        ChartSeries data = new ChartSeries();
        data.setLabel("Activity");
        for (Double d : values) {
            data.set(Integer.toString(i++), Double.valueOf(faktor * d.doubleValue()));
        }
 
        model.setTitle("Assay");
        model.setLegendPosition("ne");
 
        Axis xAxis = model.getAxis(AxisType.X);
        xAxis.setLabel("Sample");
 
        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setLabel(axisLabel);
        yAxis.setMin(min * 1.1);
        yAxis.setMax(max * 1.1);

        model.addSeries(data);
        return model;
    }

    @Override
    public BarChartModel getBarChart() {
        switch (this.outcomeType) {
            case SINGLE_POINT : return computeSinglePointBarChart();
        }
        return null;
    }

    public AssayOutcomeType getOutcomeType() {
        return this.outcomeType;
    }

    /**
     * @return a localized list of outcometypes for selection in
     * template mode
     */
    public List<SelectItem> getOutcomeTypes() {
        List<SelectItem> l = new ArrayList<SelectItem> ();
        for(AssayOutcomeType t : AssayOutcomeType.values()) {
            l.add(new SelectItem(t, 
                    Messages.getString(MESSAGE_BUNDLE, "AssayOutcomeType_" + t.toString(), null)));
        }
        return l;
    }

    public List<AssayRecord> getRecords() {
        return this.records;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public Material getTarget() {
        return this.target;
    }

    public String getUnits() {
        return this.units;
    }

    public void setOutcomeType(AssayOutcomeType outcomeType) {
        this.outcomeType = outcomeType;
    }

    public Assay setRecords(List<AssayRecord> records) {
        this.records = records;
        return this;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setTarget(Material target) {
        this.target = target;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
