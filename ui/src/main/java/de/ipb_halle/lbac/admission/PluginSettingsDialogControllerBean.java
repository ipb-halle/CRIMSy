/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.admission;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ipb_halle.lbac.material.JsfMessagePresenter;
import de.ipb_halle.lbac.material.MessagePresenter;

/**
 * This JSF backing bean controls the form of the plugin user settings.
 * <p>
 * View file: WEB-INF/templates/accountSettings/pluginsTab.xhtml
 *
 * @author flange
 */
@ViewScoped
@Named
public class PluginSettingsDialogControllerBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private UserBean userBean;

    private Logger logger;

    private MessagePresenter messagePresenter;

    private String molPluginType;

    private String previewStructure;

    /**
     * default constructor
     */
    public PluginSettingsDialogControllerBean() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * Initializes the bean state:
     * <ul>
     * <li>initialize the message presenter for i18n</li>
     * <li>set the plugin types to their preferred value</li>
     * <li>set the preview chemical structure to benzene</li>
     * </ul>
     */
    @PostConstruct
    public void init() {
        this.messagePresenter = JsfMessagePresenter.getInstance();

        this.molPluginType = userBean.getPluginSettings()
                .getPreferredMolPluginType();
        this.previewStructure = benzene;
    }

    public String getMolPluginType() {
        return molPluginType;
    }

    public void setMolPluginType(String molPluginType) {
        this.molPluginType = molPluginType;
    }

    /**
     * Returns the list of available chemical structure plugin types.
     * 
     * @return an unmodifiable list of chemical structure plugin types.
     */
    public List<String> getAvailableMolPluginTypes() {
        return userBean.getPluginSettings().getAllMolPluginTypes();
    }

    public String getPreviewStructure() {
        return previewStructure;
    }

    /**
     * Saves the selected plugin types as preferred plugin types and notifies to
     * user upon success.
     */
    public void actionSave() {
        User currentUser = userBean.getCurrentAccount();

        if (!currentUser.isPublicAccount()) {
            if (currentUser
                    .getSubSystemType() == AdmissionSubSystemType.LOCAL) {
                boolean result = userBean.getPluginSettings()
                        .setPreferredMolPluginType(molPluginType);

                if (result) {
                    messagePresenter.info("admission_plugins_updated");
                } else {
                    messagePresenter.error("admission_plugins_not_updated");
                }
            }
        }
    }

    /**
     * Loads a benzene molfile v2000 as preview.
     */
    public void actionLoadBenzene() {
        previewStructure = benzene;
    }

    /**
     * Loads a gramicidin S molfile v2000 as preview.
     */
    public void actionLoadGramicidinS() {
        previewStructure = gramicidinS;
    }

    /**
     * Loads a chlorophyll a molfile v2000 as preview.
     */
    public void actionLoadChlorophyllA() {
        previewStructure = chlorophyllA;
    }

    // Molfile from https://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:16716
    private String benzene = "\n" + "  Marvin  10310613082D          \n" + "\n"
            + "  6  6  0  0  0  0            999 V2000\n"
            + "    0.7145   -0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    0.0000   -0.8250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    0.7145    0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    0.0000    0.8250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   -0.7145   -0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   -0.7145    0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "  2  1  2  0  0  0  0\n" + "  3  1  1  0  0  0  0\n"
            + "  4  3  2  0  0  0  0\n" + "  5  2  1  0  0  0  0\n"
            + "  6  4  1  0  0  0  0\n" + "  5  6  2  0  0  0  0\n" + "M  END\n"
            + "";

    // Molfile from http://www.chemspider.com/Chemical-Structure.16736115.html
    private String chlorophyllA = "\n" + "\n" + "\n"
            + " 65 71  0  0001  0  0  0  0  0999 V2000\n"
            + "    6.5548   -6.1808    0.0000 N   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    4.2912   -3.9368    0.0000 N   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    6.2005   -7.4603    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    5.1770   -8.3067    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    7.3422   -8.1493    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    7.8934   -6.1218    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    4.3896   -2.6574    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    2.9723   -4.2715    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    5.1573   -4.9211    0.0000 Mg  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    5.6887   -9.5468    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    4.0156   -7.9524    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.3658   -7.3225    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    3.9172   -6.6336    0.0000 N   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    6.9879   -9.4681    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    6.6533   -2.4802    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    2.5589   -6.3383    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.1296   -4.0156    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    5.4525   -1.9094    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    2.0865   -5.2163    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    3.1101   -2.1653    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.7595   -5.2163    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    6.8501   -3.7991    0.0000 N   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    2.2834   -3.2282    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    7.8146   -1.9291    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.7792   -2.8148    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    2.8345   -8.4642    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    1.8897   -7.4997    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    4.9998  -10.6688    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    2.5196   -9.7831    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    2.7361   -0.9252    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    7.8146  -10.4720    0.0000 O   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    0.9645  -11.2397    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    5.6100  -11.8105    0.0000 O   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    2.1849  -11.8893    0.0000 O   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    1.2401   -9.9799    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    3.6416    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    9.6650   -7.6572    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    3.6809  -10.6098    0.0000 O   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    0.9842   -3.1888    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    0.0000  -11.8499    0.0000 O   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   10.0783   -2.6377    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.0115   -0.6299    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    0.5905   -7.6375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    3.1692  -11.8105    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   10.3342   -1.3779    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    2.3031  -13.4246    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    3.4644  -12.7357    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    4.6258  -13.4050    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    5.7872  -12.7357    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    6.9485  -13.4050    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.1099  -12.7160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    9.2516  -13.3853    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   10.4130  -12.7160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   11.5743  -13.3853    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   12.7357  -12.7160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    4.6258  -14.7435    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "    9.2713  -14.7238    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   13.8971  -13.3853    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   15.0584  -12.7160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   16.2001  -13.3853    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   17.3615  -12.7160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   18.5229  -13.3853    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   19.6842  -12.7160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   13.8971  -14.7238    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "   18.5229  -14.7238    0.0000 C   0  0  0  0  0  0  0  0  0  0  0\n"
            + "  1  3  1  0\n" + "  1  6  1  0\n" + "  1  9  1  0\n"
            + "  2  9  1  0\n" + "  2  7  1  0\n" + "  2  8  1  0\n"
            + "  3  4  1  0\n" + "  3  5  2  0\n" + "  4 10  1  0\n"
            + "  4 11  2  0\n" + "  5 12  1  0\n" + "  5 14  1  0\n"
            + "  6 21  1  0\n" + "  6 12  2  0\n" + "  7 18  2  0\n"
            + "  7 20  1  0\n" + "  8 23  1  0\n" + "  8 19  2  0\n"
            + " 10 28  1  6\n" + " 10 14  1  0\n" + " 11 26  1  0\n"
            + " 11 13  1  0\n" + " 12 37  1  0\n" + " 13 16  2  0\n"
            + " 14 31  2  0\n" + " 15 22  2  0\n" + " 15 24  1  0\n"
            + " 15 18  1  0\n" + " 16 27  1  0\n" + " 16 19  1  0\n"
            + " 17 22  1  0\n" + " 17 25  1  0\n" + " 17 21  2  0\n"
            + " 20 30  1  0\n" + " 20 23  2  0\n" + " 23 39  1  0\n"
            + " 24 25  2  0\n" + " 24 42  1  0\n" + " 25 41  1  0\n"
            + " 26 27  1  0\n" + " 26 29  1  1\n" + " 27 43  1  6\n"
            + " 28 33  2  0\n" + " 28 38  1  0\n" + " 29 35  1  0\n"
            + " 30 36  2  0\n" + " 32 35  1  0\n" + " 32 34  1  0\n"
            + " 32 40  2  0\n" + " 34 46  1  0\n" + " 38 44  1  0\n"
            + " 41 45  1  0\n" + " 46 47  1  0\n" + " 47 48  2  0\n"
            + " 48 49  1  0\n" + " 48 56  1  0\n" + " 49 50  1  0\n"
            + " 50 51  1  0\n" + " 51 52  1  0\n" + " 52 53  1  0\n"
            + " 52 57  1  1\n" + " 53 54  1  0\n" + " 54 55  1  0\n"
            + " 55 58  1  0\n" + " 58 59  1  0\n" + " 58 64  1  1\n"
            + " 59 60  1  0\n" + " 60 61  1  0\n" + " 61 62  1  0\n"
            + " 62 63  1  0\n" + " 62 65  1  0\n" + "M  END\n" + "";

    // Molfile from https://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:5530
    private String gramicidinS = " \n" + "  Marvin  04170713232D          \n"
            + " \n" + " 84 88  0  0  1  0            999 V2000\n"
            + "   18.2069  -10.7246    0.0000 C   0  0  2  0  0  0  0  0  0  0  0  0\n"
            + "   18.7878  -11.3048    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   19.5191  -10.9317    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   19.3900  -10.1210    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   18.5792   -9.9930    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    9.6405   -8.2832    0.0000 C   0  0  2  0  0  0  0  0  0  0  0  0\n"
            + "    9.0557   -7.6990    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.3192   -8.0748    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.4491   -8.8913    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    9.2657   -9.0201    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    9.6468   -9.9136    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   10.3596   -9.4984    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   11.0676   -8.2789    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   11.7801   -7.8623    0.0000 C   0  0  1  0  0  0  0  0  0  0  0  0\n"
            + "   11.7759   -6.8310    0.0000 C   0  0  2  0  0  0  0  0  0  0  0  0\n"
            + "   11.0593   -6.4185    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   12.4884   -6.4143    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   12.4926   -8.2748    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   12.5009   -8.9581    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   13.2050   -7.8581    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   13.9175   -8.2706    0.0000 C   0  0  1  0  0  0  0  0  0  0  0  0\n"
            + "   13.9133   -7.4456    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   13.1967   -7.0331    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   14.6300   -7.8540    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   14.6258   -7.0290    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   15.3425   -8.2664    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   16.0549   -7.8498    0.0000 C   0  0  2  0  0  0  0  0  0  0  0  0\n"
            + "   16.7674   -8.2623    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   16.7740   -9.0873    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   17.4799   -7.8456    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   10.3508   -7.8705    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   10.3462   -7.0456    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   13.1957   -6.2081    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   13.9096   -5.7948    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   18.1966   -8.2541    0.0000 C   0  0  2  0  0  0  0  0  0  0  0  0\n"
            + "   18.9087   -7.8376    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   16.0531   -7.0248    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   16.7666   -6.4045    0.0000 C   0  0  3  0  0  0  0  0  0  0  0  0\n"
            + "   16.7648   -5.5796    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   17.4820   -6.8154    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   18.9042   -7.0126    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   19.5904   -6.5996    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   19.6133   -5.7747    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   18.8965   -5.3662    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   18.1844   -5.7827    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   18.1890   -6.6076    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   16.7789  -10.7267    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   16.0657  -11.1421    0.0000 C   0  0  1  0  0  0  0  0  0  0  0  0\n"
            + "   16.0684  -12.1734    0.0000 C   0  0  2  0  0  0  0  0  0  0  0  0\n"
            + "   16.7842  -12.5872    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   15.3552  -12.5888    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   15.3540  -10.7283    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   15.3745  -10.0449    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   14.6408  -11.1437    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   13.9290  -10.7299    0.0000 C   0  0  1  0  0  0  0  0  0  0  0  0\n"
            + "   13.9317  -11.5549    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   14.6476  -11.9686    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   13.2158  -11.1452    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   13.2185  -11.9702    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   12.5041  -10.7314    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   11.7909  -11.1468    0.0000 C   0  0  2  0  0  0  0  0  0  0  0  0\n"
            + "   11.0791  -10.7330    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   11.0740   -9.9080    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   10.3659  -11.1484    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   17.4950  -11.1365    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   17.4981  -11.9614    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   14.6471  -12.7936    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   13.9324  -13.2057    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    9.6498  -10.7386    0.0000 C   0  0  2  0  0  0  0  0  0  0  0  0\n"
            + "    8.9095  -11.1538    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   11.7912  -12.2606    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   11.0769  -12.6734    0.0000 C   0  0  3  0  0  0  0  0  0  0  0  0\n"
            + "   11.0772  -13.4984    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   10.3622  -12.2611    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.9127  -11.9788    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.1981  -12.3905    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.2012  -13.2154    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.9172  -13.6252    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    9.6300  -13.2100    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    9.6544  -12.3850    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   18.2013   -9.0791    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   17.4891   -9.4955    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   10.3509   -8.6957    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   17.4882  -10.3081    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + " 28 30  1  0  0  0  0\n" + " 13 14  1  0  0  0  0\n"
            + " 41 42  2  0  0  0  0\n" + " 42 43  1  0  0  0  0\n"
            + " 43 44  2  0  0  0  0\n" + " 44 45  1  0  0  0  0\n"
            + " 45 46  2  0  0  0  0\n" + " 46 41  1  0  0  0  0\n"
            + " 14 15  1  1  0  0  0\n" + " 15 16  1  0  0  0  0\n"
            + " 15 17  1  0  0  0  0\n" + " 14 18  1  0  0  0  0\n"
            + " 18 19  2  0  0  0  0\n" + " 18 20  1  0  0  0  0\n"
            + " 20 21  1  0  0  0  0\n" + " 21 22  1  6  0  0  0\n"
            + " 22 23  1  0  0  0  0\n" + " 21 24  1  0  0  0  0\n"
            + " 24 25  2  0  0  0  0\n" + " 24 26  1  0  0  0  0\n"
            + " 26 27  1  0  0  0  0\n" + "  8  9  1  0  0  0  0\n"
            + " 13 31  1  0  0  0  0\n" + "  9 10  1  0  0  0  0\n"
            + " 31 32  2  0  0  0  0\n" + " 10  6  1  0  0  0  0\n"
            + " 61 62  1  0  0  0  0\n" + " 62 63  2  0  0  0  0\n"
            + " 62 64  1  0  0  0  0\n" + " 47 48  1  0  0  0  0\n"
            + " 48 49  1  1  0  0  0\n" + " 49 50  1  0  0  0  0\n"
            + " 49 51  1  0  0  0  0\n" + " 48 52  1  0  0  0  0\n"
            + " 52 53  2  0  0  0  0\n" + " 52 54  1  0  0  0  0\n"
            + " 54 55  1  0  0  0  0\n" + " 55 56  1  6  0  0  0\n"
            + " 56 57  1  0  0  0  0\n" + " 55 58  1  0  0  0  0\n"
            + " 58 59  2  0  0  0  0\n" + " 58 60  1  0  0  0  0\n"
            + " 60 61  1  0  0  0  0\n" + " 23 33  1  0  0  0  0\n"
            + " 47 65  1  0  0  0  0\n" + "  5  1  1  0  0  0  0\n"
            + " 65 66  2  0  0  0  0\n" + " 33 34  1  0  0  0  0\n"
            + " 57 67  1  0  0  0  0\n" + " 67 68  1  0  0  0  0\n"
            + " 30 35  1  0  0  0  0\n" + " 64 69  1  0  0  0  0\n"
            + " 11 12  2  0  0  0  0\n" + " 69 70  1  6  0  0  0\n"
            + " 35 36  1  6  0  0  0\n" + " 61 71  1  1  0  0  0\n"
            + "  1  2  1  0  0  0  0\n" + " 71 72  1  0  0  0  0\n"
            + " 27 37  1  1  0  0  0\n" + " 72 73  1  0  0  0  0\n"
            + "  2  3  1  0  0  0  0\n" + " 72 74  1  0  0  0  0\n"
            + " 37 38  1  0  0  0  0\n" + " 70 75  1  0  0  0  0\n"
            + "  3  4  1  0  0  0  0\n" + " 38 39  1  0  0  0  0\n"
            + "  4  5  1  0  0  0  0\n" + " 38 40  1  0  0  0  0\n"
            + "  6  7  1  0  0  0  0\n" + " 75 76  2  0  0  0  0\n"
            + " 76 77  1  0  0  0  0\n" + " 77 78  2  0  0  0  0\n"
            + " 78 79  1  0  0  0  0\n" + " 79 80  2  0  0  0  0\n"
            + " 80 75  1  0  0  0  0\n" + " 36 41  1  0  0  0  0\n"
            + "  7  8  1  0  0  0  0\n" + " 81 82  2  0  0  0  0\n"
            + " 11 69  1  0  0  0  0\n" + " 35 81  1  0  0  0  0\n"
            + " 31  6  1  0  0  0  0\n" + " 11 10  1  0  0  0  0\n"
            + " 65  1  1  0  0  0  0\n" + " 81  5  1  0  0  0  0\n"
            + " 27 28  1  0  0  0  0\n" + "  6 83  1  6  0  0  0\n"
            + " 28 29  2  0  0  0  0\n" + "  1 84  1  6  0  0  0\n" + "M  END\n"
            + "";
}
