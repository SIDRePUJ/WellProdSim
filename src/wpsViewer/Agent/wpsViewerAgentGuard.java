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
package wpsViewer.Agent;

import BESA.Kernel.Agent.Event.EventBESA;
import BESA.Kernel.Agent.GuardBESA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


/**
 * @author jairo
 */
public class wpsViewerAgentGuard extends GuardBESA {

    private static final Logger logger = LoggerFactory.getLogger(wpsReport.class);

    /**
     * @param event
     */
    @Override
    public void funcExecGuard(EventBESA event) {
        wpsViewerMessage viewerMessage = (wpsViewerMessage) event.getData();
        try {
            MDC.put("peasantAlias", viewerMessage.getPeasantAlias());
            switch (viewerMessage.getLevel()) {
                case "TRACE" -> logger.trace(viewerMessage.getPeasantMessage());
                case "DEBUG" -> logger.debug(viewerMessage.getPeasantMessage());
                case "WARN" -> logger.warn(viewerMessage.getPeasantMessage());
                case "ERROR" -> logger.error(viewerMessage.getPeasantMessage());
                default -> logger.info(viewerMessage.getPeasantMessage());
            }
        } finally {
            MDC.clear();
        }
    }

}
