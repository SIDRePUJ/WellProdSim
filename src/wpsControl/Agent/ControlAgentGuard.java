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
import BESA.Kernel.Agent.GuardBESA;
import BESA.Kernel.System.AdmBESA;
import BESA.Kernel.System.Directory.AgHandlerBESA;
import wpsActivator.wpsStart;
import wpsPeasantFamily.Agent.Guards.FromControlGuard;
import wpsPeasantFamily.Agent.Guards.ToControlMessage;
import wpsViewer.Agent.wpsReport;

import java.util.Map;

/**
 *
 * @author jairo
 */
public class ControlAgentGuard extends GuardBESA {

    /**
     *
     * @param event Event rising the Guard
     */
    @Override
    public void funcExecGuard(EventBESA event) {
        String currentDate = "";
        ToControlMessage toControlMessage = (ToControlMessage) event.getData();
        String agentAlias = toControlMessage.getPeasantFamilyAlias();
        ControlAgentState state = (ControlAgentState) this.getAgent().getState();

        //wpsReport.debug(state.printAgentMap(), "ControlAgentGuard");

        state.modifyAgentMap(agentAlias);

        if (state.allAgentsAlive()) {
            try {
                AdmBESA adm = AdmBESA.getInstance();
                EventBESA eventBesa = new EventBESA(FromControlGuard.class.getName(), null);
                for (String agentName : state.getAliveAgentMap().keySet()) {
                    AgHandlerBESA agHandler = adm.getHandlerByAlias(agentName);
                    agHandler.sendEvent(eventBesa);
                    wpsReport.debug("Unblocking event to " + agentName, "ControlAgentGuard");
                }
            } catch (ExceptionBESA ex) {
                wpsReport.error(ex, "ControlAgentGuard");
            }
            //state.resetActiveAgents();
            currentDate = ControlCurrentDate.getInstance().getDatePlusXDaysAndUpdate(wpsStart.DAYS_TO_CHECK);
            wpsReport.debug("Current Date General: " + currentDate, "ControlAgentState");
        }else{
            int trueCount = 0;
            int falseCount = 0;

            for (Boolean value : state.getAliveAgentMap().values()) {
                if (value) {
                    trueCount++;
                } else {
                    falseCount++;
                }
            }
            wpsReport.debug("Agentes llegaron: " + trueCount + " - Agentes ausentes: " + falseCount, "ControlAgentState");
        }

        if (currentDate.contains("2023")) {
            try {
                wpsStart.stopSimulation();
            } catch (ExceptionBESA ex) {
                wpsReport.error(ex, "ControlAgentGuard");
            }
        }
    }


}
