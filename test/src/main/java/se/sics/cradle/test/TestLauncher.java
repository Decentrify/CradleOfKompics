/*
 * Copyright (C) 2016 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2016 Royal Institute of Technology (KTH)
 *
 * Cradle is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.cradle.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.dozy.DozyResource;
import se.sics.dozy.DozySyncI;
import se.sics.dozy.test.TestJsonREST;
import se.sics.dozy.dropwizard.DropwizardDozy;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;

/**
 *
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class TestLauncher extends ComponentDefinition {

    private Logger LOG = LoggerFactory.getLogger(TestLauncher.class);
    private String logPrefix = "";

    private DropwizardDozy webServer;

    public TestLauncher() {
        LOG.info("{}initiating...", logPrefix);

        subscribe(handleStart, control);
    }

    private Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);

            startWebServer();
        }
    };

    private void startWebServer() {
        LOG.info("{}starting web server", logPrefix);
        Map<String, DozySyncI> syncInterfaces = new HashMap<>();
        List<DozyResource> resources = new ArrayList<>();
        resources.add(new TestJsonREST());
        webServer = new DropwizardDozy(syncInterfaces, resources);
        Config config = ConfigFactory.load();
        String[] args = new String[]{"server", config.getString("webservice.server")};
        try {
            webServer.run(args);
         } catch (ConfigException.Missing ex) {
            LOG.error("{}bad configuration, could not find webservice.server", logPrefix);
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            LOG.error("{}webservice error", logPrefix);
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) throws IOException {
        if (Kompics.isOn()) {
            Kompics.shutdown();
        }
        Kompics.createAndStart(TestLauncher.class, Runtime.getRuntime().availableProcessors(), 20); // Yes 20 is totally arbitrary
        try {
            Kompics.waitForTermination();
        } catch (InterruptedException ex) {
            System.exit(1);
        }
    }
}
