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
package wpsPeasantFamily.Tasks.L4SkillsResources;

import BESA.ExceptionBESA;
import BESA.Kernel.Agent.Event.EventBESA;
import BESA.Kernel.System.AdmBESA;
import BESA.Kernel.System.Directory.AgHandlerBESA;
import wpsWorld.Agent.WorldGuard;
import wpsWorld.Messages.WorldMessage;
import static wpsWorld.Messages.WorldMessageType.CROP_IRRIGATION;
import wpsControl.Agent.ControlCurrentDate;
import rational.mapping.Believes;
import rational.mapping.Task;
import wpsPeasantFamily.Agent.PeasantFamilyBDIAgentBelieves;
import wpsActivator.wpsStart;
import wpsPeasantFamily.Data.ResourceNeededType;
import wpsPeasantFamily.Data.TimeConsumedBy;
import wpsSocietyMarket.MarketAgentGuard;
import wpsSocietyMarket.MarketMessage;
import static wpsSocietyMarket.MarketMessageType.BUY_TOOLS;
import static wpsSocietyMarket.MarketMessageType.BUY_WATER;
import wpsViewer.Agent.wpsReport;

/**
 *
 * @author jairo
 */
public class ObtainToolsTask extends Task {

    /**
     *
     */
    public ObtainToolsTask() {
    }

    /**
     *
     * @param parameters Believes
     */
    @Override
    public void executeTask(Believes parameters) {
        PeasantFamilyBDIAgentBelieves believes = (PeasantFamilyBDIAgentBelieves) parameters;
        believes.useTime(TimeConsumedBy.valueOf(this.getClass().getSimpleName()));

        try {
            AdmBESA adm = AdmBESA.getInstance();
            AgHandlerBESA ah = adm.getHandlerByAlias(wpsStart.config.getMarketAgentName());
            MarketMessage marketMessage = new MarketMessage(
                    BUY_TOOLS,
                    believes.getPeasantProfile().getPeasantFamilyAlias(),
                    10);

            EventBESA ev = new EventBESA(
                    MarketAgentGuard.class.getName(),
                    marketMessage);
            ah.sendEvent(ev);
            wpsReport.debug("ObtainToolsTask.executeTask: "
                    + believes.getPeasantProfile().getPeasantFamilyAlias()
                    + " BUY_TOOLS",
                    believes.getPeasantProfile().getPeasantFamilyAlias()
            );

        } catch (ExceptionBESA ex) {
            wpsReport.error(ex, "obtainToolsTask.executeTask");
        }
        believes.setCurrentResourceNeededType(ResourceNeededType.NONE);
    }

    /**
     *
     * @param parameters Believes
     */
    @Override
    public void interruptTask(Believes parameters) {}

    /**
     *
     * @param parameters Believes
     */
    @Override
    public void cancelTask(Believes parameters) {}


    /**
     *
     * @param parameters Believes
     * @return
     */
    @Override
    public boolean checkFinish(Believes parameters) {
        PeasantFamilyBDIAgentBelieves believes = (PeasantFamilyBDIAgentBelieves) parameters;
        return believes.getCurrentResourceNeededType() == ResourceNeededType.NONE;
    }
}
