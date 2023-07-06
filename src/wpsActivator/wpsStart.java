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
package wpsActivator;

import BESA.ExceptionBESA;
import BESA.Kernel.Agent.AgentBESA;
import BESA.Kernel.Agent.Event.EventBESA;
import BESA.Kernel.System.AdmBESA;
import BESA.Kernel.System.Directory.AgHandlerBESA;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import wpsControl.Agent.ControlAgent;
import wpsControl.Agent.ControlCurrentDate;
import wpsPeasantFamily.Agent.Guards.StatusGuard;
import wpsPeasantFamily.Agent.PeasantFamilyBDIAgent;
import wpsPeasantFamily.Agent.HeartBeatGuard;
import wpsPerturbation.Agent.PerturbationAgent;
import wpsSociety.Agent.SocietyAgent;
import wpsSocietyBank.Agent.BankAgent;
import wpsSocietyMarket.MarketAgent;
import wpsViewer.Agent.wpsReport;
import wpsViewer.Agent.wpsViewerAgent;

/**
 *
 */
public class wpsStart {

    private static int PLAN_ID = 0;
    final private static double PASSWD = 0.91;
    public static wpsConfig config;
    public static int peasantFamiliesAgents = 100;
    private final static int SIMULATION_TIME = 15;
    public final static int DAYS_TO_CHECK = 7;
    static final long startTime = System.currentTimeMillis();

    /**
     * The main method to start the simulation.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        config = wpsConfig.getInstance();

        // Set init date of simulation
        ControlCurrentDate.getInstance().setCurrentDate(config.getStartSimulationDate());

        List<PeasantFamilyBDIAgent> peasantFamilyBDIAgents = new ArrayList<>();

        try {

            ControlAgent controlAgent = ControlAgent.createAgent(config.getControlAgentName(), PASSWD);
            SocietyAgent societyAgent = SocietyAgent.createAgent(config.getSocietyAgentName(), PASSWD);
            BankAgent bankAgent = BankAgent.createBankAgent(config.getBankAgentName(), PASSWD);
            MarketAgent marketAgent = MarketAgent.createAgent(config.getMarketAgentName(), PASSWD);
            PerturbationAgent perturbationAgent = PerturbationAgent.createAgent(PASSWD);
            wpsViewerAgent viewerAgent = wpsViewerAgent.createAgent(config.getViewerAgentName(), PASSWD);

            // @TODO: Regular and Proactive Peasant Agents
            for (int i = 0; i < peasantFamiliesAgents; i++) {
                PeasantFamilyBDIAgent peasantFamilyBDIAgent = new PeasantFamilyBDIAgent(
                        config.getUniqueFarmerName(),
                        config.getFarmerProfile()
                );
                peasantFamilyBDIAgents.add(peasantFamilyBDIAgent);
            }

            // Simulation Start
            startAllAgents(
                    peasantFamilyBDIAgents,
                    viewerAgent,
                    controlAgent,
                    societyAgent,
                    bankAgent,
                    marketAgent,
                    perturbationAgent
            );
            printHeader();


        } catch (Exception ex) {
            wpsReport.error(ex, "wpsStart");
        }
    }

    /**
     * Gets the next plan ID.
     *
     * @return the next plan ID
     */
    public static int getPlanID() {
        return ++PLAN_ID;
    }

    /**
     * Starts all the agents and begins the simulation.
     *
     * @param peasantFamilies the list of peasant family agents
     * @param wpsAgents the other simulation agents
     * @throws ExceptionBESA if there is an exception while starting the agents
     */
    private static void startAllAgents(List<PeasantFamilyBDIAgent> peasantFamilies, AgentBESA... wpsAgents) throws ExceptionBESA {

        try {
            // Starting general simulation agents
            for (AgentBESA agent : wpsAgents) {
                agent.start();
                wpsReport.info(agent.getAlias() + " Started", "wpsStart");
            }
            // Starting families agents
            for (PeasantFamilyBDIAgent peasantFamily : peasantFamilies) {
                peasantFamily.start();
                //wpsReport.info(peasantFamily.getAlias() + " Started");
            }
            // first heart beat to families
            for (int i = 1; i <= peasantFamiliesAgents; i++) {
                AdmBESA adm = AdmBESA.getInstance();
                EventBESA eventBesa = new EventBESA(HeartBeatGuard.class.getName(), null);
                AgHandlerBESA agHandler = adm.getHandlerByAlias("PeasantFamily_" + i);
                agHandler.sendEvent(eventBesa);
            }

        } catch (ExceptionBESA ex) {
            wpsReport.error(ex, "wpsStart");
        }

        stopSimulationByTime();
    }

    /**
     * Stops the simulation after a specified time.
     *
     * @throws ExceptionBESA if there is an exception while stopping the agents
     */
    @SuppressWarnings("rawtypes")
    public static void stopSimulation() throws ExceptionBESA {
        getStatus();
        AdmBESA adm = AdmBESA.getInstance();
        Enumeration enumeration = adm.getIdList();
        while (enumeration.hasMoreElements()) {
            adm.killAgent((String) enumeration.nextElement(), PASSWD);
        }
        adm.kill(0.09);
        // Calculate the time of simulation
        wpsReport.info("Simulation finished in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds.\n\n\n\n", "wpsStart");
        System.exit(0);

    }

    public static void stopSimulationByTime() throws ExceptionBESA {

        // Closing simulation after X minutes
        try {
            Thread.sleep((60 * SIMULATION_TIME) * 1000);
            stopSimulation();
        } catch (InterruptedException e) {
            wpsReport.error(e.getMessage(), "wpsStart");
        }

    }

    /**
     * Sends status events to the peasant family agents.
     */
    public static void getStatus() {
        // first heart beat to families
        try {
            for (int i = 1; i <= peasantFamiliesAgents; i++) {
                AdmBESA adm = AdmBESA.getInstance();
                EventBESA eventBesa = new EventBESA(StatusGuard.class.getName(), null);
                AgHandlerBESA agHandler = adm.getHandlerByAlias("PeasantFamily_" + i);
                agHandler.sendEvent(eventBesa);
            }
            Thread.sleep(5000);
        } catch (ExceptionBESA | InterruptedException ex) {
            wpsReport.error(ex, "wpsStart");
        }
    }

    /**
     * Print header at Simulation begin
     */
    public static void printHeader() {

        wpsReport.info("""
                                       
                                       
                                       
                 * ==========================================================================
                 *   __      __ _ __   ___           WellProdSim                            *
                 *   \\ \\ /\\ / /| '_ \\ / __|          @version 1.0                           *
                 *    \\ V  V / | |_) |\\__ \\          @since 2023                            *
                 *     \\_/\\_/  | .__/ |___/                                                 *
                 *             | |                   @author Jairo Serrano                  *
                 *             |_|                   @author Enrique Gonzalez               *
                 * ==========================================================================
                 * Social Simulator used to estimate productivity and well-being of peasant *
                 * families. It is event oriented, high concurrency, heterogeneous time     *
                 * management and emotional reasoning BDI.                                  *
                 * ==========================================================================
                 
                """, "wpsStart");
    }

}
