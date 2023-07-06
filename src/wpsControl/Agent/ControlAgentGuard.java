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

        if (state.getAgentMap().containsKey(agentAlias)) {
            wpsReport.warn(agentAlias + " has already arrived.", "ControlAgentGuard");
            return;
        }

        wpsReport.warn(agentAlias + " Arrive - " + toControlMessage.getDays(), "ControlAgentGuard");

        state.increaseActiveAgents();
        state.modifyAgentMap(agentAlias, true);

        wpsReport.warn(state.getActiveAgentsCount() + " Finished.", "ControlAgentGuard");

        if (state.getActiveAgentsReady()) {
            try {
                for (int i = 1; i <= wpsStart.peasantFamiliesAgents; i++) {
                    AdmBESA adm = AdmBESA.getInstance();
                    EventBESA eventBesa = new EventBESA(FromControlGuard.class.getName(), null);
                    AgHandlerBESA agHandler = adm.getHandlerByAlias("PeasantFamily_" + i);
                    agHandler.sendEvent(eventBesa);
                    //wpsReport.warn("Activando PeasantFamily_" + i);
                }
            } catch (ExceptionBESA ex) {
                wpsReport.error(ex, "ControlAgentGuard");
            }

            state.resetActiveAgents();
            currentDate = ControlCurrentDate.getInstance().getDatePlusXDaysAndUpdate(wpsStart.DAYS_TO_CHECK);
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
