/*
 * KICKS
 * Custom Tag for molecule plugins. This Java code must 
 * be supplemented by the KICKS JavaScript library.
 *
 * (c) 2015 Frank Broda & Leibniz-Inst. f. Pflanzenbiochemie
 *
 * Created: 2015-05-22 fbroda
 * Version: $Id: UIMolPlugin.java 505 2016-08-16 15:54:46Z fbroda $ 
 * 
 */
package de.ipb_halle.lbac.plugin; 

import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;

// import javax.el.ValueExpression;
// import javax.el.ELContext;
// import javax.faces.component.EditableValueHolder;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
// import javax.faces.render.FacesRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



@FacesComponent("UIMolPlugin")
public class UIMolPlugin extends UIInput {	

	private final static String[]		pluginTypes = { null, "JChemPaint", "Marvin", "MarvinJS", "MolPaintJS" };

	protected final static String	defaultPluginPath = "/plugins";
	protected final static String	defaultPluginType = "MolPaintJS";
	private int			height;
	private String			pluginPath;
	private Map<String,String>	pluginParam;
	private String			pluginType;
	private int			width;

	private Logger logger;

	/**
	 * Default constructor
	 */
	public UIMolPlugin() {
		super();
		logger = LogManager.getLogger(this.getClass().getName());

		this.height = 400;
		this.pluginPath = UIMolPlugin.defaultPluginPath;
		this.pluginType = UIMolPlugin.defaultPluginType;
		initPluginParam();
		this.width = 400;
		setRendererType(null);
	}

        /**
         * decode browser response
         */
	@Override
        public void decode(FacesContext context) {
                Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
                String clientId = this.getClientId(context);

		String pt = (String) getAttributes().get("pluginType");
		setPluginType(clientId, pt);

		String value = requestMap.get(clientId);
		// this.logger.info("decode(): " + value);
		setSubmittedValue(value);
		setValid(true);
	}

	/**
	 * encode editor / viewer elements in HTML
	 */
	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		//if (!this.isRendered()) return;

		ResponseWriter writer = context.getResponseWriter();
		String clientId = this.getClientId(context);

		String pt = (String) getAttributes().get("pluginType");
		setPluginType(clientId, pt);

		Object prolog = getAttributes().get("prolog");
		Object edit = getAttributes().get("edit");

		if(Boolean.valueOf((prolog == null) ? "false" : prolog.toString()).booleanValue()) {
			// this.logger.info("encodeBegin(): Prolog");
			encodeProlog(writer, clientId);
		} else {
			if(Boolean.valueOf((edit == null) ? "false" : edit.toString()).booleanValue()) {
				// this.logger.info("encodeBegin(): Editor");
				encodeEditor(writer, clientId);
			} else {
				// this.logger.info("encodeBegin(): Viewer");
				encodeViewer(writer, clientId);
			}
		}
		writer.flush();
	}

	@Override
	public void encodeChildren(FacesContext context) throws IOException {
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
	}


	/**
	 * encode an editor applet of type <code>pluginType</code>
	 */
	private void encodeEditor(ResponseWriter writer, String clientId) throws IOException {
		switch(this.pluginType) {
			case "JChemPaint":
				encodeJChemPaintEditor(writer, clientId);
				break;
			case "Marvin" : 
				encodeMarvinSketch(writer, clientId);
				break;
			case "MarvinJS": 
				encodeMarvinJSEditor(writer, clientId);
				break;
			case "MolPaintJS":
				encodeMolPaintJSEditor(writer, clientId);
				break;
			default :
				this.logger.warn("encodeEditor(): unknown plugin type: " + this.pluginType);
		}
	}


	/**
	 * encode the prolog section of a plugin. 
	 */
	private void encodeProlog(ResponseWriter writer, String clientId) throws IOException {
		switch(this.pluginType) {
			case "JChemPaint":
				encodeJChemPaintProlog(writer, clientId);
				break;
			case "Marvin" : 
				encodeMarvinProlog(writer, clientId);
				break;
			case "MarvinJS": 
				encodeMarvinJSProlog(writer, clientId);
				break;
			case "MolPaintJS":
				encodeMolPaintJSProlog(writer, clientId);
				break;
			default :
				this.logger.warn("encodeProlog(): unknown plugin type: " + this.pluginType);
		}
	}

	/**
	 * encode a viewer applet of type <code>pluginType</code>
	 */
	private void encodeViewer(ResponseWriter writer, String clientId) throws IOException { 
		switch(this.pluginType) {
			case "JChemPaint":
				encodeJChemPaintViewer(writer, clientId);
				break;
			case "Marvin" : 
				encodeMarvinView(writer, clientId);
				break;
			case "MarvinJS": 
				encodeMarvinJSViewer(writer, clientId);
				break;
			case "MolPaintJS":
				encodeMolPaintJSViewer(writer, clientId);
				break;
			default :
				this.logger.warn("encodeViewer(): unknown plugin type: " + this.pluginType);
		}
	}

	/******************************************************
	 *
	 * MolPaintJS 
	 *
	 ******************************************************/

	private String getMolPaintJSProperties(boolean viewer) {
		StringBuilder sb = new StringBuilder();
		sb.append("{iconSize:32");
		// installPath is a global property
		if(viewer) {
			sb.append(", viewer:1");
			sb.append(String.format(", sizeX: %d", this.width));
			sb.append(String.format(", sizeY: %d", this.height));
		} else {
			sb.append(String.format(", sizeX: %d", this.width - 72));
                	sb.append(String.format(", sizeY: %d", this.height - 36));
		}
		sb.append("}");
		return sb.toString();
	}

	private void encodeMolPaintJSEditor(ResponseWriter writer, String clientId) throws IOException {
                String id = clientId + "_MolPaintJS";

                writer.startElement("div", this);
                writer.writeAttribute("id", id, null); 
                writer.endElement("div");

                writer.startElement("input", this);
                writer.writeAttribute("type", "hidden", null);
                writer.writeAttribute("id", clientId, null);
                writer.writeAttribute("class","hiddenMolEditorInputs",null);
                writer.writeAttribute("name", clientId, null);
                writer.writeAttribute("value", getValue(), "value");
                writer.endElement("input");

                writer.startElement("script", this);
                writer.writeAttribute("type", "text/javascript", null);
                
                writer.writeText(
                        String.format(
                                "molpaintjs.newContext('%s', %s).setMolecule('%s').init();\n",
                                id,
                                getMolPaintJSProperties(false),
                                escape((String) getValue())), null);
           //     writer.writeText(String.format("  molEditorRegistry.registerEditor('%s', 'MolPaintJS');\n", clientId), null);
                writer.endElement("script");
	}

        private void encodeMolPaintJSProlog(ResponseWriter writer, String clientId) throws IOException {

                writer.startElement("link", this);
                writer.writeAttribute("type", "text/css", null);
                writer.writeAttribute("rel", "stylesheet", null);
                writer.writeAttribute("href", String.format("%s/MolPaintJS/css/styles.css", this.pluginPath), null);
                writer.endElement("link");

        	writer.startElement("script", this);
		writer.writeAttribute("type", "text/javascript", null);
                writer.writeAttribute("src", String.format("%s/MolPaintJS/js/molpaint.js", this.pluginPath), null);
                writer.endElement("script");

		writer.startElement("script", this);
                writer.writeAttribute("type", "text/javascript", null);

		// installPath currently needs a trailing slash!
		writer.writeText(String.format("var molpaintjs = new MolPaintJS({installPath: '%s/MolPaintJS/'});\n", 
		  this.pluginPath), null);
		writer.endElement("script");

        }

	private void encodeMolPaintJSViewer(ResponseWriter writer, String clientId) throws IOException {
		String id = clientId + "_MolPaintJS";

		writer.startElement("div", this);
		writer.writeAttribute("id", id, null);
		writer.endElement("div");

		writer.startElement("script", this);
                writer.writeAttribute("type", "text/javascript", null);
		writer.writeText(String.format("molpaintjs.newContext('%s', %s).setMolecule('%s').init();\n",
		  id, getMolPaintJSProperties(true), escape((String) getValue())), null); 
		writer.endElement("script");
	}

        private void initMolPaintJSDefaults() {
                this.pluginParam = new TreeMap<String, String> ();
        }


	/******************************************************
	 *
	 * JChemPaint applet (Java) 
	 *
	 ******************************************************/

	private void jcpParam(ResponseWriter writer, String key) throws IOException {
		String param = this.pluginParam.get(key);
		if(param != null) {
			writer.startElement("param", this);
			writer.writeAttribute("name", key, null);
			writer.writeAttribute("value", param, null);
			writer.endElement("param");
		}
	}

	private void encodeJChemPaintEditor(ResponseWriter writer, String clientId) throws IOException {
	      writer.startElement("applet", this);
		writer.writeAttribute("code", "org.openscience.jchempaint.applet.JChemPaintEditorApplet", null);
		writer.writeAttribute("id", clientId + "_JChemPaint", null);
		writer.writeAttribute("name", clientId + "_JChemPaint", null);
		writer.writeAttribute("archive", 
		  String.format("%s/jchempaint/jchempaint-applet-editor.jar, %s/jchempaint/jchempaint-applet-core.jar",
		  this.pluginPath, this.pluginPath), null);
		writer.writeAttribute("height", Integer.valueOf(this.height), null);
		writer.writeAttribute("width", Integer.valueOf(this.width), null);
		
		jcpParam(writer, "boxborder");
		jcpParam(writer, "centerimage");
		jcpParam(writer, "codebase");
		jcpParam(writer, "image");
		jcpParam(writer, "impliciths");

		writer.endElement("applet");

		writer.startElement("input", this);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", clientId, null);
		writer.writeAttribute("name", clientId, null);
		writer.writeAttribute("value", getValue(), "value");
		writer.endElement("input");

		writer.startElement("script", this);
		writer.writeAttribute("language", "JavaScript", null);
		writer.writeText(String.format("  molEditorRegistry.registerEditor('%s', 'JChemPaint');\n", clientId), null);
		writer.writeText(String.format("  molEditorRegistry.setMol('%s', 'JChemPaint');\n", clientId), null);
		writer.endElement("script");
	}

	/*
	 */
	private void encodeJChemPaintProlog(ResponseWriter writer, String clientId) throws IOException {
		// no prolog needed
	}

	private void encodeJChemPaintViewer(ResponseWriter writer, String clientId) throws IOException {
	      writer.startElement("applet", this);
		writer.writeAttribute("code", "org.openscience.jchempaint.applet.JChemPaintViewerApplet", null);
		writer.writeAttribute("id", clientId + "_JChemPaint", null);
		writer.writeAttribute("name", clientId + "_JChemPaint", null);
		writer.writeAttribute("archive", 
		  String.format("%s/jchempaint/jchempaint-applet-editor.jar, %s/jchempaint/jchempaint-applet-core.jar",
		  this.pluginPath, this.pluginPath), null);
		writer.writeAttribute("height", Integer.valueOf(this.height), null);
		writer.writeAttribute("width", Integer.valueOf(this.width), null);
		
		jcpParam(writer, "boxborder");
		jcpParam(writer, "centerimage");
		jcpParam(writer, "codebase");
		jcpParam(writer, "image");
		jcpParam(writer, "impliciths");

		writer.endElement("applet");

		writer.startElement("input", this);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", clientId, null);
		writer.writeAttribute("name", clientId, null);
		writer.writeAttribute("value", getValue(), "value");
		writer.endElement("input");

		writer.startElement("script", this);
		writer.writeAttribute("language", "JavaScript", null);
		writer.writeText("  molEditorRegistry.initJChemPaint();\n", null);
		writer.writeText(String.format("  molEditorRegistry.setMol('%s','JChemPaint');\n", clientId), null);
		writer.endElement("script");
	}

	private void initJChemPaintDefaults() {
		this.pluginParam = new TreeMap<String, String> ();
		this.pluginParam.put("boxborder", "false");
		this.pluginParam.put("centerimage", "true");
		this.pluginParam.put("codebase", ".");
		// this.pluginParam.put("codebase", "/plugins/jchempaint/EditorApplet_files/");
		// this.pluginParam.put("codebase_lookup", "false");
		this.pluginParam.put("image", "/pix/hourglass.gif");
		this.pluginParam.put("impliciths", "true");
		// this.pluginParam.put("load", "aceton.mol");
		// this.pluginParam.put("onLoadTarget", "statusFrame");
		// this.pluginParam.put("smiles", "OC(C)C");
	}

	/******************************************************
	 *
	 * TRADITIONAL MARVIN (Java) applet
	 *
	 ******************************************************/

	/**
	 * conditionally append parameters to MarvinSketch JavaScript code
	 * @param writer ResponseWriter 
	 * @param mpp parameter object for plugin instance
	 * @param key parameter key
	 */
	private void msketchParam(ResponseWriter writer, String key) throws IOException {
		String param = this.pluginParam.get(key);
		if(param != null) {
			writer.writeText(String.format("  msketch_param('%s', '%s');\n", 
			  key, param), null);
		}
	}

	 /**
	 * conditionally append parameters to MarvinView JavaScript code
	 * @param writer ResponseWriter
	 * @param key parameter key
	 */
	private void mviewParam(ResponseWriter writer, String key) throws IOException {
		String param = this.pluginParam.get(key);
		if(param != null) {
			  writer.writeText(String.format("  mview_param('%s', '%s');\n", 
			  key, param), null);
		}
	}

	/**
	 * @param writer ResponseWriter
	 */
	private void encodeMarvinSketch(ResponseWriter writer, String clientId) throws IOException {
		writer.startElement("script", this);
		writer.writeAttribute("language", "JavaScript", null);

		writer.writeText(String.format("  molEditorRegistry.registerEditor('%s', 'Marvin');\n", clientId), null);
		writer.writeText(String.format("  msketch_name='%s_Marvin';\n", clientId), null);

		writer.writeText(String.format("  msketch_begin('%s/marvin/', %d, %d);\n", 
		  this.pluginPath, this.width, this.height), null);

		writer.writeText(String.format("  msketch_param('mol', '%s');\n", escape((String) getValue())), null);

		msketchParam(writer, "colorScheme");
		msketchParam(writer, "rendering");
		msketchParam(writer, "molbg");
		msketchParam(writer, "menubar");
		msketchParam(writer, "menuconfig");

		writer.writeText("  msketch_end();\n", null);
		writer.endElement("script");

		writer.startElement("input", this);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", clientId, null);
		writer.writeAttribute("name", clientId, null);
		writer.writeAttribute("value", getValue(), "value");
		writer.endElement("input");
	}

	private void encodeMarvinProlog(ResponseWriter writer, String clientId) throws IOException {
		writer.startElement("script", this);
		writer.writeAttribute("language", "JavaScript", null);
		writer.writeAttribute("src", String.format("%s/marvin/marvin.js", this.pluginPath), null);
		// writer.writeText("",  null);
		writer.endElement("script");

	}

	/**
	 */
        private void encodeMarvinView(ResponseWriter writer, String clientId) throws IOException {
		writer.startElement("script", this);
		writer.writeAttribute("language", "JavaScript", null);

		writer.writeText(String.format("  mview_begin('%s/marvin/', %d, %d);\n", 
		  this.pluginPath, this.width, this.height), null);

		writer.writeText(String.format("  mview_param('mol', '%s');\n", escape((String) getValue())), null);

		mviewParam(writer, "colorScheme");
		mviewParam(writer, "rendering");
		mviewParam(writer, "molbg");
		mviewParam(writer, "menubar");
		mviewParam(writer, "menuconfig");

		writer.writeText("  mview_end();\n", null);
		writer.endElement("script");
        }

	private void initMarvinDefaults() {
		this.pluginParam = new TreeMap<String, String> ();
		this.pluginParam.put("molbg", "#f5f5f5");
		this.pluginParam.put("colorScheme", "mono");
		this.pluginParam.put("menubar", "false");
	//	this.pluginParam.put("menuconfig", "config.xml");
		this.pluginParam.put("rendering", "wireframe");
	}


	/******************************************************
	 *
	 * MarvinJS plugin (JavaScript)
	 *
	 ******************************************************/

	private void encodeMarvinJSEditor(ResponseWriter writer, String clientId) throws IOException {

		writer.startElement("iframe", this);
		writer.writeAttribute("src", String.format("%s/marvinJS/editor.html", this.pluginPath), null);
		writer.writeAttribute("id", String.format("%s_MarvinJS", clientId), null);
		writer.writeAttribute("class", "sketcher-frame", null);
		writer.writeAttribute("height", Integer.valueOf(this.height), null);
		writer.writeAttribute("width", Integer.valueOf(this.width), null);
		writer.endElement("iframe");

                writer.startElement("input", this);
                writer.writeAttribute("type", "hidden", null);
                writer.writeAttribute("id", clientId, null);
                writer.writeAttribute("name", clientId, null);
                writer.writeAttribute("value", getValue(), "value");
                writer.endElement("input");

		writer.startElement("script", this);
		writer.writeAttribute("language", "JavaScript", null);
		writer.writeText(String.format("  molEditorRegistry.registerEditor('%s', 'MarvinJS');\n", clientId), null);
		if(getValue().toString().length() > 80) {
			writer.writeText(String.format("  $(document).ready(function() {\n"
			  + "    molEditorRegistry.setMol('%s', 'MarvinJS');\n  });\n", clientId), null);
		}
		writer.endElement("script");
	}

	private void encodeMarvinJSProlog(ResponseWriter writer, String clientId) throws IOException {
	/*
		writer.startElement("link", this);
		writer.writeAttribute("type", "text/css", null);
		writer.writeAttribute("rel", "stylesheet", null);
		writer.writeAttribute("href", String.format("%s/marvinJS/css/doc.css", this.pluginPath), null);
		writer.endElement("link");
		writer.startElement("link", this);
		writer.writeAttribute("type", "text/css", null);
		writer.writeAttribute("rel", "stylesheet", null);
		writer.writeAttribute("href", String.format("%s/marvinJS/js/lib/rainbow/github.css", this.pluginPath), null);
		writer.endElement("link");
	*/
		writer.startElement("script", this);
		writer.writeAttribute("src", String.format("%s/marvinJS/js/lib/jquery-1.9.1.min.js", this.pluginPath), null);
		writer.endElement("script");
		writer.startElement("script", this);
		writer.writeAttribute("src", String.format("%s/marvinJS/js/lib/rainbow/rainbow-custom.min.js", this.pluginPath), null);
		writer.endElement("script");
		writer.startElement("script", this);
		writer.writeAttribute("src", String.format("%s/marvinJS/gui/lib/promise-1.0.0.min.js", this.pluginPath), null);
		writer.endElement("script");
		writer.startElement("script", this);
		writer.writeAttribute("src", String.format("%s/marvinJS/js/marvinjslauncher.js", this.pluginPath), null);
		writer.endElement("script");

		/* for Viewer emulation */
		writer.startElement("style", this);
        	writer.writeText("  iframe#marvinjs-iframe {\n    width: 0;\n    height: 0;\n    display: initial;\n    position: absolute;"
		  + "\n    left: -1000;\n    top: -1000;\n    margin: 0;\n    padding: 0;\n  }\n", null);
		writer.endElement("style"); 
		writer.startElement("iframe", this);
		writer.writeAttribute("src", String.format("%s/marvinJS/marvinpack.html", this.pluginPath), null);
		writer.writeAttribute("id", "marvinjs-iframe", null);
		writer.endElement("iframe");
/*
		writer.startElement("script", this);
		writer.writeAttribute("src", String.format("%s/ipb/marvinJSView.js", this.pluginPath), null);
		writer.endElement("script");
*/

	}

	private void encodeMarvinJSViewer(ResponseWriter writer, String clientId) throws IOException {
		writer.startElement("img", this);
		writer.writeAttribute("id", clientId, null);
		writer.endElement("img");
		writer.startElement("script", this);
		writer.writeText(String.format(
		  "MarvinJSUtil.getPackage(\"#marvinjs-iframe\").then(function (marvinNameSpace) {\n"
		  + "  marvinNameSpace.onReady(function() {\n"
		  + "    document.getElementById('%s').src=marvinNameSpace.ImageExporter.molToDataUrl('%s');\n"
                  + "  }); },function (error) {\n"
                  + "   alert(\"Cannot retrieve marvin instance from iframe:\"+error);\n"
                  + "});\n", clientId, escape( (String) getValue())), null);
		writer.endElement("script");
	}

	private void initMarvinJSDefaults() {
		this.pluginParam = new TreeMap<String, String> ();
	}

	/******************************************************
	 *
	 * 
	 *
	 ******************************************************/

	/**
	 * Escape string for HTML / XML. Used mainly for 
	 * molecules in MDL MOL format.
	 * @param s string to escape
	 * @return escaped string
	 */
	private String escape(String s) {
		if(s == null) {
			return "";
		}
		return s.replace("\\", "\\\\")
		  .replace("\"", "\\\"")
		  .replace("\n", "\\n")
		  .replace("\r", "");
	}

	private void initPluginParam() {
	       switch(this.pluginType) {
			case "JChemPaint":
				initJChemPaintDefaults();
				break;
			case "Marvin" :
				initMarvinDefaults();
				break;
			case "MarvinJS":
				initMarvinJSDefaults();
				break;
			case "MolPaintJS":
				initMolPaintJSDefaults();
				break;
			default :
				this.logger.warn("initPluginParam() unknown plugin type: " + this.pluginType);
		}

	}

	public void setHeight(int h) { this.height = h; }

	public void setPluginParameter(String k, String v) {
		this.pluginParam.put(k, v);
	}

	public void setPluginType(String clientId, String pt) {
		if(pt != null) {
			// this.logger.info(String.format("setPluginType(%s): pluginType=%s", clientId, pt));
			this.pluginType = pt;
			initPluginParam();
		}
	}

	public void setWidth(int w) { this.width = w; }
}
