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
import rational.mapping.Believes;
import rational.mapping.Task;
import wpsActivator.wpsStart;
import wpsPeasantFamily.Agent.PeasantFamilyBDIAgentBelieves;
import wpsPeasantFamily.Data.ResourceNeededType;
import wpsPeasantFamily.Data.TimeConsumedBy;
import wpsSocietyMarket.MarketAgentGuard;
import wpsSocietyMarket.MarketMessage;
import static wpsSocietyMarket.MarketMessageType.BUY_WATER;
import wpsViewer.Agent.wpsReport;

/**
 *
 * @author jairo
 */
public class ObtainWaterTask extends Task {

    /**
     *
     */
    public ObtainWaterTask() {
    }

    /**
     *
     * @param parameters
     */
    @Override
    public void executeTask(Believes parameters) {

        PeasantFamilyBDIAgentBelieves believes = (PeasantFamilyBDIAgentBelieves) parameters;
        wpsReport.info("🚰🚰🚰 Comprando Agua", believes.getPeasantProfile().getPeasantFamilyAlias());
        believes.useTime(TimeConsumedBy.valueOf(this.getClass().getSimpleName()));

        try {
            AdmBESA adm = AdmBESA.getInstance();
            AgHandlerBESA ah = adm.getHandlerByAlias(wpsStart.config.getMarketAgentName());

            MarketMessage marketMessage = new MarketMessage(
                    BUY_WATER,
                    believes.getPeasantProfile().getPeasantFamilyAlias(),
                    100
            );

            EventBESA ev = new EventBESA(
                    MarketAgentGuard.class.getName(),
                    marketMessage);
            ah.sendEvent(ev);

        } catch (ExceptionBESA ex) {
            wpsReport.error(ex, "obtainWaterTask");
        }
        believes.setCurrentResourceNeededType(ResourceNeededType.NONE);

    }

    /**
     *
     * @param parameters
     */
    @Override
    public void interruptTask(Believes parameters) {
        ((PeasantFamilyBDIAgentBelieves) parameters).setCurrentResourceNeededType(ResourceNeededType.NONE);
    }

    /**
     *
     * @param parameters
     */
    @Override
    public void cancelTask(Believes parameters) {
        ((PeasantFamilyBDIAgentBelieves) parameters).setCurrentResourceNeededType(ResourceNeededType.NONE);
    }

    /**
     *
     * @param parameters
     * @return
     */
    @Override
    public boolean checkFinish(Believes parameters) {
        return ((PeasantFamilyBDIAgentBelieves) parameters).getCurrentResourceNeededType() != ResourceNeededType.WATER;
    }
}
