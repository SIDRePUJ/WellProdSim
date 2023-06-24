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
package wpsPeasantFamily.Tasks.L3Development;

import BESA.ExceptionBESA;
import BESA.Kernel.Agent.Event.EventBESA;
import BESA.Kernel.System.AdmBESA;
import BESA.Kernel.System.Directory.AgHandlerBESA;
import wpsWorld.Agent.WorldGuard;
import wpsWorld.Messages.WorldMessage;
import rational.mapping.Believes;
import rational.mapping.Task;
import wpsPeasantFamily.Agent.PeasantFamilyBDIAgentBelieves;
import wpsPeasantFamily.Data.ResourceNeededType;
import wpsPeasantFamily.Data.TimeConsumedBy;
import wpsViewer.Agent.wpsReport;
import static wpsWorld.Messages.WorldMessageType.CROP_OBSERVE;
import static wpsWorld.Messages.WorldMessageType.CROP_INFORMATION;

/**
 *
 * @author jairo
 */
public class CheckCropsTask extends Task {

    /**
     *
     */
    public CheckCropsTask() {
    }

    /**
     *
     * @param parameters
     */
    @Override
    public void executeTask(Believes parameters) {
        //wpsReport.info("⚙️⚙️⚙️");
        PeasantFamilyBDIAgentBelieves believes = (PeasantFamilyBDIAgentBelieves) parameters;
        String peasantFamilyAlias = believes.getPeasantProfile().getPeasantFamilyLandAlias();
        // @TODO: falta calcular el tiempo necesario para el cultivo
        believes.useTime(TimeConsumedBy.valueOf(this.getClass().getSimpleName()));

        if (believes.getPeasantProfile().getWaterAvailable() < 20) {
            believes.setCurrentResourceNeededType(ResourceNeededType.WATER);
        }

        try {
            AdmBESA adm = AdmBESA.getInstance();
            AgHandlerBESA ah = adm.getHandlerByAlias(peasantFamilyAlias);

            WorldMessage worldMessage;

            // 80% de probabilidad de ejecutar
            if (Math.random() < 0.2) {
                worldMessage = new WorldMessage(
                        CROP_INFORMATION,
                        believes.getPeasantProfile().getCurrentCropName(),
                        believes.getInternalCurrentDate(),
                        peasantFamilyAlias
                );
                //wpsReport.warn("enviado CROP_INFORMATION");
            } else {
                worldMessage = new WorldMessage(
                        CROP_OBSERVE,
                        believes.getPeasantProfile().getCurrentCropName(),
                        believes.getInternalCurrentDate(),
                        peasantFamilyAlias
                );
                //wpsReport.warn("enviado CROP_OBSERVE");
            }

            EventBESA event = new EventBESA(
                    WorldGuard.class.getName(),
                    worldMessage
            );
            ah.sendEvent(event);

        } catch (ExceptionBESA ex) {
            wpsReport.error(ex);
        }

        believes.setCheckedToday();
        this.setTaskFinalized();

        //wpsReport.warn(believes.toJson());

    }

    /**
     *
     * @param parameters
     */
    @Override
    public void interruptTask(Believes parameters) {
        this.setTaskFinalized();
    }

    /**
     *
     * @param parameters
     */
    @Override
    public void cancelTask(Believes parameters) {
        this.setTaskFinalized();
    }

    /**
     *
     * @param believes
     * @return
     */
    @Override
    public boolean checkFinish(Believes parameters) {
        PeasantFamilyBDIAgentBelieves believes = (PeasantFamilyBDIAgentBelieves) parameters;
        return believes.isCheckedToday();
    }
}