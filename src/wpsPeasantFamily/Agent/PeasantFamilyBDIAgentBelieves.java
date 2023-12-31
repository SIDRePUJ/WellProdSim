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
package wpsPeasantFamily.Agent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import BESA.ExceptionBESA;
import BESA.Kernel.Agent.Event.EventBESA;
import BESA.Kernel.System.AdmBESA;
import BESA.Kernel.System.Directory.AgHandlerBESA;
import wpsActivator.wpsStart;
import wpsControl.Agent.ControlAgentGuard;
import wpsPeasantFamily.Agent.Guards.FromControlGuard;
import wpsPeasantFamily.Agent.Guards.ToControlMessage;
import wpsPeasantFamily.Data.PeasantFamilyProfile;
import rational.data.InfoData;
import rational.mapping.Believes;
import wpsControl.Agent.ControlCurrentDate;
import wpsPeasant.EmotionalModel.EmotionalState;
import wpsPeasantFamily.Data.CropCareType;
import wpsPeasantFamily.Data.FarmingResource;
import wpsPeasantFamily.Data.MoneyOriginType;
import wpsPeasantFamily.Data.PeasantActivityType;
import wpsPeasantFamily.Data.PeasantLeisureType;
import wpsPeasantFamily.Data.ResourceNeededType;
import wpsPeasantFamily.Data.SeasonType;
import wpsPeasantFamily.Data.TimeConsumedBy;
import wpsViewer.Agent.wpsReport;

/**
 *
 * @author jairo
 */
public class PeasantFamilyBDIAgentBelieves implements Believes {

    private PeasantFamilyProfile peasantProfile;
    private EmotionalState peasantEmotionalState;
    private SeasonType currentSeason;
    private CropCareType currentCropCare;
    private MoneyOriginType currentMoneyOrigin;
    private PeasantActivityType currentPeasantActivityType;
    private PeasantLeisureType currentPeasantLeisureType;
    private ResourceNeededType currentResourceNeededType;

    private int currentDay;
    private int robberyAccount;

    private double timeLeftOnDay;
    private boolean newDay;
    private boolean checkedToday;
    private boolean robbedToday;
    private boolean askedForLoanToday;
    private boolean haveLoan;
    private boolean weekBlock;
    private boolean busy;
    private double toPay;
    private boolean loanDenied;

    private String internalCurrentDate;
    private String ptwDate;

    private Map<String, FarmingResource> priceList = new HashMap<>();

    /**
     *
     * @param alias Peasant Family Alias
     * @param peasantProfile profile of the peasant family
     */
    public PeasantFamilyBDIAgentBelieves(String alias, PeasantFamilyProfile peasantProfile) {
        this.setPeasantProfile(peasantProfile);
        this.internalCurrentDate = ControlCurrentDate.getInstance().getCurrentDate();
        this.peasantProfile.setPeasantFamilyAlias(alias);
        this.peasantEmotionalState = new EmotionalState();

        this.busy = false;
        this.currentDay = 1;
        this.timeLeftOnDay = 24;
        this.checkedToday = false;
        this.askedForLoanToday = false;
        this.robbedToday = false;
        this.haveLoan = false;
        this.newDay = true;
        this.weekBlock = false;
        this.priceList.clear();
        this.loanDenied = false;
        this.ptwDate = "";

        this.currentSeason = SeasonType.NONE;
        this.currentCropCare = CropCareType.NONE;
        this.currentMoneyOrigin = MoneyOriginType.NONE;
        this.currentPeasantActivityType = PeasantActivityType.NONE;
        this.currentPeasantLeisureType = PeasantLeisureType.NONE;

    }
    public boolean isHaveLoan() {
        return haveLoan;
    }

    public void setHaveLoan(boolean haveLoan) {
        this.haveLoan = haveLoan;
    }

    public boolean isAskedForLoanToday() {
        return askedForLoanToday;
    }

    public void setAskedForLoanToday() {
        this.askedForLoanToday = true;
    }

    public boolean isRobbedToday() {
        return robbedToday;
    }

    public void setRobbedToday() {
        this.robbedToday = false;
    }

    public boolean isCheckedToday() {
        return checkedToday;
    }

    public void setCheckedToday() {
        this.checkedToday = true;
    }

    public int getRobberyAccount() {
        return robberyAccount;
    }

    public void increaseRobberyAccount() {
        this.robberyAccount++;
    }

    public String getPtwDate() {
        return ptwDate;
    }

    public void setPtwDate(String ptwDate) {
        this.ptwDate = ptwDate;
    }

    /**
     *
     * Make variable reset Every Day
     */
    public void makeNewDay() {
        this.currentDay++;
        this.timeLeftOnDay = 24;
        this.newDay = true;
        if (this.currentSeason == SeasonType.GROWING) {
            this.checkedToday = false;
        }
        this.robbedToday = false;
        this.askedForLoanToday = false;
        this.internalCurrentDate = ControlCurrentDate.getInstance().getDatePlusOneDay(internalCurrentDate);
        wpsReport.debug(this.toJson(), this.getPeasantProfile().getPeasantFamilyAlias());
    }

    /**
     *
     * @return
     */
    public int getCurrentDay() {
        return currentDay;
    }

    /**
     *
     * @param currentDay
     */
    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    /**
     *
     * @return
     */
    public double getTimeLeftOnDay() {
        return timeLeftOnDay;
    }

    /**
     *
     * @param timeLeftOnDay
     */
    public void setTimeLeftOnDay(double timeLeftOnDay) {
        this.timeLeftOnDay = timeLeftOnDay;
    }

    /**
     *
     * @return
     */
    public String getInternalCurrentDate() {
        return internalCurrentDate;
    }

    /**
     *
     * @param internalCurrentDate
     */
    public void setInternalCurrentDate(String internalCurrentDate) {
        this.internalCurrentDate = internalCurrentDate;
    }

    /**
     * Time unit defined by hours spent on activities.
     *
     * @param time
     */
    public void useTime(TimeConsumedBy time) {
        decreaseTime(time.getTime());
    }

    /**
     * Time unit defined by hours spent on activities.
     *
     * @param time
     */
    public void useTime(double time) {
        decreaseTime(time);
    }

    /**
     * Time unit defined by hours spent on activities.
     *
     * @param time
     */
    public synchronized void decreaseTime(double time) {
        this.timeLeftOnDay = this.timeLeftOnDay - time;
        if (this.timeLeftOnDay <= 0) {
            /*wpsReport.info("🌤️🌤️  NewDay para "
                    + this.peasantProfile.getPeasantFamilyAlias()
                    + " con "
                    + this.peasantProfile.getHealth()
                    + " de Salud.",
                    this.getPeasantProfile().getPeasantFamilyAlias()
            );*/
            //wpsReport.debug(toJson(), this.getPeasantProfile().getPeasantFamilyAlias());
            this.makeNewDay();
        } else {
            /*wpsReport.info("⏱️⏱️  "
                    + this.peasantProfile.getPeasantFamilyAlias()
                    + " Le quedan "
                    + this.timeLeftOnDay
                    + " horas del día "
                    + internalCurrentDate
                    + " con "
                    + this.peasantProfile.getHealth()
                    + " de Salud."
            );*/
        }
        try {
            Thread.sleep((long) (50 * time));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param time
     * @return
     */
    public boolean haveTimeAvailable(TimeConsumedBy time) {
        return this.timeLeftOnDay - time.getTime() >= 0;
        //wpsReport.info("⏳🚩⏳🚩⏳ No alcanza le tiempo " + time.getTime() + " tiene " + this.timeLeftOnDay + " del día " + wpsCurrentDate.getInstance().getCurrentDate());
        //wpsReport.info("⏳ ⏳ ⏳ Todavía tiene " + this.timeLeftOnDay + " en el día " + wpsCurrentDate.getInstance().getCurrentDate());
    }

    /**
     * Check if is a new Day
     *
     * @return true if is a new day
     */
    public boolean isNewDay() {
        return this.newDay;
    }

    /**
     * Set a new Day false
     *
     * @param newDay
     */
    public void setNewDay(boolean newDay) {
        this.newDay = newDay;
    }

    /**
     *
     */
    public void releaseWeekBlock() {
        this.weekBlock = false;
    }

    /**
     *
     */
    public void setWeekBlock() {
        this.weekBlock = true;
    }

    /**
     *
     * @return
     */
    public boolean getWeekBlock() {
        return this.weekBlock;
    }

    /**
     *
     * @return
     */
    public ResourceNeededType getCurrentResourceNeededType() {
        return currentResourceNeededType;
    }

    /**
     *
     *
     */
    public void setCurrentResourceNeededType(ResourceNeededType currentResourceNeededType) {
        this.currentResourceNeededType = currentResourceNeededType;
    }

    /**
     *
     * @return
     */
    public PeasantLeisureType getCurrentPeasantLeisureType() {
        return currentPeasantLeisureType;
    }

    /**
     *
     *
     * @param currentPeasantLeisureType
     */
    public void setCurrentPeasantLeisureType(PeasantLeisureType currentPeasantLeisureType) {
        this.currentPeasantLeisureType = currentPeasantLeisureType;
    }

    /**
     *
     *
     */
    public void setRandomCurrentPeasantLeisureType() {
        Random rand = new Random();

        switch (rand.nextInt(1)) {
            case 0 -> this.currentPeasantLeisureType = PeasantLeisureType.LEISURE;
            case 1 -> this.currentPeasantLeisureType = PeasantLeisureType.WASTERESOURCE;
            //case 2 -> this.currentPeasantLeisureType = PeasantLeisureType.WASTERESOURCE;
        }
    }

    /**
     *
     * @return
     */
    public SeasonType getCurrentSeason() {
        return currentSeason;
    }

    /**
     *
     * @param currentSeason the currentSeason to set
     */
    public void setCurrentSeason(SeasonType currentSeason) {
        this.currentSeason = currentSeason;
    }

    /**
     *
     * @return
     */
    public CropCareType getCurrentCropCare() {
        return currentCropCare;
    }

    /**
     *
     * @param currentCropCare the currentCropCare to set
     */
    public void setCurrentCropCare(CropCareType currentCropCare) {
        this.currentCropCare = currentCropCare;
    }

    /**
     *
     * @return
     */
    public MoneyOriginType getCurrentMoneyOrigin() {
        return currentMoneyOrigin;
    }

    /**
     *
     * @param currentMoneyOrigin the currentMoneyOrigin to set
     */
    public void setCurrentMoneyOrigin(MoneyOriginType currentMoneyOrigin) {
        this.currentMoneyOrigin = currentMoneyOrigin;
    }

    public PeasantActivityType getCurrentActivity() {
        return this.currentPeasantActivityType;
    }

    public void setCurrentActivity(PeasantActivityType peasantActivityType) {
        this.currentPeasantActivityType = peasantActivityType;
    }

    /**
     *
     * @return the currentPeasantActivityType
     */
    public EmotionalState getPeasantEmotionalState() {
        return peasantEmotionalState;
    }

    /**
     *
     * @return the currentPeasantActivityType
     */
    public PeasantFamilyProfile getPeasantProfile() {
        return peasantProfile;
    }

    /**
     *
     * @param peasantProfile the peasantProfile to set
     */
    private void setPeasantProfile(PeasantFamilyProfile peasantProfile) {
        this.peasantProfile = peasantProfile;
    }

    /**
     *
     * @param infoData
     * @return
     */
    @Override
    public boolean update(InfoData infoData) {
        return true;
    }

    /**
     *
     * @param priceList the priceList to set
     */
    public void setPriceList(Map<String, FarmingResource> priceList) {
        this.priceList = priceList;
    }

    /**
     *
     * @return the priceList
     */
    public Map<String, FarmingResource> getPriceList() {
        return priceList;
    }

    /**
     *
     * @return @throws CloneNotSupportedException
     */
    @Override
    public Believes clone() throws CloneNotSupportedException {
        return this;
    }

    /**
     *
     * @return
     */
    public boolean isFree() {
        return !this.busy;
    }

    /**
     *
     * @return
     */
    public boolean isBusy() {
        return this.busy;
    }

    /**
     *
     * @param busy
     */
    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    /**
     *
     * @return
     */
    public String toJson() {
        return "PeasantFamilyBDIAgentBelieves{" + "peasantProfile=" + peasantProfile.getPeasantFamilyAlias()
                + ",peasantEmotionalState=" + peasantEmotionalState + ",currentSeason="
                + currentSeason + ",currentCropCare=" + currentCropCare
                + ",robberyAccount=" + robberyAccount + ",ptwDate=" + ptwDate
                + ",currentMoneyOrigin=" + currentMoneyOrigin + ",currentPeasantActivityType="
                + currentPeasantActivityType + ", currentPeasantLeisureType=" + currentPeasantLeisureType
                + ",currentResourceNeededType=" + currentResourceNeededType + ",currentDay="
                + currentDay + ",timeLeftOnDay=" + timeLeftOnDay
                + ",newDay=" + newDay + ",weekBlock="
                + weekBlock + ",busy=" + busy + ",askedForLoanToday=" + askedForLoanToday
                + ",robbedToday=" + robbedToday + ",checkedToday=" + checkedToday
                + ",internalCurrentDate=" + internalCurrentDate + ",toPay=" + toPay
                + peasantProfile.toJson();
    }

    @Override
    public String toString() {
        return "\n"
                + " * ==========================================================================\n"
                + " * wpsPeasantFamilyProfile: " + peasantProfile.getPeasantFamilyAlias() + "\n"
                + " * ==========================================================================\n"
                + " * PeasantEmotionalState: " + peasantEmotionalState + "\n"
                + " * CurrentSeason: " + currentSeason + "\n"
                + " * CurrentCropCare: " + currentCropCare + "\n"
                + " * CurrentMoneyOrigin: " + currentMoneyOrigin + "\n"
                + " * PeasantActivityType: " + currentPeasantActivityType + "\n"
                + " * currentPeasantLeisureType: " + currentPeasantLeisureType + "\n"
                + " * robberyAccount: " + robberyAccount + "\n"
                + " * ptwDate: " + ptwDate + "\n"
                + " * CurrentDay: " + currentDay + "\n"
                + " * TimeLeftOnDay: " + timeLeftOnDay + "\n"
                + " * NewDay: " + newDay + "\n"
                + " * WeekBlock: " + weekBlock + "\n"
                + " * Busy: " + busy + "\n"
                + " * InternalCurrentDate: " + internalCurrentDate + "\n"
                + " * Price List: " + priceList + "\n"
                + " * ==========================================================================\n"
                + peasantProfile.toString();
    }

    public double getToPay() {
        return toPay;
    }

    public void setToPay(double toPay) {
        this.toPay = toPay;
    }
    public void discountToPay(double toPay) {
        this.toPay -= toPay;
    }

    public boolean isLoanDenied() {
        return loanDenied;
    }

    public void setLoanDenied(boolean loanDenied) {
        this.loanDenied = loanDenied;
    }

    public void decreaseHealth() {
        this.peasantProfile.decreaseHealth();
        if (this.getPeasantProfile().getHealth()<=0) {
            try {
                wpsReport.debug("👻👻 murió agente " + this.peasantProfile.getPeasantFamilyAlias() + " 👻👻", this.peasantProfile.getPeasantFamilyAlias());
                AdmBESA adm = AdmBESA.getInstance();
                AgHandlerBESA agHandler = adm.getHandlerByAlias(this.peasantProfile.getPeasantFamilyAlias());
                adm.killAgent(agHandler.getAgId(),wpsStart.PASSWD);
            } catch (ExceptionBESA ex) {
                wpsReport.error(ex, this.peasantProfile.getPeasantFamilyAlias());
            }
        }
    }
}
