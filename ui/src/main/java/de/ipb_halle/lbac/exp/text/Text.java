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
package de.ipb_halle.lbac.exp.text;

import de.ipb_halle.lbac.exp.assay.AssayRecord;
import java.util.ArrayList;
import java.util.List;

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author fbroda
 */
public class Text extends ExpRecord implements DTO {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private String text;

    /**
     * default constructor
     */
    public Text() {
        super();
        setType(ExpRecordType.TEXT);
    }


    /* 
     * method to debug and silence errors in Mojarra 2.2.12
     */
    public List<AssayRecord> getRecords() {
        this.logger.info("TextController().getRecords(index=={})", this.getIndex());
/*
 within nested ui:repeat loops, Mojarra calls this method although 
 the component should not be rendered.
 Apache MyFaces seem to not suffer from this condition

[WARN ] 2020-06-24 13:18:45 Text - getRecords()
java.lang.RuntimeException: Show stacktrace for index=6
        at de.ipb_halle.lbac.exp.text.Text.getRecords(Text.java:54) [classes/:1.3.0]
        at sun.reflect.GeneratedMethodAccessor2646.invoke(Unknown Source) ~[?:?]
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[?:1.8.0_232]
        at java.lang.reflect.Method.invoke(Method.java:498) ~[?:1.8.0_232]
        at javax.el.BeanELResolver.getValue(BeanELResolver.java:94) [el-api.jar:3.0.FR]
        at com.sun.faces.el.DemuxCompositeELResolver._getValue(DemuxCompositeELResolver.java:176) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.el.DemuxCompositeELResolver.getValue(DemuxCompositeELResolver.java:203) [javax.faces-2.2.12.jar:2.2.12]
        at org.apache.el.parser.AstValue.getValue(AstValue.java:169) [jasper-el.jar:8.5.32]
        at org.apache.el.ValueExpressionImpl.getValue(ValueExpressionImpl.java:190) [jasper-el.jar:8.5.32]
        at org.apache.webbeans.el22.WrappedValueExpression.getValue(WrappedValueExpression.java:70) [openwebbeans-el22-1.7.5.jar:1.7.5]
        at com.sun.faces.facelets.el.ContextualCompositeValueExpression.getValue(ContextualCompositeValueExpression.java:158) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.facelets.el.TagValueExpression.getValue(TagValueExpression.java:109) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.facelets.component.UIRepeat.getValue(UIRepeat.java:279) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.facelets.component.UIRepeat.getDataModel(UIRepeat.java:255) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.facelets.component.UIRepeat.setIndex(UIRepeat.java:523) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.facelets.component.UIRepeat.doVisitChildren(UIRepeat.java:800) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.facelets.component.UIRepeat.visitTree(UIRepeat.java:748) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UINamingContainer.visitTree(UINamingContainer.java:174) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UINamingContainer.visitTree(UINamingContainer.java:174) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.facelets.component.UIRepeat.visitChildren(UIRepeat.java:862) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.facelets.component.UIRepeat.visitTree(UIRepeat.java:759) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIForm.visitTree(UIForm.java:371) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.component.UIComponent.visitTree(UIComponent.java:1700) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.application.view.FaceletViewHandlingStrategy.locateComponentByClientId(FaceletViewHandlingStrategy.java:2093) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.application.view.FaceletViewHandlingStrategy.reapplyDynamicRemove(FaceletViewHandlingStrategy.java:2185) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.application.view.FaceletViewHandlingStrategy.reapplyDynamicActions(FaceletViewHandlingStrategy.java:2127) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.application.view.FaceletViewHandlingStrategy.buildView(FaceletViewHandlingStrategy.java:977) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.lifecycle.RenderResponsePhase.execute(RenderResponsePhase.java:99) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.lifecycle.Phase.doPhase(Phase.java:101) [javax.faces-2.2.12.jar:2.2.12]
        at com.sun.faces.lifecycle.LifecycleImpl.render(LifecycleImpl.java:219) [javax.faces-2.2.12.jar:2.2.12]
        at javax.faces.webapp.FacesServlet.service(FacesServlet.java:659) [javax.faces-2.2.12.jar:2.2.12]
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231) [catalina.jar:8.5.32]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) [catalina.jar:8.5.32]
        at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52) [tomcat-websocket.jar:8.5.32]
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) [catalina.jar:8.5.32]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) [catalina.jar:8.5.32]
        at org.apache.openejb.server.httpd.EEFilter.doFilter(EEFilter.java:65) [openejb-http-7.1.0.jar:7.1.0]
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) [catalina.jar:8.5.32]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) [catalina.jar:8.5.32]
        at de.ipb_halle.lbac.cloud.servlet.SessionTimeoutCookieFilter.doFilter(SessionTimeoutCookieFilter.java:65) [classes/:1.3.0]
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) [catalina.jar:8.5.32]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) [catalina.jar:8.5.32]
        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:198) [catalina.jar:8.5.32]
        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96) [catalina.jar:8.5.32]
        at org.apache.tomee.catalina.OpenEJBValve.invoke(OpenEJBValve.java:44) [tomee-catalina-7.1.0.jar:7.1.0]
        at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:493) [catalina.jar:8.5.32]
        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:140) [catalina.jar:8.5.32]
        at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:81) [catalina.jar:8.5.32]
        at org.apache.tomee.catalina.OpenEJBSecurityListener$RequestCapturer.invoke(OpenEJBSecurityListener.java:97) [tomee-catalina-7.1.0.jar:7.1.0]
        at org.apache.catalina.valves.AbstractAccessLogValve.invoke(AbstractAccessLogValve.java:650) [catalina.jar:8.5.32]
        at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:87) [catalina.jar:8.5.32]
        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:342) [catalina.jar:8.5.32]
        at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:800) [tomcat-coyote.jar:8.5.32]
        at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66) [tomcat-coyote.jar:8.5.32]
        at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:800) [tomcat-coyote.jar:8.5.32]
        at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1471) [tomcat-coyote.jar:8.5.32]
        at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49) [tomcat-coyote.jar:8.5.32]
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149) [?:1.8.0_232]
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624) [?:1.8.0_232]
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61) [tomcat-util.jar:8.5.32]
        at java.lang.Thread.run(Thread.java:748) [?:1.8.0_232]

        try {
            throw new RuntimeException(String.format("Show stacktrace for index=%d", this.getIndex()));
        } catch(Exception e) {
            this.logger.warn("getRecords()", (Throwable) e);
        }
*/
        return new ArrayList<AssayRecord> ();
    }

    public TextEntity createEntity() {
        return new TextEntity()
            .setExpRecordId(getExpRecordId())
            .setText(this.text);
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
