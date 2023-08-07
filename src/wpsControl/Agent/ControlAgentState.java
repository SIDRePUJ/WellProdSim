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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import wpsActivator.wpsStart;
import wpsPeasantFamily.Agent.Guards.FromControlGuard;
import wpsPeasantFamily.Agent.Guards.KillZombieGuard;
import wpsPeasantFamily.Agent.Guards.StatusGuard;
import wpsViewer.Agent.wpsReport;

public class ControlAgentState extends StateBESA implements Serializable {

    private AtomicInteger activeAgentsCount = new AtomicInteger(0);
    private ConcurrentMap<String, Boolean> agentMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Boolean> deadAgentMap = new ConcurrentHashMap<>();
    private Timer timer = new Timer();
    public ControlAgentState() {
        super();
    }
    public ConcurrentMap<String, Boolean> getAliveAgentMap() {
        return agentMap;
    }
    public ConcurrentMap<String, Boolean> getDeadAgentMap() {
        return deadAgentMap;
    }
    private ConcurrentMap<String, Timer> agentTimers = new ConcurrentHashMap<>();

    public boolean allAgentsAlive() {
        int trueCount = 0;
        int falseCount = 0;

        for (Boolean value : agentMap.values()) {
            if (value) {
                trueCount++;
            } else {
                falseCount++;
            }
        }

        wpsReport.debug("Number of true values: " + trueCount, "ControlAgentState");
        wpsReport.debug("Number of false values: " + falseCount, "ControlAgentState");

        return !agentMap.containsValue(false);
    }

    public int getActiveAgentsCount() {
        return this.activeAgentsCount.get();
    }

    public void resetActiveAgents() {
        this.activeAgentsCount.set(0);
        this.agentMap.replaceAll((k, v) -> false);
    }

    public void increaseActiveAgents() {
        this.activeAgentsCount.incrementAndGet();
    }

    public String printAgentMap() {
        List<String> keys = new ArrayList<>(agentMap.keySet());
        Collections.sort(keys);
        String agentMapString = "Blocked Agents:\n";
        for (String key : keys) {
            if (!agentMap.get(key)) {
                agentMapString = agentMapString.concat(key + ": " + agentMap.get(key) + "\n");
            }
        }
        return agentMapString;
    }

    public String printDeadAgentMap() {
        List<String> keys = new ArrayList<>(deadAgentMap.keySet());
        Collections.sort(keys);
        String agentMapString = "Dead Agents:\n";
        for (String key : keys) {
            agentMapString = agentMapString.concat(key + ": " + agentMap.get(key) + "\n");
        }
        return agentMapString;
    }

    public void addAgentToMap(String agentName) {
        wpsReport.debug("Agent " + agentName + " is new and alive", "ControlAgentState");
        this.agentMap.put(agentName, false);
    }

    public void removeAgentFromMap(String agentName) {
        wpsReport.debug("Agent " + agentName + " is dead", "ControlAgentState");
        this.agentMap.remove(agentName);
        this.deadAgentMap.put(agentName, true);
    }

    public void modifyAgentMap(String agentName) {
        // Cancela cualquier temporizador existente para este agente
        Timer existingTimer = agentTimers.get(agentName);
        if (existingTimer != null) {
            existingTimer.cancel();
        }

        // Inicia un nuevo temporizador para este agente
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                wpsReport.debug("Agent " + agentName + " is dead by ControlAgentState", "ControlAgentState");
                try {
                    AdmBESA adm = AdmBESA.getInstance();
                    EventBESA eventBesa = new EventBESA(KillZombieGuard.class.getName(), null);
                    AgHandlerBESA agHandler = adm.getHandlerByAlias(agentName);
                    agHandler.sendEvent(eventBesa);
                } catch (ExceptionBESA ex) {
                    wpsReport.error(ex, "ControlAgentState");
                }
            }
        }, 2 * 60 * 1000); // 1 minuto
        agentTimers.put(agentName, timer);

        // Marca el agente como "vivo"
        this.agentMap.put(agentName, true);
    }

}

