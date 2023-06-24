/**
 * ==========================================================================
 * __      __ _ __   ___  *    WellProdSim                                  *
 * \ \ /\ / /| '_ \ / __| *    @version 1.0                                 *
 *  \ V  V / | |_) |\__ \ *    @since 2023                                  *
 *   \_/\_/  | .__/ |___/ *                                                 *
 *           | |          *    @author Jairo Serrano                        *
 *           |_|          *    @author Enrique Gonzalez                     *
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
import wpsActivator.wpsStart;
import wpsPeasantFamily.Agent.Guards.FromControlGuard;
import wpsViewer.Agent.wpsReport;

public class ControlAgentState extends StateBESA implements Serializable {

    private int activeAgentsCount;
    private Map<String, Boolean> agentMap = new HashMap<>();
    private Timer timer;

    public ControlAgentState() {
        super();
        this.activeAgentsCount = 0;
        this.timer = new Timer();
    }

    public boolean getActiveAgentsReady() {
        return (this.activeAgentsCount == wpsStart.peasantFamiliesAgents);
    }

    public int getActiveAgentsCount() {
        return this.activeAgentsCount;
    }

    public void resetActiveAgents() {
        this.activeAgentsCount = 0;
        this.clearAgentMap();
    }

    public synchronized void increaseActiveAgents() {
        this.activeAgentsCount++;
    }

    public synchronized void clearAgentMap() {
        this.agentMap.clear();
    }

    public synchronized void modifyAgentMap(String agentName, boolean status) {
        this.agentMap.put(agentName, status);
        // Reschedule the timer to execute checkAgentsAlive in 5 minutes
        this.timer.schedule(new CheckAgentsAliveTask(), 2 * 60 * 1000);
    }

    private class CheckAgentsAliveTask extends TimerTask {

        @Override
        public void run() {
            checkAgentsAlive();
        }
    }

    private void checkAgentsAlive() {
        wpsStart.getStatus();
        wpsReport.info("\n\n--- agentMap ---\n\n" + this.agentMap);

        // Detectar agentes no responsivos
        List<String> unresponsiveAgents = new ArrayList<>();
        for (int i = 1; i <= wpsStart.peasantFamiliesAgents; i++) {
            String agentName = "PeasantFamily_" + i;
            if (!agentMap.containsKey(agentName)) {
                unresponsiveAgents.add(agentName);
            } else {
                try {
                    AdmBESA adm = AdmBESA.getInstance();
                    EventBESA eventBesa = new EventBESA(FromControlGuard.class.getName(), null);
                    AgHandlerBESA agHandler = adm.getHandlerByAlias(agentName);
                    agHandler.sendEvent(eventBesa);
                } catch (ExceptionBESA ex) {
                    wpsReport.error(ex);
                }
            }
        }

        // Guardar los agentes no responsivos en una variable separada
        String unresponsiveAgentList = String.join(", ", unresponsiveAgents);
        wpsReport.info("\n\nAgentes no responsivos: " + unresponsiveAgentList + "\n\n");

        this.resetActiveAgents();
        ControlCurrentDate.getInstance().getDatePlusXDaysAndUpdate(wpsStart.DAYSTOCHECK);

    }
}
