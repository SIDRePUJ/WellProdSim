/**
 * ==========================================================================
 * __      __ _ __   ___  *    WellProdSim                                  *
 * \ \ /\ / /| '_ \ / __| *    @version 1.0                                 *
 * \ V  V / | |_) |\__ \ *    @since 2023                                  *
 * \_/\_/  | .__/ |___/ *                                                 *
 * | |          *    @author Jairo Serrano                        *
 * |_|          *    @author Enrique Gonzalez                     *
 * ==========================================================================
 * Social Simulator used to estimate productivity and well-being of peasant *
 * families. It is event oriented, high concurrency, heterogeneous time     *
 * management and emotional reasoning BDI.                                  *
 * ==========================================================================
 */
package wpsControl.Agent;

import BESA.ExceptionBESA;
import BESA.Kernel.Agent.Event.EventBESA;
import BESA.Kernel.Agent.StateBESA;
import BESA.Kernel.System.AdmBESA;
import BESA.Kernel.System.Directory.AgHandlerBESA;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import wpsActivator.wpsStart;
import wpsPeasantFamily.Agent.Guards.FromControlGuard;
import wpsViewer.Agent.wpsReport;

public class ControlAgentState extends StateBESA implements Serializable {

    private AtomicInteger activeAgentsCount = new AtomicInteger(0);
    private ConcurrentMap<String, Boolean> agentMap = new ConcurrentHashMap<>();
    private Timer timer = new Timer();
    public ControlAgentState() {
        super();
    }

    public ConcurrentMap<String, Boolean> getAgentMap() {
        return agentMap;
    }

    public boolean getActiveAgentsReady() {
        if (this.activeAgentsCount.get() == wpsStart.peasantFamiliesAgents) {
            return true;
        } else {
            return false;
        }
    }

    public int getActiveAgentsCount() {
        return this.activeAgentsCount.get();
    }

    public void resetActiveAgents() {
        this.activeAgentsCount.set(0);
        this.clearAgentMap();
    }

    public void increaseActiveAgents() {
        this.activeAgentsCount.incrementAndGet();
    }

    public void clearAgentMap() {
        this.agentMap.clear();
    }

    public void modifyAgentMap(String agentName, boolean status) {
        this.agentMap.put(agentName, status);
        // Reschedule the timer to execute checkAgentsAlive in 2 minutes
        /*this.timer.schedule(
                new CheckAgentsAliveTask(),
                2 * 60 * 1000
        );*/
    }

    private void checkAgentsAlive() {
        wpsStart.getStatus();
        wpsReport.info("\n\n--- agentMap ---\n\n" + this.agentMap, "ControlAgentState.checkAgentsAlive");

        // Detectar agentes no responsivos
        List<String> unresponsiveAgents = new ArrayList<>();
        for (int i = 1; i <= wpsStart.peasantFamiliesAgents; i++) {
            String agentName = "PeasantFamily_" + i;
            if (!agentMap.containsKey(agentName)) {
                wpsReport.info("Agent " + agentName + " not found in agentMap", "ControlAgentState.checkAgentsAlive");
                unresponsiveAgents.add(agentName);
            } else {
                try {
                    AdmBESA adm = AdmBESA.getInstance();
                    EventBESA eventBesa = new EventBESA(FromControlGuard.class.getName(), null);
                    AgHandlerBESA agHandler = adm.getHandlerByAlias(agentName);
                    agHandler.sendEvent(eventBesa);
                } catch (ExceptionBESA ex) {
                    wpsReport.error(ex, "ControlAgentState.checkAgentsAlive");
                }
            }
        }

        // Guardar los agentes no responsivos en una variable separada
        String unresponsiveAgentList = String.join(", ", unresponsiveAgents);
        wpsReport.info("\n\nAgentes no responsivos: " + unresponsiveAgentList + "\n\n", "ControlAgentState.checkAgentsAlive");

        this.resetActiveAgents();
        ControlCurrentDate.getInstance().getDatePlusXDaysAndUpdate(wpsStart.DAYS_TO_CHECK);
    }

    private class CheckAgentsAliveTask extends TimerTask {

        @Override
        public void run() {
            checkAgentsAlive();
        }
    }

}

