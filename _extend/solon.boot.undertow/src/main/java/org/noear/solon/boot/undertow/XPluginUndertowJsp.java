package org.noear.solon.boot.undertow;

import io.undertow.Undertow;
import io.undertow.UndertowMessages;
import io.undertow.UndertowOptions;
import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.*;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.DefaultClassIntrospector;
import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.noear.solon.XApp;
import org.noear.solon.XUtil;
import org.noear.solon.core.XClassLoader;
import org.noear.solon.core.XPlugin;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

/**
 * @Created by: Yukai
 * @Date: 2019/3/28 15:50
 * @Description : Yukai is so handsome xxD
 */
public class XPluginUndertowJsp implements XPlugin {
    private static Undertow.Builder serverBuilder = null;
    private static Undertow _server = null;


    @Override
    public void start(XApp app) {
        try {
            setupJsp(app);
        } catch (Exception e) {
            e.printStackTrace();
        }
        _server.start();

    }


    public void setupJsp(XApp app) throws Exception {
        final ServletContainer container = ServletContainer.Factory.newInstance();

        String fileRoot = getResourceRoot();

        DeploymentInfo builder = new DeploymentInfo()
                .setClassLoader(XPluginUndertowJsp.class.getClassLoader())
                .setDeploymentName("solon")
                .setContextPath("/")
                .setDefaultEncoding(XServerProp.encoding_request)
                .setClassIntrospecter(DefaultClassIntrospector.INSTANCE)
                .setResourceManager(new DefaultResourceManager(XClassLoader.global(), fileRoot))
                .setDefaultMultipartConfig(new MultipartConfigElement(System.getProperty("java.io.tmpdir")))
                .addServlet(JspServletBuilder.createServlet("JSPServlet", "*.jsp"))
                .addServlet(new ServletInfo("ACTServlet", UtHttpHandlerJsp.class).addMapping("/"));  //这个才是根据上下文对象`XContext`进行分发

        if (XServerProp.session_timeout > 0) {
            builder.setDefaultSessionTimeout(XServerProp.session_timeout);
        }

        HashMap<String, TagLibraryInfo> tagLibraryMap = TldLocator.createTldInfos("WEB-INF");

        JspServletBuilder.setupDeployment(builder, new HashMap<String, JspPropertyGroup>(),tagLibraryMap , new HackInstanceManager());

        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        HttpHandler jsp_handler = manager.start();

        //************************** init server start******************
        serverBuilder = getInstance().setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE,false);;
        serverBuilder.addHttpListener(app.port(), "0.0.0.0");
        serverBuilder.setHandler(jsp_handler);

        _server = serverBuilder.build();

        //************************* init server end********************
    }


    // 允许在其他代码层访问容器构造器实例
    public static Undertow.Builder getInstance() {
        synchronized (XPluginImp.class) {
            if (serverBuilder == null) {
                synchronized (XPlugin.class) {
                    serverBuilder = Undertow.builder();
                }
            }
        }
        return serverBuilder;
    }

    @Override
    public void stop() throws Throwable {
        if (_server != null) {
            _server.stop();
            _server = null;
        }
    }

    private String getResourceRoot() throws FileNotFoundException {
        URL rootURL = getRootPath();
        if (rootURL == null) {
            throw new FileNotFoundException("Unable to find root");
        }
        String resURL = rootURL.toString();

        boolean isDebug = XApp.cfg().isDebugMode();
        if (isDebug && (resURL.startsWith("jar:") == false)) {
            int endIndex = resURL.indexOf("target");
            return resURL.substring(0, endIndex) + "src/main/resources/";
        }

        return "";
    }

    private URL getRootPath() {
        URL root = XUtil.getResource("/");
        if (root != null) {
            return root;
        }
        try {
            String path = XUtil.getResource("").toString();
            if (path.startsWith("jar:")) {
                int endIndex = path.indexOf("!");
                path = path.substring(0, endIndex + 1) + "/";
            } else {
                return null;
            }
            return new URL(path);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static class DefaultResourceManager implements ResourceManager{
        private final ClassLoader classLoader;
        private final String prefix;

        public DefaultResourceManager(ClassLoader classLoader, String prefix) {
            this.classLoader = classLoader;
            if (prefix.isEmpty()) {
                this.prefix = "";
            } else if (prefix.endsWith("/")) {
                this.prefix = prefix;
            } else {
                this.prefix = prefix + "/";
            }

        }

        @Override
        public Resource getResource(String path) throws IOException {
            String modPath = path;
            if (path.startsWith("/")) {
                modPath = path.substring(1);
            }

            String realPath = this.prefix + modPath;
            URL resource = null;
            if (realPath.startsWith("file:")) {
                resource = URI.create(realPath).toURL();
            } else {
                resource = this.classLoader.getResource(realPath);
            }

            return resource == null ? null : new URLResource(resource, path);
        }

        @Override
        public boolean isResourceChangeListenerSupported() {
            return false;
        }

        @Override
        public void registerResourceChangeListener(ResourceChangeListener listener) {
            throw UndertowMessages.MESSAGES.resourceChangeListenerNotSupported();
        }

        @Override
        public void removeResourceChangeListener(ResourceChangeListener listener) {
            throw UndertowMessages.MESSAGES.resourceChangeListenerNotSupported();
        }

        @Override
        public void close() throws IOException {
        }
    }
}
